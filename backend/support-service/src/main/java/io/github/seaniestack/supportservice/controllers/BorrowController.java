package io.github.seaniestack.supportservice.controllers;

import io.github.seaniestack.supportservice.dtos.BorrowDTO;
import io.github.seaniestack.supportservice.dtos.BorrowRequest;
import io.github.seaniestack.supportservice.services.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowDTO>> getBorrowsForUser(@PathVariable Long userId) {
        log.trace("GET /api/borrows/user/{}", userId);
        List<BorrowDTO> borrows = borrowService.getBorrowsForUser(userId);
        log.debug("Returning {} borrows for user {}", borrows.size(), userId);
        return ResponseEntity.ok(borrows);
    }

    @GetMapping("/{borrowId}")
    public ResponseEntity<BorrowDTO> getBorrow(@PathVariable Long borrowId) {
        log.trace("GET /api/borrows/{}", borrowId);
        BorrowDTO borrow = borrowService.getBorrowById(borrowId);
        return ResponseEntity.ok(borrow);
    }

    @PostMapping
    public ResponseEntity<BorrowDTO> createBorrow(@RequestBody BorrowRequest request) {
        log.trace("POST /api/borrows userId={} bookId={}", request.userId(), request.bookId());
        BorrowDTO borrow = borrowService.createBorrow(request);
        log.info("Borrow {} created", borrow.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(borrow);
    }

    @PatchMapping("/{borrowId}/return")
    public ResponseEntity<BorrowDTO> returnBorrow(@PathVariable Long borrowId) {
        log.trace("PATCH /api/borrows/{}/return", borrowId);
        BorrowDTO borrow = borrowService.returnBorrow(borrowId);
        log.info("Borrow {} returned", borrowId);
        return ResponseEntity.ok(borrow);
    }
}
