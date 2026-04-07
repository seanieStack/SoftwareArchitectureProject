package io.github.seaniestack.supportservice.dtos;

import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;

import java.time.LocalDateTime;

public record BorrowDTO(
        Long id,
        Long userId,
        Long bookId,
        BorrowStatus status,
        LocalDateTime borrowedAt,
        LocalDateTime deadline,
        LocalDateTime returnedAt
) {
    public static BorrowDTO from(Borrow borrow) {
        return new BorrowDTO(
                borrow.getId(),
                borrow.getUserId(),
                borrow.getBookId(),
                borrow.getStatus(),
                borrow.getBorrowedAt(),
                borrow.getDeadline(),
                borrow.getReturnedAt()
        );
    }
}
