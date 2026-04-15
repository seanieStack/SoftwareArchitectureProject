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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BookEventPublisher bookEventPublisher;

    @InjectMocks
    private BookService bookService;

    // ── getAllBooks() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllBooks returns all books as DTOs")
    void getAllBooks_returnsMappedDTOs() {
        when(bookRepository.findAll()).thenReturn(List.of(makeBook(1L), makeBook(2L)));

        List<BookDTO> result = bookService.getAllBooks();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookDTO::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("getAllBooks returns empty list when no books exist")
    void getAllBooks_emptyList_whenNoBooksExist() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<BookDTO> result = bookService.getAllBooks();

        assertThat(result).isEmpty();
    }

    // ── getBookById() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBookById returns DTO for an existing book")
    void getBookById_existingBook_returnsDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(makeBook(1L)));

        BookDTO result = bookService.getBookById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("getBookById throws ResourceNotFoundException for missing book")
    void getBookById_missingBook_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getBookAvailability() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getBookAvailability returns correct availability when copies remain")
    void getBookAvailability_withCopies_returnsAvailable() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(makeBook(1L)));

        BookAvailabilityDTO result = bookService.getBookAvailability(1L);

        assertThat(result.available()).isTrue();
        assertThat(result.availableCopies()).isEqualTo(3);
    }

    @Test
    @DisplayName("getBookAvailability returns unavailable when no copies remain")
    void getBookAvailability_noCopies_returnsUnavailable() {
        Book book = makeBook(1L);
        book.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookAvailabilityDTO result = bookService.getBookAvailability(1L);

        assertThat(result.available()).isFalse();
        assertThat(result.availableCopies()).isZero();
    }

    @Test
    @DisplayName("getBookAvailability throws for missing book")
    void getBookAvailability_missingBook_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookAvailability(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── createBook() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBook persists and returns DTO for a valid request")
    void createBook_validRequest_savesAndReturnsDTO() {
        BookRequest request = makeRequest("Clean Architecture", "9780134494166", 3, 3);
        stubRepositoriesForCreate();

        BookDTO result = bookService.createBook(request);

        assertThat(result.title()).isEqualTo("Clean Architecture");
        assertThat(result.isbn()).isEqualTo("9780134494166");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook reuses existing author from DB")
    void createBook_existingAuthor_reusesItRatherThanCreatingNew() {
        BookRequest request = makeRequest("Book", "9780000000002", 1, 1);
        Author existingAuthor = Author.builder().id(10L).name("Author One").build();
        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(authorRepository.findByNameIgnoreCase("Author One")).thenReturn(Optional.of(existingAuthor));
        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any())).thenAnswer(inv -> { Book b = inv.getArgument(0); b.setId(1L); return b; });

        bookService.createBook(request);

        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook reuses existing category from DB")
    void createBook_existingCategory_reusesItRatherThanCreatingNew() {
        BookRequest request = makeRequest("Book", "9780000000002", 1, 1);
        Category existingCat = Category.builder().id(10L).name("Fiction").build();
        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(authorRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(authorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(existingCat));
        when(bookRepository.save(any())).thenAnswer(inv -> { Book b = inv.getArgument(0); b.setId(1L); return b; });

        bookService.createBook(request);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook throws ConflictException for duplicate ISBN")
    void createBook_duplicateIsbn_throwsConflict() {
        BookRequest request = makeRequest("Book", "9780000000001", 3, 3);
        Book existing = makeBook(5L);
        existing.setIsbn("9780000000001");
        when(bookRepository.findByIsbn("9780000000001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("9780000000001");
    }

    @Test
    @DisplayName("createBook throws IllegalArgumentException for null request")
    void createBook_nullRequest_throwsException() {
        assertThatThrownBy(() -> bookService.createBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");
    }

    @Test
    @DisplayName("createBook throws IllegalArgumentException when totalCopies is zero")
    void createBook_zeroTotalCopies_throwsException() {
        assertThatThrownBy(() -> bookService.createBook(makeRequest("Book", "123", 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalCopies");
    }

    @Test
    @DisplayName("createBook throws when availableCopies exceeds totalCopies")
    void createBook_availableExceedsTotal_throwsException() {
        assertThatThrownBy(() -> bookService.createBook(makeRequest("Book", "123", 2, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("availableCopies");
    }

    @Test
    @DisplayName("createBook throws when title is blank")
    void createBook_blankTitle_throwsException() {
        BookRequest request = new BookRequest("  ", "1234567890", Set.of("Author"), Set.of("Cat"), 1, 1);
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title");
    }

    @Test
    @DisplayName("createBook throws when ISBN is blank")
    void createBook_blankIsbn_throwsException() {
        BookRequest request = new BookRequest("Title", "  ", Set.of("Author"), Set.of("Cat"), 1, 1);
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    @DisplayName("createBook throws when authors set is empty")
    void createBook_emptyAuthors_throwsException() {
        BookRequest request = new BookRequest("Title", "123", Set.of(), Set.of("Cat"), 1, 1);
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author");
    }

    @Test
    @DisplayName("createBook throws when categories set is empty")
    void createBook_emptyCategories_throwsException() {
        BookRequest request = new BookRequest("Title", "123", Set.of("Author"), Set.of(), 1, 1);
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("category");
    }

    // ── updateBook() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBook modifies all fields and saves")
    void updateBook_validRequest_updatesAndSaves() {
        Book existing = makeBook(1L);
        BookRequest request = makeRequest("Updated Title", "9780000000002", 5, 4);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn("9780000000002")).thenReturn(Optional.empty());
        stubAuthorCategoryCreate();
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookDTO result = bookService.updateBook(1L, request);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.totalCopies()).isEqualTo(5);
        verify(bookRepository).save(existing);
    }

    @Test
    @DisplayName("updateBook throws ResourceNotFoundException for missing book")
    void updateBook_missingBook_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, makeRequest("Title", "123", 1, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateBook allows same ISBN for the same book (update without ISBN change)")
    void updateBook_sameIsbnSameBook_doesNotThrow() {
        Book existing = makeBook(1L);
        existing.setIsbn("9780000000001");
        BookRequest request = makeRequest("New Title", "9780000000001", 3, 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn("9780000000001")).thenReturn(Optional.of(existing)); // same book
        stubAuthorCategoryCreate();
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> bookService.updateBook(1L, request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateBook throws ConflictException when ISBN belongs to a different book")
    void updateBook_isbnBelongsToDifferentBook_throwsConflict() {
        Book existing = makeBook(1L);
        Book other = makeBook(2L);
        other.setIsbn("9780000000002");
        BookRequest request = makeRequest("Title", "9780000000002", 1, 1);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn("9780000000002")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> bookService.updateBook(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    // ── deleteBook() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook deletes book and publishes event when no copies are borrowed")
    void deleteBook_noBorrowedCopies_deletesAndPublishesEvent() {
        Book book = makeBook(1L); // all 3 copies available, 0 borrowed
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
        ArgumentCaptor<BookRemovedEvent> captor = ArgumentCaptor.forClass(BookRemovedEvent.class);
        verify(bookEventPublisher).publishBookRemoved(captor.capture());
        assertThat(captor.getValue().bookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deleteBook throws ConflictException when copies are currently borrowed")
    void deleteBook_borrowedCopies_throwsConflict() {
        Book book = makeBook(1L);
        book.setAvailableCopies(1); // 2 of 3 are borrowed
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("borrowed");

        verify(bookRepository, never()).delete(any());
        verify(bookEventPublisher, never()).publishBookRemoved(any());
    }

    @Test
    @DisplayName("deleteBook throws ResourceNotFoundException for missing book")
    void deleteBook_missingBook_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── checkAvailability() ───────────────────────────────────────────────────

    @Test
    @DisplayName("checkAvailability returns true when copies are available")
    void checkAvailability_withCopies_returnsTrue() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(makeBook(1L)));
        assertThat(bookService.checkAvailability(1L)).isTrue();
    }

    @Test
    @DisplayName("checkAvailability returns false when no copies are available")
    void checkAvailability_noCopies_returnsFalse() {
        Book book = makeBook(1L);
        book.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        assertThat(bookService.checkAvailability(1L)).isFalse();
    }

    // ── decrementAvailableCopies() ────────────────────────────────────────────

    @Test
    @DisplayName("decrementAvailableCopies reduces available count by 1")
    void decrementAvailableCopies_reducesCount() {
        Book book = makeBook(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenReturn(book);

        bookService.decrementAvailableCopies(1L);

        assertThat(book.getAvailableCopies()).isEqualTo(2);
        verify(bookRepository).save(book);
    }

    // ── incrementAvailableCopies() ────────────────────────────────────────────

    @Test
    @DisplayName("incrementAvailableCopies increases available count by 1")
    void incrementAvailableCopies_increasesCount() {
        Book book = makeBook(1L);
        book.setAvailableCopies(2);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenReturn(book);

        bookService.incrementAvailableCopies(1L);

        assertThat(book.getAvailableCopies()).isEqualTo(3);
        verify(bookRepository).save(book);
    }

    // ── retireAvailableCopies() ───────────────────────────────────────────────

    @Test
    @DisplayName("retireAvailableCopies deletes book and returns null when no copies are borrowed")
    void retireAvailableCopies_noBorrowedCopies_deletesBook() {
        Book book = makeBook(1L); // all 3 available
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookDTO result = bookService.retireAvailableCopies(1L);

        assertThat(result).isNull();
        verify(bookRepository).delete(book);
        verify(bookEventPublisher).publishBookRemoved(any());
    }

    @Test
    @DisplayName("retireAvailableCopies sets available to 0 and adjusts total when copies are borrowed")
    void retireAvailableCopies_withBorrowedCopies_setsAvailableToZero() {
        Book book = makeBook(1L);
        book.setAvailableCopies(1); // 2 of 3 are borrowed
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenReturn(book);

        BookDTO result = bookService.retireAvailableCopies(1L);

        assertThat(result).isNotNull();
        assertThat(book.getAvailableCopies()).isEqualTo(0);
        assertThat(book.getTotalCopies()).isEqualTo(2); // only the borrowed copies remain
        verify(bookRepository, never()).delete(any());
    }

    // ── searchBooks() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchBooks delegates to repository with correct parameters")
    void searchBooks_delegatesToRepository() {
        when(bookRepository.search("clean", "Martin", "Architecture")).thenReturn(List.of(makeBook(1L)));

        List<BookDTO> result = bookService.searchBooks("clean", "Martin", "Architecture");

        assertThat(result).hasSize(1);
        verify(bookRepository).search("clean", "Martin", "Architecture");
    }

    @Test
    @DisplayName("searchBooks converts blank strings to null before passing to repository")
    void searchBooks_blankParams_convertedToNull() {
        when(bookRepository.search(null, null, null)).thenReturn(List.of());

        bookService.searchBooks("  ", "", null);

        verify(bookRepository).search(null, null, null);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Book makeBook(Long id) {
        Book book = Book.builder()
                .title("Test Book")
                .isbn("9780000000001")
                .totalCopies(3)
                .availableCopies(3)
                .active(true)
                .authors(new HashSet<>(Set.of(Author.builder().id(1L).name("Author One").build())))
                .categories(new HashSet<>(Set.of(Category.builder().id(1L).name("Fiction").build())))
                .build();
        book.setId(id);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        return book;
    }

    private BookRequest makeRequest(String title, String isbn, int total, int available) {
        return new BookRequest(title, isbn, Set.of("Author One"), Set.of("Fiction"), total, available);
    }

    private void stubRepositoriesForCreate() {
        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        stubAuthorCategoryCreate();
        when(bookRepository.save(any())).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
    }

    private void stubAuthorCategoryCreate() {
        when(authorRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(authorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
