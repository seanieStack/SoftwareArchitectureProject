package io.github.seaniestack.supportservice.dtos;

import java.time.LocalDateTime;

public record BorrowRequest(
        Long userId,
        Long bookId,
        LocalDateTime deadline
) {
}
