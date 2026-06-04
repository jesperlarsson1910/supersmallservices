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

    private Long ticketEventId;
    private String seatNumber;
    private String section;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private Long heldByOrderId;
    private Long heldByUserId;          // set on click-to-hold
    private LocalDateTime holdExpiresAt;

    public enum SeatStatus {
        AVAILABLE, HELD, SOLD
    }

    public Seat() {}

    public Seat(Long ticketEventId, String seatNumber, String section, BigDecimal price) {
        this.ticketEventId = ticketEventId;
        this.seatNumber    = seatNumber;
        this.section       = section;
        this.price         = price;
    }

    public Long getId()                          { return id; }
    public Long getTicketEventId()               { return ticketEventId; }
    public void setTicketEventId(Long v)         { this.ticketEventId = v; }
    public String getSeatNumber()                { return seatNumber; }
    public void setSeatNumber(String v)          { this.seatNumber = v; }
    public String getSection()                   { return section; }
    public void setSection(String v)             { this.section = v; }
    public BigDecimal getPrice()                 { return price; }
    public void setPrice(BigDecimal v)           { this.price = v; }
    public SeatStatus getStatus()                { return status; }
    public void setStatus(SeatStatus v)          { this.status = v; }
    public Long getHeldByOrderId()               { return heldByOrderId; }
    public void setHeldByOrderId(Long v)         { this.heldByOrderId = v; }
    public Long getHeldByUserId()                { return heldByUserId; }
    public void setHeldByUserId(Long v)          { this.heldByUserId = v; }
    public LocalDateTime getHoldExpiresAt()      { return holdExpiresAt; }
    public void setHoldExpiresAt(LocalDateTime v){ this.holdExpiresAt = v; }
}
