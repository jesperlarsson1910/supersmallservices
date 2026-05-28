package org.example.inventoryservice.service;

import org.example.event.TicketOrderPlacedEvent;
import org.example.inventoryservice.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class InventoryListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryListener.class);
    private final InventoryService inventoryService;

    public InventoryListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleTicketOrderPlaced(
            TicketOrderPlacedEvent event,
            @Header(required = false, name = "x-delivery-attempt") Integer deliveryAttempt,
            @Header(required = false, name = "X-Sender-App") String senderApp) {

        if (deliveryAttempt != null && deliveryAttempt > 1) {
            logger.info("Retry detected — delivery attempt {}", deliveryAttempt);
        }

        logger.info("Received TicketOrderPlacedEvent from {}: orderId={}, seatId={}, qty={}",
                senderApp, event.orderId(), event.seatId(), event.quantity());

        inventoryService.processOrder(event);
    }
}
