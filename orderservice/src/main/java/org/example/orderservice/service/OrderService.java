package org.example.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.event.TicketOrderPlacedEvent;
import org.example.grpc.SeatResponse;
import org.example.orderservice.grpc.InventoryGrpcClient;
import org.example.orderservice.grpc.SeatUnavailableException;
import org.example.orderservice.model.OutboxEvent;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.OutboxRepository;
import org.example.orderservice.repository.TicketOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        this.orderRepository     = orderRepository;
        this.outboxRepository    = outboxRepository;
        this.objectMapper        = objectMapper;
        this.inventoryGrpcClient = inventoryGrpcClient;
    }

    @Transactional
    public TicketOrder placeOrder(TicketOrder order) throws JsonProcessingException {
        // gRPC pre-flight — passes userId so click-holds are respected
        SeatResponse seatResponse = inventoryGrpcClient.checkSeatAvailability(
                order.getSeatId(),
                order.getTicketEventId(),
                order.getQuantity(),
                order.getUserId()
        );

        if (!seatResponse.getAvailable()) {
            logger.warn("Seat {} unavailable ({}), rejecting order",
                    order.getSeatId(), seatResponse.getReason());
            throw new SeatUnavailableException(
                    "Seat " + order.getSeatId() + " is not available: " + seatResponse.getReason());
        }

        TicketOrder saved = orderRepository.save(order);

        TicketOrderPlacedEvent event = new TicketOrderPlacedEvent(
                UUID.randomUUID(),
                saved.getId(),
                saved.getTicketEventId(),
                saved.getSeatId(),
                saved.getQuantity(),
                saved.getTotalPrice(),
                saved.getUserId()
        );

        outboxRepository.save(new OutboxEvent(
                event.eventId(), "TICKET_ORDER", saved.getId(),
                "TICKET_ORDER_PLACED", objectMapper.writeValueAsString(event)
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

    public List<TicketOrder> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
