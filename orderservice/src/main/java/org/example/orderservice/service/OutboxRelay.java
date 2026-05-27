package org.example.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.event.TicketOrderPlacedEvent;
import org.example.orderservice.config.RabbitConfig;
import org.example.orderservice.controller.ChaosContext;
import org.example.orderservice.controller.ChaosScenario;
import org.example.orderservice.model.OutboxEvent;
import org.example.orderservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxRelay {

    private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ChaosContext chaosContext;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxRepository outboxRepository,
                       RabbitTemplate rabbitTemplate,
                       ChaosContext chaosContext,
                       ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.chaosContext = chaosContext;
        this.objectMapper = objectMapper;

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack && correlationData != null) {
                Long id = Long.valueOf(correlationData.getId());
                updateStatus(id, OutboxEvent.OutboxStatus.PROCESSED);
                logger.info("Event {} acked by broker", id);
            } else if (correlationData != null) {
                logger.error("Event {} failed to publish: {}", correlationData.getId(), cause);
            }
        });
    }

    private void updateStatus(Long id, OutboxEvent.OutboxStatus status) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(status);
            outboxRepository.save(event);
        });
    }

    @Scheduled(fixedDelay = 5000)
    public void relayEvents() {
        List<OutboxEvent> pending = outboxRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            try {
                ChaosScenario scenario = chaosContext.getCurrentScenario();
                CorrelationData correlationData = new CorrelationData(event.getId().toString());

                Object payload;
                if (scenario == ChaosScenario.DATA_CORRUPTION) {
                    payload = "{\"corrupted\": true, \"quantity\": -99}";
                    logger.warn("Chaos: corrupting payload for event {}", event.getEventId());
                } else {
                    payload = objectMapper.readValue(event.getPayload(), TicketOrderPlacedEvent.class);
                }

                logger.info("Relaying outbox event {} (order {})", event.getEventId(), event.getAggregateId());

                rabbitTemplate.convertAndSend(
                        RabbitConfig.EXCHANGE_NAME,
                        "ticket.order.placed",
                        payload,
                        msg -> {
                            msg.getMessageProperties().setHeader("X-Sender-App", "OrderService");
                            return msg;
                        },
                        correlationData
                );

                if (scenario == ChaosScenario.DUPLICATE_MESSAGE) {
                    logger.warn("Chaos: sending duplicate for event {}", event.getEventId());
                    rabbitTemplate.convertAndSend(
                            RabbitConfig.EXCHANGE_NAME,
                            "ticket.order.placed",
                            payload,
                            msg -> {
                                msg.getMessageProperties().setHeader("X-Sender-App", "OrderService");
                                return msg;
                            },
                            correlationData
                    );
                }

            } catch (Exception e) {
                logger.error("Error relaying event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
