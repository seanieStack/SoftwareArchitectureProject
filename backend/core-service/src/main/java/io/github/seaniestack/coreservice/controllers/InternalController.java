package io.github.seaniestack.coreservice.controllers;

import io.github.seaniestack.coreservice.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoints called by other services (not through the gateway).
 */
@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final BookService bookService;

    @GetMapping("/books/{bookId}/exists")
    public ResponseEntity<Void> bookExists(@PathVariable Long bookId) {
        log.trace("GET /api/internal/books/{}/exists", bookId);
        bookService.checkAvailability(bookId); // throws ResourceNotFoundException if not found
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/books/{bookId}/borrow")
    public ResponseEntity<Void> borrowCopy(@PathVariable Long bookId) {
        log.trace("PATCH /api/internal/books/{}/borrow", bookId);
        bookService.decrementAvailableCopies(bookId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/books/{bookId}/return")
    public ResponseEntity<Void> returnCopy(@PathVariable Long bookId) {
        log.trace("PATCH /api/internal/books/{}/return", bookId);
        bookService.incrementAvailableCopies(bookId);
        return ResponseEntity.noContent().build();
    }
}
