package org.example.botservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME    = "bot.holdexpired.queue";
    public static final String EXCHANGE_NAME = "inventory.exchange";

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue botHoldExpiredQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding botHoldExpiredBinding(Queue botHoldExpiredQueue,
                                         TopicExchange inventoryExchange) {
        return BindingBuilder.bind(botHoldExpiredQueue)
            .to(inventoryExchange)
            .with("seat.hold.expired");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
