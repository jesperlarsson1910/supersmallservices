package org.example.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bff.service.ProxyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final ProxyService proxyService;
    private final RestClient eventClient;
    private final RestClient inventoryClient;
    private final RestClient orderClient;

    public GatewayController(
            ProxyService proxyService,
            @Value("${services.event-url}")     String eventUrl,
            @Value("${services.inventory-url}") String inventoryUrl,
            @Value("${services.order-url}")     String orderUrl) {

        this.proxyService    = proxyService;
        this.eventClient     = RestClient.create(eventUrl);
        this.inventoryClient = RestClient.create(inventoryUrl);
        this.orderClient     = RestClient.create(orderUrl);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String userId(HttpServletRequest req)   { return (String) req.getAttribute("userId"); }
    private String username(HttpServletRequest req) { return (String) req.getAttribute("username"); }
    private String role(HttpServletRequest req)     { return (String) req.getAttribute("role"); }

    // ── Events ────────────────────────────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<String> getEvents(HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET,
                "/events", req.getQueryString(),
                userId(req), username(req), role(req), null);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<String> getEvent(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET,
                "/events/" + id, null,
                userId(req), username(req), role(req), null);
    }

    @PostMapping("/events")
    public ResponseEntity<String> createEvent(@RequestBody String body, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.POST,
                "/events", null,
                userId(req), username(req), role(req), body);
    }

    @PatchMapping("/events/{id}/status")
    public ResponseEntity<String> updateEventStatus(@PathVariable Long id,
                                                    HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.PATCH,
                "/events/" + id + "/status", req.getQueryString(),
                userId(req), username(req), role(req), null);
    }

    // ── Venues ────────────────────────────────────────────────────────────────

    @GetMapping("/venues")
    public ResponseEntity<String> getVenues(HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.GET,
                "/venues", null,
                userId(req), username(req), role(req), null);
    }

    @PostMapping("/venues")
    public ResponseEntity<String> createVenue(@RequestBody String body, HttpServletRequest req) {
        return proxyService.forward(eventClient, HttpMethod.POST,
                "/venues", null,
                userId(req), username(req), role(req), body);
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    @GetMapping("/inventory/events/{eventId}/seats")
    public ResponseEntity<String> getSeats(@PathVariable Long eventId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.GET,
                "/inventory/events/" + eventId + "/seats", null,
                userId(req), username(req), role(req), null);
    }

    @PostMapping("/inventory/seats/{seatId}/confirm")
    public ResponseEntity<String> confirmSeat(@PathVariable Long seatId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.POST,
                "/inventory/seats/" + seatId + "/confirm", null,
                userId(req), username(req), role(req), null);
    }

    @PostMapping("/inventory/seats/{seatId}/release")
    public ResponseEntity<String> releaseSeat(@PathVariable Long seatId, HttpServletRequest req) {
        return proxyService.forward(inventoryClient, HttpMethod.POST,
                "/inventory/seats/" + seatId + "/release", null,
                userId(req), username(req), role(req), null);
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @PostMapping("/orders")
    public ResponseEntity<String> placeOrder(@RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        // Inject userId from JWT so frontend doesn't need to send it
        body.put("userId", Long.parseLong(userId(req)));
        return proxyService.forward(orderClient, HttpMethod.POST,
                "/orders", null,
                userId(req), username(req), role(req), body);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrder(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(orderClient, HttpMethod.GET,
                "/orders/" + id, null,
                userId(req), username(req), role(req), null);
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id, HttpServletRequest req) {
        return proxyService.forward(orderClient, HttpMethod.DELETE,
                "/orders/" + id + "/cancel", null,
                userId(req), username(req), role(req), null);
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
