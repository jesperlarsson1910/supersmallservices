package org.example.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Outbound: order-service publishes here
    public static final String EXCHANGE_NAME = "ticket.exchange";

    // Inbound: order-service listens for saga results + hold-expiry
    public static final String ORDER_RESULTS_QUEUE = "order.results.queue";
    public static final String ORDER_RESULTS_EXCHANGE = "inventory.exchange";

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(ORDER_RESULTS_EXCHANGE);
    }

    @Bean
    public Queue orderResultsQueue() {
        return QueueBuilder.durable(ORDER_RESULTS_QUEUE).build();
    }

    // Bind to seat reserved, seat reservation failed, and hold expired events
    @Bean
    public Binding seatsReservedBinding(Queue orderResultsQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(orderResultsQueue).to(inventoryExchange).with("seats.reserved");
    }

    @Bean
    public Binding seatsFailedBinding(Queue orderResultsQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(orderResultsQueue).to(inventoryExchange).with("seats.reservation.failed");
    }

    @Bean
    public Binding holdExpiredBinding(Queue orderResultsQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(orderResultsQueue).to(inventoryExchange).with("seat.hold.expired");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
