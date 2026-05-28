package org.example.notificationservice.service;

import org.example.notificationservice.model.TicketPurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPurchaseConfirmation(TicketPurchasedEvent event, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@ticketservice.com");
        message.setTo(toEmail);
        message.setSubject("Your ticket order #" + event.orderId() + " is confirmed!");
        message.setText(buildEmailBody(event));

        mailSender.send(message);
        logger.info("Confirmation email sent for order {} to {}", event.orderId(), toEmail);
    }

    private String buildEmailBody(TicketPurchasedEvent event) {
        return """
            Hi there,

            Your ticket purchase is confirmed!

            Order ID:    %d
            Quantity:    %d ticket(s)
            Total Price: %s SEK

            Thank you for your purchase.
            The Ticket Service Team
            """.formatted(
                event.orderId(),
                event.quantity(),
                event.totalPrice()
            );
    }
}
