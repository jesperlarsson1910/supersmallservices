package org.example.inventoryservice.controller;

import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/events/{eventId}/seats")
    public List<Seat> getAvailableSeats(@PathVariable Long eventId) {
        return inventoryService.getAvailableSeats(eventId);
    }

    @PostMapping("/seats/{seatId}/confirm")
    public void confirmSeat(@PathVariable Long seatId) {
        inventoryService.confirmSeat(seatId);
    }

    @PostMapping("/seats/{seatId}/release")
    public void releaseSeat(@PathVariable Long seatId) {
        inventoryService.releaseSeat(seatId);
    }
}
