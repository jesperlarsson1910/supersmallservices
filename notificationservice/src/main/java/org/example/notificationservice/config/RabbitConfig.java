package org.example.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME    = "notification.queue";
    public static final String EXCHANGE_NAME = "ticket.exchange";

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(notificationQueue).to(ticketExchange).with("ticket.purchased");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
