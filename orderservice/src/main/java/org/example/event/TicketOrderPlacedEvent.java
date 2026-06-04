package org.example.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketOrderPlacedEvent(
        UUID eventId,
        Long orderId,
        Long ticketEventId,
        Long seatId,
        Integer quantity,
        BigDecimal totalPrice,
        Long userId
) {}
