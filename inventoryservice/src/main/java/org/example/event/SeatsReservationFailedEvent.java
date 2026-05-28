package org.example.event;

import java.util.UUID;

public record SeatsReservationFailedEvent(
        UUID eventId,
        Long orderId,
        String reason
) {}
