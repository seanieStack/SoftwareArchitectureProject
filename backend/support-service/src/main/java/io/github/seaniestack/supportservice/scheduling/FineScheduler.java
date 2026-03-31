package io.github.seaniestack.supportservice.scheduling;

import io.github.seaniestack.supportservice.services.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class FineScheduler {

    private final FineService fineService;

    @Scheduled(cron = "0 0 0 * * *")
    public void processOverdueBorrows(){
        log.info("Processing overdue borrows");
        LocalDateTime now = LocalDateTime.now();

        fineService.processNewlyOverdue(now);
        fineService.updateAccruingFines(now);

        log.info("Processing overdue borrows done");
    }
}
