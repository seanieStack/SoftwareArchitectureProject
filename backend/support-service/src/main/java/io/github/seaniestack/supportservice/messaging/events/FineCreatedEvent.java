package io.github.seaniestack.supportservice.messaging.events;

import java.math.BigDecimal;

public record FineCreatedEvent(
        Long fineId,
        Long borrowId,
        Long userId,
        BigDecimal amount
) {
}
