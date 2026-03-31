package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByDeadlineBeforeAndStatus(LocalDateTime now, BorrowStatus status);

    List<Borrow> findByStatus(BorrowStatus status);

    List<Borrow> findByUserId(Long userId);

    boolean existsByBookIdAndStatusIn(Long bookId, List<BorrowStatus> statuses);

    List<Borrow> findByDeadlineBetweenAndStatus(LocalDateTime from, LocalDateTime to, BorrowStatus status);
}
