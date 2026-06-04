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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger logger      = LoggerFactory.getLogger(InventoryService.class);
    private static final int CLICK_HOLD_MIN = 5;  // short hold when user clicks a seat
    private static final int ORDER_HOLD_MIN = 15; // longer hold while saga completes

    private final SeatRepository seatRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryService(SeatRepository seatRepository,
                            ProcessedEventRepository processedEventRepository,
                            RabbitTemplate rabbitTemplate) {
        this.seatRepository           = seatRepository;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate           = rabbitTemplate;
    }

    // Called by UI click — places a short 5-minute hold for the user
    @Transactional
    public Seat holdSeatForUser(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        if (seat.getStatus() == Seat.SeatStatus.SOLD) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SEAT_SOLD");
        }

        if (seat.getStatus() == Seat.SeatStatus.HELD) {
            // Allow same user to extend their own hold
            if (!userId.equals(seat.getHeldByUserId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "SEAT_HELD_BY_OTHER");
            }
        }

        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldByUserId(userId);
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(CLICK_HOLD_MIN));
        logger.info("Seat {} held for user {} until {}", seatId, userId, seat.getHoldExpiresAt());
        return seatRepository.save(seat);
    }

    // Called by RabbitMQ saga — confirms hold after order is placed
    @Transactional
    public void processOrder(TicketOrderPlacedEvent event) {
        logger.info("Processing ticket order event: {}", event.eventId());

        if (processedEventRepository.existsById(event.eventId())) {
            logger.info("Event {} already processed, skipping", event.eventId());
            return;
        }

        Seat seat = seatRepository.findById(event.seatId()).orElse(null);

        boolean canProceed = seat != null && (
                seat.getStatus() == Seat.SeatStatus.AVAILABLE ||
                        // Allow if user already has a click-hold on this seat
                        (seat.getStatus() == Seat.SeatStatus.HELD &&
                                event.userId() != null &&
                                event.userId().equals(seat.getHeldByUserId()))
        );

        if (!canProceed) {
            logger.warn("Seat {} unavailable for order {}", event.seatId(), event.orderId());
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "seats.reservation.failed",
                    new SeatsReservationFailedEvent(UUID.randomUUID(), event.orderId(), "SEAT_UNAVAILABLE"));
            processedEventRepository.save(new ProcessedEvent(event.eventId()));
            return;
        }

        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldByOrderId(event.orderId());
        seat.setHeldByUserId(null);
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(ORDER_HOLD_MIN));
        seatRepository.save(seat);

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "seats.reserved",
                new SeatsReservedEvent(UUID.randomUUID(), event.orderId()));

        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        logger.info("Seat {} held for order {} until {}", seat.getId(), event.orderId(), seat.getHoldExpiresAt());
    }

    @Transactional
    public void confirmSeat(Long seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setStatus(Seat.SeatStatus.SOLD);
            seat.setHoldExpiresAt(null);
            seat.setHeldByUserId(null);
            seatRepository.save(seat);
            logger.info("Seat {} marked as SOLD", seatId);
        });
    }

    @Transactional
    public void releaseSeat(Long seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldByOrderId(null);
            seat.setHeldByUserId(null);
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);
            logger.info("Seat {} released to AVAILABLE", seatId);
        });
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireHeldSeats() {
        List<Seat> expired = seatRepository.findByStatusAndHoldExpiresAtBefore(
                Seat.SeatStatus.HELD, LocalDateTime.now());

        for (Seat seat : expired) {
            logger.warn("Hold expired for seat {} (order={}, user={})",
                    seat.getId(), seat.getHeldByOrderId(), seat.getHeldByUserId());

            if (seat.getHeldByOrderId() != null) {
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "seat.hold.expired",
                        new SeatHoldExpiredEvent(UUID.randomUUID(), seat.getId(), seat.getHeldByOrderId()));
            }

            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldByOrderId(null);
            seat.setHeldByUserId(null);
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);
        }

        if (!expired.isEmpty()) logger.info("Released {} expired holds", expired.size());
    }

    public List<Seat> getAvailableSeats(Long ticketEventId) {
        return seatRepository.findByTicketEventIdAndStatus(ticketEventId, Seat.SeatStatus.AVAILABLE);
    }
}
