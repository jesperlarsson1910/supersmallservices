package org.example.botservice.model;

import java.util.UUID;

public record SeatHoldExpiredEvent(
    UUID eventId,
    Long seatId,
    Long orderId
) {}
