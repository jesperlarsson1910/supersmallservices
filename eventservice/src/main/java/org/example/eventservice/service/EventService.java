package org.example.eventservice.service;

import org.example.eventservice.model.TicketEvent;
import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.TicketEventRepository;
import org.example.eventservice.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final TicketEventRepository eventRepository;
    private final VenueRepository venueRepository;

    public EventService(TicketEventRepository eventRepository, VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    // --- Events ---

    public List<TicketEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<TicketEvent> getOnSaleEvents() {
        return eventRepository.findByStatus(TicketEvent.EventStatus.ON_SALE);
    }

    public List<TicketEvent> searchByArtist(String artist) {
        return eventRepository.findByArtistContainingIgnoreCase(artist);
    }

    public List<TicketEvent> searchByCity(String city) {
        return eventRepository.findByVenueCity(city);
    }

    public TicketEvent getEvent(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    @Transactional
    public TicketEvent createEvent(TicketEvent event) {
        return eventRepository.save(event);
    }

    @Transactional
    public TicketEvent updateStatus(Long id, TicketEvent.EventStatus status) {
        TicketEvent event = getEvent(id);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    // --- Venues ---

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenue(Long id) {
        return venueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venue not found: " + id));
    }

    @Transactional
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }
}
