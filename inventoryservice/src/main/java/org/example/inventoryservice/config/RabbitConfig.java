package org.example.inventoryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Inbound: listens for ticket orders published by order-service
    public static final String QUEUE_NAME       = "inventory.orders.queue";
    public static final String DLQ_NAME         = "inventory.orders.dlq";
    public static final String TICKET_EXCHANGE  = "ticket.exchange";
    public static final String DLX_NAME         = "ticket.dlx";

    // Outbound: publishes seat results and hold-expiry events
    public static final String EXCHANGE_NAME    = "inventory.exchange";

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(TICKET_EXCHANGE);
    }

    @Bean
    public TopicExchange ticketDlx() {
        return new TopicExchange(DLX_NAME);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue inventoryOrdersQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", "inventory.orders.dlq")
                .build();
    }

    @Bean
    public Queue inventoryOrdersDlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding inventoryOrdersBinding(Queue inventoryOrdersQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(inventoryOrdersQueue).to(ticketExchange).with("ticket.order.placed");
    }

    @Bean
    public Binding dlqBinding(Queue inventoryOrdersDlq, TopicExchange ticketDlx) {
        return BindingBuilder.bind(inventoryOrdersDlq).to(ticketDlx).with("inventory.orders.dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
