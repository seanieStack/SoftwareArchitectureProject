package io.github.seaniestack.supportservice.controllers;

import io.github.seaniestack.supportservice.dtos.AnalyticsDTO;
import io.github.seaniestack.supportservice.dtos.LoanCount;
import io.github.seaniestack.supportservice.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {
    private final AnalyticsService analyticsService;

    @GetMapping("/api/admin/analytics")
    public ResponseEntity<AnalyticsDTO> getAnalytics() {
        log.trace("GET /api/admin/analytics");
        AnalyticsDTO analytics = analyticsService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /** Internal endpoint called by core-service (not through gateway). */
    @GetMapping("/api/internal/activeLoans")
    public ResponseEntity<LoanCount> getActiveLoans() {
        log.trace("GET /api/internal/activeLoans");
        LoanCount count = analyticsService.getActiveLoansCount();
        return ResponseEntity.ok(count);
    }
}
