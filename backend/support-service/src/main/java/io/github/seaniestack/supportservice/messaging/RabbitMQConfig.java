package io.github.seaniestack.supportservice.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CORE_EVENTS_EXCHANGE = "core.events";
    public static final String BOOK_REMOVED_ROUTING_KEY = "book.removed";
    public static final String BOOK_REMOVED_QUEUE = "support.book.removed";

    @Bean
    public TopicExchange coreEventsExchange() {
        return new TopicExchange(CORE_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue bookRemovedQueue() {
        return QueueBuilder.durable(BOOK_REMOVED_QUEUE).build();
    }

    @Bean
    public Binding bookRemovedBinding(Queue bookRemovedQueue, TopicExchange coreEventsExchange) {
        return BindingBuilder.bind(bookRemovedQueue).to(coreEventsExchange).with(BOOK_REMOVED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
