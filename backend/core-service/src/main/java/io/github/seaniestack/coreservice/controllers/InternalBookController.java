package io.github.seaniestack.coreservice.controllers;

import io.github.seaniestack.coreservice.entities.Book;
import io.github.seaniestack.coreservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/internal/books")
@RequiredArgsConstructor
public class InternalBookController {

    private final BookRepository bookRepository;

    @GetMapping("/{bookId}/exists")
    public ResponseEntity<Void> bookExists(@PathVariable Long bookId) {
        boolean exists = bookRepository.existsById(bookId);
        return exists
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{bookId}/borrow")
    @Transactional
    public ResponseEntity<Void> borrowCopy(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found. Id: " + bookId));
        book.borrowCopy();
        bookRepository.save(book);
        log.debug("Decremented available copies for book {}", bookId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{bookId}/return")
    @Transactional
    public ResponseEntity<Void> returnCopy(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found. Id: " + bookId));
        book.returnCopy();
        bookRepository.save(book);
        log.debug("Incremented available copies for book {}", bookId);
        return ResponseEntity.ok().build();
    }
}
