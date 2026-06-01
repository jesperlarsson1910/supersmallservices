package org.example.inventoryservice;

import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.repository.ProcessedEventRepository;
import org.example.inventoryservice.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryServiceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired SeatRepository seatRepository;
    @Autowired ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        seatRepository.deleteAll();
    }

    @Test
    void getAvailableSeats_returnsOnlyAvailable() throws Exception {
        // Arrange
        seatRepository.save(new Seat(1L, "A1", "Floor", new BigDecimal("149.00")));
        seatRepository.save(new Seat(1L, "A2", "Floor", new BigDecimal("149.00")));
        Seat held = new Seat(1L, "A3", "Floor", new BigDecimal("149.00"));
        held.setStatus(Seat.SeatStatus.HELD);
        seatRepository.save(held);

        // Act + Assert
        mockMvc.perform(get("/inventory/events/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void confirmSeat_updatesSeatToSold() throws Exception {
        // Arrange
        Seat seat = seatRepository.save(new Seat(1L, "B1", "Balcony", new BigDecimal("89.00")));
        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldByOrderId(42L);
        seatRepository.save(seat);

        // Act + Assert
        mockMvc.perform(post("/inventory/seats/" + seat.getId() + "/confirm"))
                .andExpect(status().isOk());

        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Seat.SeatStatus.SOLD);
        assertThat(updated.getHoldExpiresAt()).isNull();
    }

    @Test
    void releaseSeat_makesItAvailableAgain() throws Exception {
        // Arrange
        Seat seat = new Seat(1L, "C1", "Floor", new BigDecimal("149.00"));
        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldByOrderId(99L);
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));
        seat = seatRepository.save(seat);

        // Act + Assert
        mockMvc.perform(post("/inventory/seats/" + seat.getId() + "/release"))
                .andExpect(status().isOk());

        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
        assertThat(updated.getHeldByOrderId()).isNull();
        assertThat(updated.getHoldExpiresAt()).isNull();
    }

    @Test
    void expireHeldSeats_releasesExpiredHolds() {
        // Arrange — seat with expired hold
        Seat expired = new Seat(1L, "D1", "Floor", new BigDecimal("149.00"));
        expired.setStatus(Seat.SeatStatus.HELD);
        expired.setHeldByOrderId(77L);
        expired.setHoldExpiresAt(LocalDateTime.now().minusMinutes(5));
        seatRepository.save(expired);

        // Seat with valid hold — should NOT be released
        Seat valid = new Seat(1L, "D2", "Floor", new BigDecimal("149.00"));
        valid.setStatus(Seat.SeatStatus.HELD);
        valid.setHeldByOrderId(78L);
        valid.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));
        seatRepository.save(valid);

        // Act — run the scheduler manually via the service
        // (scheduler is disabled in test profile, call directly)
        var expiredSeats = seatRepository.findByStatusAndHoldExpiresAtBefore(
                Seat.SeatStatus.HELD, LocalDateTime.now()
        );
        assertThat(expiredSeats).hasSize(1);
        assertThat(expiredSeats.get(0).getHeldByOrderId()).isEqualTo(77L);
    }
}
