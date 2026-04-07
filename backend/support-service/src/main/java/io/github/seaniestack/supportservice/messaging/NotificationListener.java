package io.github.seaniestack.supportservice.messaging;

import io.github.seaniestack.supportservice.entities.NotificationType;
import io.github.seaniestack.supportservice.messaging.events.BorrowCreatedEvent;
import io.github.seaniestack.supportservice.messaging.events.BorrowDueSoonEvent;
import io.github.seaniestack.supportservice.messaging.events.FineCreatedEvent;
import io.github.seaniestack.supportservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static io.github.seaniestack.supportservice.messaging.RabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = BORROW_CREATED_QUEUE)
    public void handleBorrowCreated(BorrowCreatedEvent event) {
        log.info("Received borrow.created event for borrow {}", event.borrowId());
        notificationService.createNotification(
                event.userId(),
                NotificationType.BORROW_CREATED,
                "You borrowed book %d. Due by %s.".formatted(event.bookId(), event.deadline().toLocalDate())
        );
    }

    @RabbitListener(queues = BORROW_DUE_SOON_QUEUE)
    public void handleBorrowDueSoon(BorrowDueSoonEvent event) {
        log.info("Received borrow.due-soon event for borrow {}", event.borrowId());
        notificationService.createNotification(
                event.userId(),
                NotificationType.BORROW_DUE_SOON,
                "Reminder: book %d is due tomorrow (%s).".formatted(event.bookId(), event.deadline().toLocalDate())
        );
    }

    @RabbitListener(queues = FINE_CREATED_QUEUE)
    public void handleFineCreated(FineCreatedEvent event) {
        log.info("Received fine.created event for fine {}", event.fineId());
        notificationService.createNotification(
                event.userId(),
                NotificationType.FINE_CREATED,
                "You have a new fine of €%.2f for borrow %d.".formatted(event.amount(), event.borrowId())
        );
    }
}
