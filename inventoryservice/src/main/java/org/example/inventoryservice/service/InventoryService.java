package org.example.inventoryservice.service;

import org.example.inventoryservice.controller.ChaosContext;
import org.example.inventoryservice.controller.ChaosScenario;
import org.example.event.TicketOrderPlacedEvent;
import org.example.event.SeatsReservationFailedEvent;
import org.example.event.SeatsReservedEvent;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.repository.ProcessedEventRepository;
import org.example.inventoryservice.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final SeatRepository stockRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ChaosContext chaosContext;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public InventoryService(SeatRepository stockRepository, ProcessedEventRepository processedEventRepository, ChaosContext chaosContext, org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.stockRepository = stockRepository;
        this.processedEventRepository = processedEventRepository;
        this.chaosContext = chaosContext;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void processOrder(TicketOrderPlacedEvent event) {
        logger.info("Processing event: {}", event);

        // 1. Check Idempotency
        if (processedEventRepository.existsById(event.eventId())) {
            logger.info("Event {} already processed. Skipping to ensure idempotency.", event.eventId());
            return;
        }

        // 2. Handle Chaos Scenarios
        handleChaos(event);

        // 3. Business Logic
        Seat stock = stockRepository.findById(event.product())
                .orElse(new Seat(event.product(), 100)); // Default 100 if not exists
        
        if (event.quantity() < 0) {
            throw new IllegalArgumentException("Chaos: Negative quantity detected for event " + event.eventId());
        }

        if (stock.getQuantity() < event.quantity()) {
            logger.warn("Insufficient stock for product {}. Required: {}, Available: {}", event.product(), event.quantity(), stock.getQuantity());
            
            // Compensating Action: Publish Failure Event
            SeatsReservationFailedEvent failedEvent = new SeatsReservationFailedEvent(
                java.util.UUID.randomUUID(),
                event.orderId(),
                "OUT_OF_STOCK"
            );
            
            rabbitTemplate.convertAndSend(
                org.example.inventoryservice.config.RabbitConfig.STOCK_FAILED_EXCHANGE,
                "stock.reservation.failed",
                failedEvent
            );
            
            // Still mark the event as processed to avoid retrying a known failure
            processedEventRepository.save(new org.example.inventoryservice.model.ProcessedEvent(event.eventId()));
            return;
        }

        stock.setQuantity(stock.getQuantity() - event.quantity());
        stockRepository.save(stock);

        // Success: Publish StockReservedEvent
        SeatsReservedEvent reservedEvent = new SeatsReservedEvent(
            java.util.UUID.randomUUID(),
            event.orderId()
        );

        rabbitTemplate.convertAndSend(
            org.example.inventoryservice.config.RabbitConfig.STOCK_FAILED_EXCHANGE,
            "stock.reserved",
            reservedEvent
        );

        // 4. Mark as processed
        processedEventRepository.save(new org.example.inventoryservice.model.ProcessedEvent(event.eventId()));
        logger.info("Successfully processed event {}", event.eventId());
    }

    private void handleChaos(TicketOrderPlacedEvent event) {
        ChaosScenario scenario = chaosContext.getCurrentScenario();
        
        if (scenario == ChaosScenario.TRANSIENT_FAILURE) {
            int attempt = chaosContext.incrementAndGetAttempt(event.eventId());
            if (attempt <= 2) {
                logger.warn("Chaos: Simulating transient failure (attempt {}) for event {}", attempt, event.eventId());
                throw new RuntimeException("Chaos: Transient failure");
            }
        }
    }
}
