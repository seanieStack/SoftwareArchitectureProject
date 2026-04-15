package io.github.seaniestack.supportservice.admin;

import io.github.seaniestack.supportservice.admin.dto.AnalyticsDTO;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import io.github.seaniestack.supportservice.repositories.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final BorrowRepository borrowRepository;
    private final FineRepository fineRepository;

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDTO> getAnalytics() {
        long totalBorrows = borrowRepository.count();
        long activeBorrows = borrowRepository.countByStatus(BorrowStatus.BORROWED);
        long overdueBorrows = borrowRepository.countByStatus(BorrowStatus.OVERDUE);
        long totalFinesCollected = fineRepository.countByPaidTrue();
        long unpaidFines = fineRepository.countByPaidFalse();
        return ResponseEntity.ok(new AnalyticsDTO(totalBorrows, activeBorrows, overdueBorrows, totalFinesCollected, unpaidFines));
    }
}
