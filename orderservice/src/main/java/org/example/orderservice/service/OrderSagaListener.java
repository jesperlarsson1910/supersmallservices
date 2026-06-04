package org.example.orderservice.service;

import org.example.event.SeatsReservationFailedEvent;
import org.example.event.SeatsReservedEvent;
import org.example.event.SeatHoldExpiredEvent;
import org.example.event.TicketPurchasedEvent;
import org.example.grpc.UserResponse;
import org.example.grpc.client.UserGrpcClient;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.TicketOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RabbitListener(queues = "order.results.queue")
public class OrderSagaListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderSagaListener.class);

    private final TicketOrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserGrpcClient userGrpcClient;

    public OrderSagaListener(TicketOrderRepository orderRepository,
                             RabbitTemplate rabbitTemplate,
                             UserGrpcClient userGrpcClient) {
        this.orderRepository  = orderRepository;
        this.rabbitTemplate   = rabbitTemplate;
        this.userGrpcClient   = userGrpcClient;
    }

    @RabbitHandler
    @Transactional
    public void handleSeatsReserved(SeatsReservedEvent event) {
        logger.info("Seats reserved for order {}", event.orderId());

        orderRepository.findById(event.orderId()).ifPresentOrElse(
                order -> {
                    order.setStatus(TicketOrder.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    logger.info("Order {} confirmed", event.orderId());

                    // Fetch user details via gRPC
                    UserResponse user = userGrpcClient.getUserById(order.getUserId());
                    String userName = user.getFound() ? user.getName() : "Customer";
                    String userEmail = user.getFound() ? user.getEmail() : null;

                    logger.info("Fetched user details for order {}: name={}", order.getId(), userName);

                    TicketPurchasedEvent purchased = new TicketPurchasedEvent(
                            UUID.randomUUID(),
                            order.getId(),
                            order.getUserId(),
                            order.getTicketEventId(),
                            userName,
                            userEmail,
                            order.getQuantity(),
                            order.getTotalPrice()
                    );

                    try {
                        rabbitTemplate.convertAndSend("ticket.exchange", "ticket.purchased", purchased);
                        logger.info("Published TicketPurchasedEvent for order {}", order.getId());
                    } catch (Exception e) {
                        logger.error("Failed to publish TicketPurchasedEvent: {}", e.getMessage());
                    }
                },
                () -> logger.error("Order {} not found during confirmation", event.orderId())
        );
    }

    @RabbitHandler
    @Transactional
    public void handleSeatsReservationFailed(SeatsReservationFailedEvent event) {
        logger.warn("Seat reservation failed for order {}. Reason: {}", event.orderId(), event.reason());
        orderRepository.findById(event.orderId()).ifPresentOrElse(
                order -> {
                    order.setStatus(TicketOrder.OrderStatus.CANCELLED_SEATS_UNAVAILABLE);
                    orderRepository.save(order);
                },
                () -> logger.error("Order {} not found during cancellation", event.orderId())
        );
    }

    @RabbitHandler
    @Transactional
    public void handleSeatHoldExpired(SeatHoldExpiredEvent event) {
        logger.warn("Seat hold expired for order {}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == TicketOrder.OrderStatus.PENDING) {
                        order.setStatus(TicketOrder.OrderStatus.CANCELLED_EXPIRED);
                        orderRepository.save(order);
                    }
                },
                () -> logger.error("Order {} not found during hold-expiry", event.orderId())
        );
    }

    @RabbitHandler(isDefault = true)
    public void handleUnknown(Object object) {
        logger.warn("Received unknown message type: {}", object.getClass().getName());
    }
}
