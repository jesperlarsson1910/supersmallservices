package org.example.eventservice.repository;

import org.example.eventservice.model.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {
    List<TicketEvent> findByStatus(TicketEvent.EventStatus status);
    List<TicketEvent> findByArtistContainingIgnoreCase(String artist);
    List<TicketEvent> findByVenueCity(String city);
}
