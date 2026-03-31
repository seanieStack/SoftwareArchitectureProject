package io.github.seaniestack.supportservice.messaging.events;

import java.time.LocalDateTime;

public record BorrowCreatedEvent(
        Long borrowId,
        Long userId,
        Long bookId,
        LocalDateTime deadline
) {
}
