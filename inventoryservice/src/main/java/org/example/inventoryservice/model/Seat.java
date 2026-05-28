package org.example.inventoryservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketEventId; // references event-service
    private String seatNumber;  // e.g. "A12"
    private String section;     // e.g. "Floor", "Balcony"
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private Long heldByOrderId;         // which order holds this seat
    private LocalDateTime holdExpiresAt; // TTL for the hold (default: 15 min)

    public enum SeatStatus {
        AVAILABLE,
        HELD,
        SOLD
    }

    public Seat() {}

    public Seat(Long ticketEventId, String seatNumber, String section, BigDecimal price) {
        this.ticketEventId = ticketEventId;
        this.seatNumber = seatNumber;
        this.section = section;
        this.price = price;
    }

    public Long getId() { return id; }
    public Long getTicketEventId() { return ticketEventId; }
    public void setTicketEventId(Long ticketEventId) { this.ticketEventId = ticketEventId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public Long getHeldByOrderId() { return heldByOrderId; }
    public void setHeldByOrderId(Long heldByOrderId) { this.heldByOrderId = heldByOrderId; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
}
