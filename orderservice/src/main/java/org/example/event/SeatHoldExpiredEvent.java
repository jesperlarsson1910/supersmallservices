package org.example.event;

import java.util.UUID;

public record SeatHoldExpiredEvent(
        UUID eventId,
        Long seatId,
        Long orderId
) {}
