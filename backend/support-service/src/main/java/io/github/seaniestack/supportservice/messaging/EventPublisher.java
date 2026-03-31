package io.github.seaniestack.supportservice.messaging;

import io.github.seaniestack.supportservice.messaging.events.BorrowCreatedEvent;
import io.github.seaniestack.supportservice.messaging.events.BorrowDueSoonEvent;
import io.github.seaniestack.supportservice.messaging.events.FineCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static io.github.seaniestack.supportservice.messaging.RabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBorrowCreated(BorrowCreatedEvent event) {
        log.info("Publishing borrow.created event for borrow {}", event.borrowId());
        rabbitTemplate.convertAndSend(EXCHANGE, BORROW_CREATED_KEY, event);
    }

    public void publishBorrowDueSoon(BorrowDueSoonEvent event) {
        log.info("Publishing borrow.due-soon event for borrow {}", event.borrowId());
        rabbitTemplate.convertAndSend(EXCHANGE, BORROW_DUE_SOON_KEY, event);
    }

    public void publishFineCreated(FineCreatedEvent event) {
        log.info("Publishing fine.created event for fine {}", event.fineId());
        rabbitTemplate.convertAndSend(EXCHANGE, FINE_CREATED_KEY, event);
    }
}
