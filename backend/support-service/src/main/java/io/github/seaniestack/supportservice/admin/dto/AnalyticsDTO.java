package io.github.seaniestack.supportservice.admin.dto;

public record AnalyticsDTO(
        long activeLoans,
        long returnedLoans,
        long overdueLoans,
        long outstandingFines
) {
}
