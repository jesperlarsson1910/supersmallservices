package org.example.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public TicketOrder placeOrder(@RequestBody TicketOrder order) throws JsonProcessingException {
        TicketOrder saved = orderService.placeOrder(order);

        if (chaosContext.getCurrentScenario() == ChaosScenario.FAIL_BEFORE_PUBLISH) {
            throw new RuntimeException("Chaos: crash after save, before publish");
        }

        return saved;
    }

    @GetMapping("/{id}")
    public TicketOrder getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @DeleteMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }
}
