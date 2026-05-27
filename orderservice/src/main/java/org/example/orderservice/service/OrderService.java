package org.example.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.event.TicketOrderPlacedEvent;
import org.example.orderservice.model.OutboxEvent;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.OutboxRepository;
import org.example.orderservice.repository.TicketOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private final TicketOrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(TicketOrderRepository orderRepository,
                        OutboxRepository outboxRepository,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TicketOrder placeOrder(TicketOrder order) throws JsonProcessingException {
        TicketOrder saved = orderRepository.save(order);

        TicketOrderPlacedEvent event = new TicketOrderPlacedEvent(
                UUID.randomUUID(),
                saved.getId(),
                saved.getTicketEventId(),
                saved.getSeatId(),
                saved.getQuantity(),
                saved.getTotalPrice()
        );

        String payload = objectMapper.writeValueAsString(event);

        OutboxEvent outboxEvent = new OutboxEvent(
                event.eventId(),
                "TICKET_ORDER",
                saved.getId(),
                "TICKET_ORDER_PLACED",
                payload
        );

        outboxRepository.save(outboxEvent);

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
