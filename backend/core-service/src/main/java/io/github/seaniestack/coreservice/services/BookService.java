package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.dtos.BookAvailabilityDTO;
import io.github.seaniestack.coreservice.dtos.BookDTO;
import io.github.seaniestack.coreservice.dtos.BookRequest;
import io.github.seaniestack.coreservice.entities.Author;
import io.github.seaniestack.coreservice.entities.Book;
import io.github.seaniestack.coreservice.entities.Category;
import io.github.seaniestack.coreservice.exceptions.ConflictException;
import io.github.seaniestack.coreservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.coreservice.messaging.BookEventPublisher;
import io.github.seaniestack.coreservice.messaging.events.BookRemovedEvent;
import io.github.seaniestack.coreservice.repositories.AuthorRepository;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import io.github.seaniestack.coreservice.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookEventPublisher bookEventPublisher;

    public List<BookDTO> getAllBooks() {
        log.debug("Fetching all books");
        return bookRepository.findAll().stream().map(BookDTO::from).toList();
    }

    public List<BookDTO> searchBooks(String keyword, String author, String category) {
        return bookRepository.search(normalizeOrNull(keyword), normalizeOrNull(author), normalizeOrNull(category))
                .stream().map(BookDTO::from).toList();
    }

    public BookDTO getBookById(Long id) {
        log.debug("Fetching book {}", id);
        return BookDTO.from(getBookOrThrow(id));
    }

    public BookAvailabilityDTO getBookAvailability(Long bookId) {
        return BookAvailabilityDTO.from(getBookOrThrow(bookId));
    }

    @Transactional
    public BookDTO createBook(BookRequest request) {
        validateRequest(request);
        ensureIsbnUnique(request.isbn(), null);

        Book book = Book.builder()
                .title(request.title().trim())
                .isbn(request.isbn().trim())
                .authors(resolveAuthors(request.authors()))
                .categories(resolveCategories(request.categories()))
                .totalCopies(request.totalCopies())
                .availableCopies(request.availableCopies())
                .active(true)
                .build();
        book.publish();

        Book saved = bookRepository.save(book);
        log.info("Created book {} ({})", saved.getId(), saved.getIsbn());
        return BookDTO.from(saved);
    }

    @Transactional
    public BookDTO updateBook(Long id, BookRequest request) {
        validateRequest(request);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found. Id: " + id));

        ensureIsbnUnique(request.isbn(), id);

        book.setTitle(request.title().trim());
        book.setIsbn(request.isbn().trim());
        book.setAuthors(resolveAuthors(request.authors()));
        book.setCategories(resolveCategories(request.categories()));
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        book.publish();

        Book saved = bookRepository.save(book);
        log.info("Updated book {} ({})", saved.getId(), saved.getIsbn());
        return BookDTO.from(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookOrThrow(id);

        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new ConflictException("Cannot delete book while copies are currently borrowed");
        }

        bookRepository.delete(book);
        bookEventPublisher.publishBookRemoved(new BookRemovedEvent(book.getId(), book.getIsbn(), book.getTitle()));
        log.info("Deleted book {}", id);
    }

    public boolean checkAvailability(Long bookId) {
        return getBookOrThrow(bookId).getAvailableCopies() > 0;
    }

    private void ensureIsbnUnique(String isbn, Long existingBookId) {
        String normalized = isbn.trim();
        bookRepository.findByIsbn(normalized).ifPresent(existing -> {
            if (existingBookId == null || !existing.getId().equals(existingBookId)) {
                throw new ConflictException("Book with isbn " + normalized + " already exists");
            }
        });
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found. Id: " + id));
    }

    private void validateRequest(BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (isBlank(request.title())) {
            throw new IllegalArgumentException("Title is required");
        }
        if (isBlank(request.isbn())) {
            throw new IllegalArgumentException("ISBN is required");
        }
        if (request.authors() == null || request.authors().isEmpty()) {
            throw new IllegalArgumentException("At least one author is required");
        }
        if (request.categories() == null || request.categories().isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }
        if (request.totalCopies() == null || request.totalCopies() < 1) {
            throw new IllegalArgumentException("totalCopies must be at least 1");
        }
        if (request.availableCopies() == null || request.availableCopies() < 0) {
            throw new IllegalArgumentException("availableCopies must be 0 or more");
        }
        if (request.availableCopies() > request.totalCopies()) {
            throw new IllegalArgumentException("availableCopies cannot exceed totalCopies");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Set<Author> resolveAuthors(Set<String> names) {
        Set<Author> authors = new HashSet<>();
        for (String name : names) {
            String normalized = name == null ? "" : name.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            Author author = authorRepository.findByNameIgnoreCase(normalized)
                    .orElseGet(() -> authorRepository.save(Author.builder().name(normalized).build()));
            authors.add(author);
        }
        if (authors.isEmpty()) {
            throw new IllegalArgumentException("At least one valid author is required");
        }
        return authors;
    }

    private Set<Category> resolveCategories(Set<String> names) {
        Set<Category> categories = new HashSet<>();
        for (String name : names) {
            String normalized = name == null ? "" : name.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            Category category = categoryRepository.findByNameIgnoreCase(normalized)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(normalized).build()));
            categories.add(category);
        }
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("At least one valid category is required");
        }
        return categories;
    }

    private String normalizeOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
