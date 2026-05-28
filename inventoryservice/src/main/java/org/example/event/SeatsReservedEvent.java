package org.example.event;

import java.util.UUID;

public record SeatsReservedEvent(
        UUID eventId,
        Long orderId
) {}
