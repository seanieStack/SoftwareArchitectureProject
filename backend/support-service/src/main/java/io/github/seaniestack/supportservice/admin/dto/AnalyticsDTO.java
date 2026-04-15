package io.github.seaniestack.supportservice.admin.dto;

public record AnalyticsDTO(
        long totalBorrows,
        long activeBorrows,
        long overdueBorrows,
        long totalFinesCollected,
        long unpaidFines
) {
}
