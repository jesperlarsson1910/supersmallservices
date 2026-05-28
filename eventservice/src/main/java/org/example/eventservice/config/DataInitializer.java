package org.example.eventservice.config;

import org.example.eventservice.model.TicketEvent;
import org.example.eventservice.model.Venue;
import org.example.eventservice.repository.TicketEventRepository;
import org.example.eventservice.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(VenueRepository venueRepository,
                                      TicketEventRepository eventRepository) {
        return args -> {
            if (venueRepository.count() > 0) return;

            Venue gothenburg = venueRepository.save(
                new Venue("Scandinavium", "Gothenburg", "Sweden", 12000));
            Venue stockholm = venueRepository.save(
                new Venue("Avicii Arena", "Stockholm", "Sweden", 16000));

            eventRepository.save(new TicketEvent(
                "Summer Sound Festival",
                "The biggest outdoor festival of the year",
                "Various Artists",
                LocalDateTime.of(2026, 7, 15, 18, 0),
                gothenburg
            ));

            TicketEvent onSale = new TicketEvent(
                "The Weeknd – After Hours Tour",
                "World tour stop in Stockholm",
                "The Weeknd",
                LocalDateTime.of(2026, 9, 3, 20, 0),
                stockholm
            );
            onSale.setStatus(TicketEvent.EventStatus.ON_SALE);
            eventRepository.save(onSale);

            eventRepository.save(new TicketEvent(
                "Coldplay – Music of the Spheres",
                "Spectacular light show and world hits",
                "Coldplay",
                LocalDateTime.of(2026, 8, 22, 19, 30),
                gothenburg
            ));
        };
    }
}
