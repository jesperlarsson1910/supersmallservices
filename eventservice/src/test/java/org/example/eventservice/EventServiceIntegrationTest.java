package org.example.eventservice;

import tools.jackson.databind.ObjectMapper;
import org.example.eventservice.model.TicketEvent;
import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.TicketEventRepository;
import org.example.eventservice.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventServiceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TicketEventRepository eventRepository;
    @Autowired VenueRepository venueRepository;

    private Venue venue;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        venue = venueRepository.save(new Venue("Scandinavium", "Gothenburg", "Sweden", 12000));
    }

    @Test
    void getAllEvents_returnsAllEvents() throws Exception {
        eventRepository.save(new TicketEvent("Festival", "Desc", "Artist A",
                LocalDateTime.now().plusDays(30), venue));
        eventRepository.save(new TicketEvent("Concert", "Desc", "Artist B",
                LocalDateTime.now().plusDays(60), venue));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getEvent_exists_returnsEvent() throws Exception {
        TicketEvent event = eventRepository.save(new TicketEvent(
                "Summer Sound", "Big festival", "The Weeknd",
                LocalDateTime.now().plusDays(45), venue));

        mockMvc.perform(get("/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artist").value("The Weeknd"))
                .andExpect(jsonPath("$.venue.name").value("Scandinavium"));
    }

    @Test
    void createEvent_returnsCreated() throws Exception {
        TicketEvent event = new TicketEvent(
                "New Event", "Description", "Coldplay",
                LocalDateTime.now().plusDays(90), venue);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artist").value("Coldplay"))
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    @Test
    void updateStatus_toOnSale_updatesCorrectly() throws Exception {
        TicketEvent event = eventRepository.save(new TicketEvent(
                "Rock Night", "Desc", "Metallica",
                LocalDateTime.now().plusDays(20), venue));

        mockMvc.perform(patch("/events/" + event.getId() + "/status?status=ON_SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_SALE"));

        TicketEvent updated = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TicketEvent.EventStatus.ON_SALE);
    }

    @Test
    void searchByArtist_returnsMatchingEvents() throws Exception {
        eventRepository.save(new TicketEvent("Tour 1", "Desc", "Coldplay",
                LocalDateTime.now().plusDays(10), venue));
        eventRepository.save(new TicketEvent("Tour 2", "Desc", "Coldplay",
                LocalDateTime.now().plusDays(20), venue));
        eventRepository.save(new TicketEvent("Other", "Desc", "Other Artist",
                LocalDateTime.now().plusDays(30), venue));

        mockMvc.perform(get("/events?artist=Coldplay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteEvent_removesFromDb() throws Exception {
        TicketEvent event = eventRepository.save(new TicketEvent(
                "To Delete", "Desc", "Artist",
                LocalDateTime.now().plusDays(5), venue));

        mockMvc.perform(delete("/events/" + event.getId()))
                .andExpect(status().isNoContent());

        assertThat(eventRepository.findById(event.getId())).isEmpty();
    }

    @Test
    void createVenue_returnsCreated() throws Exception {
        Venue newVenue = new Venue("Avicii Arena", "Stockholm", "Sweden", 16000);

        mockMvc.perform(post("/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVenue)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Avicii Arena"))
                .andExpect(jsonPath("$.city").value("Stockholm"));
    }
}
