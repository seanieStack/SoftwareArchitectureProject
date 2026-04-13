package io.github.seaniestack.coreservice.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CORE_EVENTS_EXCHANGE = "core.events";
    public static final String BOOK_REMOVED_ROUTING_KEY = "book.removed";

    @Bean
    public TopicExchange coreEventsExchange() {
        return new TopicExchange(CORE_EVENTS_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
