package org.example.orderservice.service;

import org.example.event.SeatsReservationFailedEvent;
import org.example.event.SeatsReservedEvent;
import org.example.event.SeatHoldExpiredEvent;
import org.example.orderservice.model.TicketOrder;
import org.example.orderservice.repository.TicketOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RabbitListener(queues = "order.results.queue")
public class OrderSagaListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderSagaListener.class);
    private final TicketOrderRepository orderRepository;

    public OrderSagaListener(TicketOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
                    logger.info("Order {} cancelled (seats unavailable)", event.orderId());
                },
                () -> logger.error("Order {} not found during cancellation", event.orderId())
        );
    }

    @RabbitHandler
    @Transactional
    public void handleSeatHoldExpired(SeatHoldExpiredEvent event) {
        logger.warn("Seat hold expired for order {}, seat {}", event.orderId(), event.seatId());
        orderRepository.findById(event.orderId()).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == TicketOrder.OrderStatus.PENDING) {
                        order.setStatus(TicketOrder.OrderStatus.CANCELLED_EXPIRED);
                        orderRepository.save(order);
                        logger.info("Order {} cancelled (hold expired)", event.orderId());
                    }
                },
                () -> logger.error("Order {} not found during hold-expiry cancellation", event.orderId())
        );
    }

    @RabbitHandler(isDefault = true)
    public void handleUnknown(Object object) {
        logger.warn("Received unknown message type: {}", object.getClass().getName());
    }
}
