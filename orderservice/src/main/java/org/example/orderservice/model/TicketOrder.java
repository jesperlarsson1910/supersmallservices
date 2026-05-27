package org.example.orderservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_orders")
public class TicketOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketEventId; // references event-service
    private Long userId;        // from JWT
    private Long seatId;        // which seat in inventory-service
    private Integer quantity;
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED_SEATS_UNAVAILABLE,
        CANCELLED_EXPIRED
    }

    public TicketOrder() {}

    public TicketOrder(Long ticketEventId, Long userId, Long seatId, Integer quantity, BigDecimal totalPrice) {
        this.ticketEventId = ticketEventId;
        this.userId = userId;
        this.seatId = seatId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public Long getTicketEventId() { return ticketEventId; }
    public void setTicketEventId(Long ticketEventId) { this.ticketEventId = ticketEventId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
