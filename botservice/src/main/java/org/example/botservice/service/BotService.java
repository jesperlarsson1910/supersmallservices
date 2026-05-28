package org.example.botservice.service;

import org.example.botservice.config.RabbitConfig;
import org.example.botservice.model.SeatHoldExpiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BotService {

    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final RestClient restClient;

    public BotService(@Value("${orderservice.url:http://orderservice:8081}") String orderServiceUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(orderServiceUrl)
            .build();
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleSeatHoldExpired(SeatHoldExpiredEvent event) {
        logger.warn("Bot: seat hold expired — seatId={} orderId={}",
            event.seatId(), event.orderId());

        if (event.orderId() == null) {
            logger.info("No orderId on expired hold for seat {}, nothing to cancel", event.seatId());
            return;
        }

        try {
            restClient.delete()
                .uri("/orders/{id}/cancel", event.orderId())
                .retrieve()
                .toBodilessEntity();

            logger.info("Bot: successfully cancelled order {}", event.orderId());

        } catch (Exception e) {
            // Log and swallow — inventoryservice already released the seat.
            // Orderservice will be left in PENDING which ops can clean up.
            logger.error("Bot: failed to cancel order {}: {}", event.orderId(), e.getMessage());
        }
    }
}
