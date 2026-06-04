package org.example.notificationservice.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketPurchasedEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        Long ticketEventId,
        String userName,
        String userEmail,
        Integer quantity,
        BigDecimal totalPrice
) {}
