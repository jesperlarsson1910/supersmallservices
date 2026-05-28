package org.example.eventservice.controller;

import org.example.eventservice.model.TicketEvent;
import org.example.eventservice.model.Venue;
import org.example.eventservice.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // --- Events ---

    @GetMapping("/events")
    public List<TicketEvent> getAllEvents(
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {

        if (artist != null) return eventService.searchByArtist(artist);
        if (city != null)   return eventService.searchByCity(city);
        if ("ON_SALE".equalsIgnoreCase(status)) return eventService.getOnSaleEvents();
        return eventService.getAllEvents();
    }

    @GetMapping("/events/{id}")
    public TicketEvent getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketEvent createEvent(@RequestBody TicketEvent event) {
        return eventService.createEvent(event);
    }

    @PatchMapping("/events/{id}/status")
    public TicketEvent updateStatus(@PathVariable Long id,
                                    @RequestParam TicketEvent.EventStatus status) {
        return eventService.updateStatus(id, status);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    // --- Venues ---

    @GetMapping("/venues")
    public List<Venue> getAllVenues() {
        return eventService.getAllVenues();
    }

    @GetMapping("/venues/{id}")
    public Venue getVenue(@PathVariable Long id) {
        return eventService.getVenue(id);
    }

    @PostMapping("/venues")
    @ResponseStatus(HttpStatus.CREATED)
    public Venue createVenue(@RequestBody Venue venue) {
        return eventService.createVenue(venue);
    }
}
