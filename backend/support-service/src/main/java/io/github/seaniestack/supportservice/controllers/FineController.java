package io.github.seaniestack.supportservice.controllers;

import io.github.seaniestack.supportservice.dtos.FineDTO;
import io.github.seaniestack.supportservice.services.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FineDTO>> getFines(@PathVariable Long userId, @RequestParam(required = false) Boolean acknowledged) {
        log.trace("GET /api/fines/user/{} acknowledged={}", userId, acknowledged);
        List<FineDTO> fines;
        if (Boolean.TRUE.equals(acknowledged)) {
            fines = fineService.getUnacknowledgedFines(userId);
        } else {
            fines = fineService.getFinesForUser(userId);
        }
        log.debug("Returning {} fines for user {}", fines.size(), userId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/{fineId}")
    public ResponseEntity<FineDTO> getFine(@PathVariable Long fineId) {
        log.trace("GET /api/fines/{}", fineId);
        FineDTO fine = fineService.getFineById(fineId);
        return ResponseEntity.ok(fine);
    }

    @PatchMapping("/{fineId}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long fineId) {
        log.trace("PATCH /api/fines/{}/acknowledge", fineId);
        fineService.acknowledgeFine(fineId);
        log.info("Fine {} acknowledged", fineId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{fineId}/pay")
    public ResponseEntity<Void> pay(@PathVariable Long fineId) {
        log.trace("PATCH /api/fines/{}/pay", fineId);
        fineService.payFine(fineId);
        log.info("Fine {} paid", fineId);
        return ResponseEntity.noContent().build();
    }
}
