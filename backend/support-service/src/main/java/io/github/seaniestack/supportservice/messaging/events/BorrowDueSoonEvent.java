package io.github.seaniestack.supportservice.messaging.events;

import java.time.LocalDateTime;

public record BorrowDueSoonEvent(
        Long borrowId,
        Long userId,
        Long bookId,
        LocalDateTime deadline
) {
}
