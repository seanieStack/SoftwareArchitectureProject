package io.github.seaniestack.supportservice.dtos;

import io.github.seaniestack.supportservice.entities.Fine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FineDTO(
        Long id,
        Long borrowId,
        Long userId,
        BigDecimal amount,
        LocalDateTime issuedAt,
        boolean paid,
        LocalDateTime paidAt,
        boolean acknowledged
) {
    public static FineDTO from(Fine fine) {
        return new FineDTO(
                fine.getId(),
                fine.getBorrowId(),
                fine.getUserId(),
                fine.getAmount(),
                fine.getIssuedAt(),
                fine.isPaid(),
                fine.getPaidAt(),
                fine.isAcknowledged()
        );
    }
}