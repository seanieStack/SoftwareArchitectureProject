package io.github.seaniestack.supportservice.messaging;

import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.entities.NotificationType;
import io.github.seaniestack.supportservice.messaging.events.BookRemovedEvent;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import io.github.seaniestack.supportservice.services.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.seaniestack.supportservice.messaging.RabbitMQConfig.BOOK_REMOVED_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreEventListener {

    private final BorrowRepository borrowRepository;
    private final NotificationService notificationService;

    @RabbitListener(queues = BOOK_REMOVED_QUEUE)
    @Transactional
    public void handleBookRemoved(BookRemovedEvent event) {
        log.info("Received book.removed event for book {} ({})", event.bookId(), event.isbn());

        List<Borrow> activeBorrows = borrowRepository.findByBookIdAndStatusIn(
                event.bookId(), List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE));

        if (activeBorrows.isEmpty()) {
            log.debug("No active borrows found for removed book {}", event.bookId());
            return;
        }

        // The core service prevents deletion while copies are borrowed, but a race condition
        // between the availability check and the delete could leave active borrow records
        // referencing a book that no longer exists. Cancel them here.
        for (Borrow borrow : activeBorrows) {
            borrow.setStatus(BorrowStatus.CANCELLED);
            borrowRepository.save(borrow);
            log.warn("Cancelled borrow {} for user {} — book {} was removed", borrow.getId(), borrow.getUserId(), event.bookId());

            notificationService.createNotification(
                    borrow.getUserId(),
                    NotificationType.BORROW_CANCELLED,
                    "Your borrow of \"%s\" has been cancelled because the book was removed from the catalogue.".formatted(event.title())
            );
        }

        log.info("Cancelled {} active borrow(s) for removed book {}", activeBorrows.size(), event.bookId());
    }
}
