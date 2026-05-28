package org.example.notificationservice.service;

import org.example.notificationservice.config.RabbitConfig;
import org.example.notificationservice.model.TicketPurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    private final EmailNotificationService emailService;

    public NotificationListener(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleTicketPurchased(TicketPurchasedEvent event) {
        logger.info("Received TicketPurchasedEvent for order {}, user {}",
            event.orderId(), event.userId());

        // In a real system userId would be used to look up the user's email
        // from authservice/userservice. For now we derive a placeholder.
        String toEmail = "user-" + event.userId() + "@example.com";

        try {
            emailService.sendPurchaseConfirmation(event, toEmail);
        } catch (Exception e) {
            // Log but don't rethrow — a failed email should not requeue the message
            logger.error("Failed to send email for order {}: {}", event.orderId(), e.getMessage());
        }
    }
}
