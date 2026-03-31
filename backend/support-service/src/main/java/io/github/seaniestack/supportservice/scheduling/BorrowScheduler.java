package io.github.seaniestack.supportservice.scheduling;

import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.messaging.EventPublisher;
import io.github.seaniestack.supportservice.messaging.events.BorrowDueSoonEvent;
import io.github.seaniestack.supportservice.services.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class BorrowScheduler {

    private final BorrowService borrowService;
    private final EventPublisher eventPublisher;

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyDueSoon() {
        log.info("Checking for borrows due soon");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        List<Borrow> dueSoon = borrowService.getBorrowsDueBetween(now, tomorrow);
        log.info("Found {} borrows due within the next day", dueSoon.size());

        for (Borrow borrow : dueSoon) {
            eventPublisher.publishBorrowDueSoon(new BorrowDueSoonEvent(
                    borrow.getId(), borrow.getUserId(), borrow.getBookId(), borrow.getDeadline()));
        }
    }
}
