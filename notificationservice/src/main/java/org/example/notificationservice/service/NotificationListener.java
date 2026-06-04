package org.example.notificationservice.service;

import org.example.grpc.UserResponse;
import org.example.grpc.client.UserGrpcClient;
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
    private final UserGrpcClient userGrpcClient;

    public NotificationListener(EmailNotificationService emailService,
                                UserGrpcClient userGrpcClient) {
        this.emailService    = emailService;
        this.userGrpcClient  = userGrpcClient;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleTicketPurchased(TicketPurchasedEvent event) {
        logger.info("Received TicketPurchasedEvent for order {}, user {}",
                event.orderId(), event.userId());

        // Fetch real email from userservice via gRPC
        UserResponse user = userGrpcClient.getUserById(event.userId());

        String toEmail = user.getFound()
                ? user.getEmail()
                : "user-" + event.userId() + "@example.com";

        String name = event.userName() != null ? event.userName() : "Customer";

        logger.info("Sending confirmation email for order {} to {}", event.orderId(), toEmail);

        try {
            emailService.sendPurchaseConfirmation(event, toEmail, name);
        } catch (Exception e) {
            logger.error("Failed to send email for order {}: {}", event.orderId(), e.getMessage());
        }
    }
}
