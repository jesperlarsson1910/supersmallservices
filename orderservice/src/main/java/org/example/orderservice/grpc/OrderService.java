package org.example.orderservice.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.event.TicketOrderPlacedEvent;
import org.example.grpc.SeatResponse;
import org.example.orderservice.grpc.InventoryGrpcClient;
import org.example.orderservice.model.OutboxEvent;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.OutboxRepository;
import org.example.orderservice.repository.TicketOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final TicketOrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final InventoryGrpcClient inventoryGrpcClient;

    public OrderService(TicketOrderRepository orderRepository,
                        OutboxRepository outboxRepository,
                        ObjectMapper objectMapper,
                        InventoryGrpcClient inventoryGrpcClient) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.inventoryGrpcClient = inventoryGrpcClient;
    }

    @Transactional
    public TicketOrder placeOrder(TicketOrder order) throws JsonProcessingException {

        // --- gRPC pre-flight check ---
        // Ask inventoryservice synchronously before touching the DB.
        // If the seat is gone we fail fast — no outbox event, no saga needed.
        SeatResponse seatResponse = inventoryGrpcClient.checkSeatAvailability(
            order.getSeatId(),
            order.getTicketEventId(),
            order.getQuantity()
        );

        if (!seatResponse.getAvailable()) {
            logger.warn("Seat {} unavailable (reason: {}), rejecting order",
                order.getSeatId(), seatResponse.getReason());
            throw new SeatUnavailableException(
                "Seat " + order.getSeatId() + " is not available: " + seatResponse.getReason()
            );
        }

        logger.info("gRPC confirmed seat {} ({} / {}) available at {}",
            order.getSeatId(),
            seatResponse.getSeatNumber(),
            seatResponse.getSection(),
            seatResponse.getPrice());

        // --- Save order ---
        TicketOrder saved = orderRepository.save(order);

        // --- Write to outbox (triggers RabbitMQ saga for actual hold) ---
        TicketOrderPlacedEvent event = new TicketOrderPlacedEvent(
            UUID.randomUUID(),
            saved.getId(),
            saved.getTicketEventId(),
            saved.getSeatId(),
            saved.getQuantity(),
            saved.getTotalPrice()
        );

        String payload = objectMapper.writeValueAsString(event);

        outboxRepository.save(new OutboxEvent(
            event.eventId(),
            "TICKET_ORDER",
            saved.getId(),
            "TICKET_ORDER_PLACED",
            payload
        ));

        return saved;
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(TicketOrder.OrderStatus.CANCELLED_EXPIRED);
            orderRepository.save(order);
        });
    }

    public TicketOrder getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}
