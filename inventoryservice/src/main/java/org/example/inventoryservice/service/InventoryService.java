package org.example.inventoryservice.service;

import org.example.event.SeatsReservationFailedEvent;
import org.example.event.SeatsReservedEvent;
import org.example.event.SeatHoldExpiredEvent;
import org.example.event.TicketOrderPlacedEvent;
import org.example.inventoryservice.config.RabbitConfig;
import org.example.inventoryservice.model.ProcessedEvent;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.repository.ProcessedEventRepository;
import org.example.inventoryservice.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private static final int HOLD_MINUTES = 15;

    private final SeatRepository seatRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryService(SeatRepository seatRepository,
                            ProcessedEventRepository processedEventRepository,
                            RabbitTemplate rabbitTemplate) {
        this.seatRepository = seatRepository;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void processOrder(TicketOrderPlacedEvent event) {
        logger.info("Processing ticket order event: {}", event.eventId());

        // Idempotency check — same as the original StockService
        if (processedEventRepository.existsById(event.eventId())) {
            logger.info("Event {} already processed, skipping", event.eventId());
            return;
        }

        Seat seat = seatRepository.findById(event.seatId()).orElse(null);

        if (seat == null || seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            logger.warn("Seat {} unavailable for order {}", event.seatId(), event.orderId());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    "seats.reservation.failed",
                    new SeatsReservationFailedEvent(UUID.randomUUID(), event.orderId(), "SEAT_UNAVAILABLE")
            );

            processedEventRepository.save(new ProcessedEvent(event.eventId()));
            return;
        }

        // Place hold with TTL
        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldByOrderId(event.orderId());
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(HOLD_MINUTES));
        seatRepository.save(seat);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                "seats.reserved",
                new SeatsReservedEvent(UUID.randomUUID(), event.orderId())
        );

        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        logger.info("Seat {} held for order {} until {}", seat.getId(), event.orderId(), seat.getHoldExpiresAt());
    }

    // Called by BFF/order-service when payment is confirmed
    @Transactional
    public void confirmSeat(Long seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setStatus(Seat.SeatStatus.SOLD);
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);
            logger.info("Seat {} marked as SOLD", seatId);
        });
    }

    // Called by BFF/order-service to release a held seat
    @Transactional
    public void releaseSeat(Long seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldByOrderId(null);
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);
            logger.info("Seat {} released back to AVAILABLE", seatId);
        });
    }

    // Runs every minute — publishes seat.hold.expired for bot service to consume
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireHeldSeats() {
        List<Seat> expiredSeats = seatRepository.findByStatusAndHoldExpiresAtBefore(
                Seat.SeatStatus.HELD, LocalDateTime.now()
        );

        for (Seat seat : expiredSeats) {
            logger.warn("Hold expired for seat {} (order {})", seat.getId(), seat.getHeldByOrderId());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    "seat.hold.expired",
                    new SeatHoldExpiredEvent(UUID.randomUUID(), seat.getId(), seat.getHeldByOrderId())
            );

            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldByOrderId(null);
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);
        }

        if (!expiredSeats.isEmpty()) {
            logger.info("Released {} expired seat holds", expiredSeats.size());
        }
    }

    public List<Seat> getAvailableSeats(Long ticketEventId) {
        return seatRepository.findByTicketEventIdAndStatus(ticketEventId, Seat.SeatStatus.AVAILABLE);
    }
}
