package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTicketEventIdAndStatus(Long ticketEventId, Seat.SeatStatus status);
    List<Seat> findByStatusAndHoldExpiresAtBefore(Seat.SeatStatus status, LocalDateTime now);
}
