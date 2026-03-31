package io.github.seaniestack.supportservice.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "support.events";

    public static final String BORROW_CREATED_KEY = "borrow.created";
    public static final String BORROW_DUE_SOON_KEY = "borrow.due-soon";
    public static final String FINE_CREATED_KEY = "fine.created";

    public static final String BORROW_CREATED_QUEUE = "notifications.borrow.created";
    public static final String BORROW_DUE_SOON_QUEUE = "notifications.borrow.due-soon";
    public static final String FINE_CREATED_QUEUE = "notifications.fine.created";

    @Bean
    public TopicExchange supportExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue borrowCreatedQueue() {
        return QueueBuilder.durable(BORROW_CREATED_QUEUE).build();
    }

    @Bean
    public Queue borrowDueSoonQueue() {
        return QueueBuilder.durable(BORROW_DUE_SOON_QUEUE).build();
    }

    @Bean
    public Queue fineCreatedQueue() {
        return QueueBuilder.durable(FINE_CREATED_QUEUE).build();
    }

    @Bean
    public Binding borrowCreatedBinding(Queue borrowCreatedQueue, TopicExchange supportExchange) {
        return BindingBuilder.bind(borrowCreatedQueue).to(supportExchange).with(BORROW_CREATED_KEY);
    }

    @Bean
    public Binding borrowDueSoonBinding(Queue borrowDueSoonQueue, TopicExchange supportExchange) {
        return BindingBuilder.bind(borrowDueSoonQueue).to(supportExchange).with(BORROW_DUE_SOON_KEY);
    }

    @Bean
    public Binding fineCreatedBinding(Queue fineCreatedQueue, TopicExchange supportExchange) {
        return BindingBuilder.bind(fineCreatedQueue).to(supportExchange).with(FINE_CREATED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
