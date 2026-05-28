package org.example.notificationservice.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketPurchasedEvent(
    UUID eventId,
    Long orderId,
    Long userId,
    Long ticketEventId,
    String seatNumber,
    String section,
    Integer quantity,
    BigDecimal totalPrice
) {}
