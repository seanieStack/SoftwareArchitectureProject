package io.github.seaniestack.coreservice.messaging;

import io.github.seaniestack.coreservice.messaging.events.BookRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookRemoved(BookRemovedEvent event) {
        log.info("Publishing book.removed event for book {}", event.bookId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CORE_EVENTS_EXCHANGE,
                RabbitMQConfig.BOOK_REMOVED_ROUTING_KEY,
                event
        );
    }
}
