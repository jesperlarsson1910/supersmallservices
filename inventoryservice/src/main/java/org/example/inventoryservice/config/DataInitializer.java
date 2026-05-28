package org.example.inventoryservice.config;

import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.repository.SeatRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initSeats(SeatRepository seatRepository) {
        return args -> {
            if (seatRepository.count() > 0) return;

            // Event 1 — floor seats
            String[] floorSeats = {"A1","A2","A3","A4","A5","B1","B2","B3","B4","B5"};
            for (String s : floorSeats) {
                seatRepository.save(new Seat(1L, s, "Floor", new BigDecimal("149.00")));
            }

            // Event 1 — balcony seats
            String[] balconySeats = {"C1","C2","C3","C4","C5","D1","D2","D3","D4","D5"};
            for (String s : balconySeats) {
                seatRepository.save(new Seat(1L, s, "Balcony", new BigDecimal("89.00")));
            }

            // Event 2 — floor seats
            String[] event2Seats = {"A1","A2","A3","B1","B2","B3"};
            for (String s : event2Seats) {
                seatRepository.save(new Seat(2L, s, "Floor", new BigDecimal("199.00")));
            }
        };
    }
}
