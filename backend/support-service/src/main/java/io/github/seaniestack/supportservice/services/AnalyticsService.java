package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.dtos.AnalyticsDTO;
import io.github.seaniestack.supportservice.dtos.LoanCount;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import io.github.seaniestack.supportservice.repositories.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final BorrowRepository borrowRepository;
    private final FineRepository fineRepository;

    public AnalyticsDTO getAnalytics() {
        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setTotalBorrows(borrowRepository.count());
        dto.setActiveBorrows(borrowRepository.countByStatus(BorrowStatus.BORROWED));
        dto.setOverdueBorrows(borrowRepository.countByStatus(BorrowStatus.OVERDUE));
        dto.setTotalFinesCollected(fineRepository.countByPaidTrue());
        dto.setUnpaidFines(fineRepository.countByPaidFalse());
        return dto;
    }

    public LoanCount getActiveLoansCount() {
        long borrowed = borrowRepository.countByStatus(BorrowStatus.BORROWED);
        long overdue = borrowRepository.countByStatus(BorrowStatus.OVERDUE);
        return new LoanCount(borrowed + overdue);
    }
}
