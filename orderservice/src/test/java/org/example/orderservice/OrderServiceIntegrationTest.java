package org.example.orderservice;

import org.example.orderservice.grpc.InventoryGrpcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.OutboxRepository;
import org.example.orderservice.repository.TicketOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.example.grpc.SeatResponse;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TicketOrderRepository orderRepository;
    @Autowired OutboxRepository outboxRepository;

    @MockitoBean
    InventoryGrpcClient inventoryGrpcClient;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void placeOrder_seatAvailable_returnsCreated() throws Exception {
        // Arrange — mock gRPC response
        when(inventoryGrpcClient.checkSeatAvailability(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(SeatResponse.newBuilder()
                        .setAvailable(true)
                        .setSeatNumber("A1")
                        .setSection("Floor")
                        .setPrice(149.0)
                        .build());

        TicketOrder order = new TicketOrder(1L, 1L, 1L, 1, new BigDecimal("149.00"));

        // Act + Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.seatId").value(1));

        // Verify outbox event was written
        assertThat(outboxRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll().get(0).getType()).isEqualTo("TICKET_ORDER_PLACED");
    }

    @Test
    void placeOrder_seatUnavailable_returnsConflict() throws Exception {
        // Arrange — gRPC says seat is held
        when(inventoryGrpcClient.checkSeatAvailability(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(SeatResponse.newBuilder()
                        .setAvailable(false)
                        .setReason("SEAT_HELD")
                        .build());

        TicketOrder order = new TicketOrder(1L, 1L, 1L, 1, new BigDecimal("149.00"));

        // Act + Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isConflict());

        // Nothing should be written to DB or outbox
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(outboxRepository.findAll()).isEmpty();
    }

    @Test
    void getOrder_exists_returnsOrder() throws Exception {
        // Arrange
        when(inventoryGrpcClient.checkSeatAvailability(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(SeatResponse.newBuilder().setAvailable(true).build());

        TicketOrder order = new TicketOrder(1L, 1L, 2L, 1, new BigDecimal("89.00"));
        order = orderRepository.save(order);

        // Act + Assert
        mockMvc.perform(get("/orders/" + order.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    void getOrder_notFound_returns500() throws Exception {
        mockMvc.perform(get("/orders/99999")
                        .header("X-User-Id", "1"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void cancelOrder_updatesStatus() throws Exception {
        // Arrange
        TicketOrder order = new TicketOrder(1L, 1L, 3L, 1, new BigDecimal("199.00"));
        order = orderRepository.save(order);

        // Act + Assert
        mockMvc.perform(delete("/orders/" + order.getId() + "/cancel")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());

        TicketOrder updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TicketOrder.OrderStatus.CANCELLED_EXPIRED);
    }
}
