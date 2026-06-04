package org.example.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bff.service.ProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    private final ProxyService proxyService;
    private final RestClient eventClient;
    private final RestClient inventoryClient;
    private final RestClient orderClient;
    private final RestClient userClient;

    public GatewayController(
            ProxyService proxyService,
            @Value("${services.event-url}")     String eventUrl,
            @Value("${services.inventory-url}") String inventoryUrl,
            @Value("${services.order-url}")     String orderUrl,
            @Value("${services.user-url}")      String userUrl) {

        this.proxyService    = proxyService;
        this.eventClient     = RestClient.create(eventUrl);
        this.inventoryClient = RestClient.create(inventoryUrl);
        this.orderClient     = RestClient.create(orderUrl);
        this.userClient      = RestClient.create(userUrl);
    }

    private String userId(HttpServletRequest req)   { return (String) req.getAttribute("userId"); }
    private String username(HttpServletRequest req) { return (String) req.getAttribute("username"); }
    private String role(HttpServletRequest req)     { return (String) req.getAttribute("role"); }

    // ── Events ────────────────────────────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<String> getEvents(HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET, "/events",
                req.getQueryString(), userId(req), username(req), role(req), null);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<String> getEvent(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET, "/events/" + id,
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/events")
    public ResponseEntity<String> createEvent(@RequestBody String body, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.POST, "/events",
                null, userId(req), username(req), role(req), body);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<String> updateEvent(@PathVariable Long id,
                                              @RequestBody String body,
                                              HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.PUT, "/events/" + id,
                null, userId(req), username(req), role(req), body);
    }

    @PatchMapping("/events/{id}/status")
    public ResponseEntity<String> updateEventStatus(@PathVariable Long id,
                                                    HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.PATCH,
                "/events/" + id + "/status", req.getQueryString(),
                userId(req), username(req), role(req), null);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.DELETE, "/events/" + id,
                null, userId(req), username(req), role(req), null);
    }

    // ── Venues ────────────────────────────────────────────────────────────────

    @GetMapping("/venues")
    public ResponseEntity<String> getVenues(HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET, "/venues",
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/venues")
    public ResponseEntity<String> createVenue(@RequestBody String body, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.POST, "/venues",
                null, userId(req), username(req), role(req), body);
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    @GetMapping("/inventory/events/{eventId}/seats")
    public ResponseEntity<String> getSeats(@PathVariable Long eventId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.GET,
                "/inventory/events/" + eventId + "/seats",
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/inventory/seats/{seatId}/hold")
    public ResponseEntity<String> holdSeat(@PathVariable Long seatId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.POST,
                "/inventory/seats/" + seatId + "/hold",
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/inventory/seats/{seatId}/release")
    public ResponseEntity<String> releaseSeat(@PathVariable Long seatId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.POST,
                "/inventory/seats/" + seatId + "/release",
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/inventory/seats/{seatId}/confirm")
    public ResponseEntity<String> confirmSeat(@PathVariable Long seatId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.POST,
                "/inventory/seats/" + seatId + "/confirm",
                null, userId(req), username(req), role(req), null);
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @PostMapping("/orders")
    public ResponseEntity<String> placeOrder(@RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        body.put("userId", Long.parseLong(userId(req)));
        return proxyService.forward(orderClient, HttpMethod.POST, "/orders",
                null, userId(req), username(req), role(req), body);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrder(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(orderClient, HttpMethod.GET, "/orders/" + id,
                null, userId(req), username(req), role(req), null);
    }

    @GetMapping("/orders/my")
    public ResponseEntity<String> getMyOrders(HttpServletRequest req) {
        return proxyService.forward(orderClient, HttpMethod.GET,
                "/orders/user/" + userId(req),
                null, userId(req), username(req), role(req), null);
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(orderClient, HttpMethod.DELETE,
                "/orders/" + id + "/cancel",
                null, userId(req), username(req), role(req), null);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(HttpServletRequest req) {
        return proxyService.forward(userClient, HttpMethod.GET, "/users",
                null, userId(req), username(req), role(req), null);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(userClient, HttpMethod.GET, "/users/" + id,
                null, userId(req), username(req), role(req), null);
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody String body, HttpServletRequest req) {
        return proxyService.forward(userClient, HttpMethod.POST, "/users",
                null, userId(req), username(req), role(req), body);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id,
                                             @RequestBody String body,
                                             HttpServletRequest req) {
        return proxyService.forward(userClient, HttpMethod.PUT, "/users/" + id,
                null, userId(req), username(req), role(req), body);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(userClient, HttpMethod.DELETE, "/users/" + id,
                null, userId(req), username(req), role(req), null);
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
