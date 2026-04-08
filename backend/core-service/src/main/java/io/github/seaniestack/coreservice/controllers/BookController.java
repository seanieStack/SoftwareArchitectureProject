package io.github.seaniestack.coreservice.controllers;

import io.github.seaniestack.coreservice.dtos.BookAvailabilityDTO;
import io.github.seaniestack.coreservice.dtos.BookDTO;
import io.github.seaniestack.coreservice.dtos.BookRequest;
import io.github.seaniestack.coreservice.services.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookDTO>> getBooks() {
        log.trace("GET /api/books");
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category
    ) {
        log.trace("GET /api/books/search keyword={} author={} category={}", keyword, author, category);
        return ResponseEntity.ok(bookService.searchBooks(keyword, author, category));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long bookId) {
        log.trace("GET /api/books/{}", bookId);
        BookDTO book = bookService.getBookById(bookId);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/{bookId}/availability")
    public ResponseEntity<BookAvailabilityDTO> checkAvailability(@PathVariable Long bookId) {
        log.trace("GET /api/books/{}/availability", bookId);
        return ResponseEntity.ok(bookService.getBookAvailability(bookId));
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookRequest request) {
        log.trace("POST /api/books isbn={}", request == null ? null : request.isbn());
        BookDTO created = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long bookId, @Valid @RequestBody BookRequest request) {
        log.trace("PUT /api/books/{}", bookId);
        BookDTO updated = bookService.updateBook(bookId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        log.trace("DELETE /api/books/{}", bookId);
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
