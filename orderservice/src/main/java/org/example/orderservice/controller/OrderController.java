package org.example.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ChaosContext chaosContext;

    public OrderController(OrderService orderService, ChaosContext chaosContext) {
        this.orderService = orderService;
        this.chaosContext = chaosContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketOrder placeOrder(@RequestBody TicketOrder order,
                                  @RequestHeader("X-User-Id") Long userId)
            throws JsonProcessingException {
        order.setUserId(userId);

        TicketOrder saved = orderService.placeOrder(order);

        if (chaosContext.getCurrentScenario() == ChaosScenario.FAIL_BEFORE_PUBLISH) {
            throw new RuntimeException("Chaos: crash after save");
        }

        return saved;
    }

    @GetMapping("/{id}")
    public TicketOrder getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/user/{userId}")
    public List<TicketOrder> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    @DeleteMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }
}
