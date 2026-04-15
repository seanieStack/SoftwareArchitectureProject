package io.github.seaniestack.coreservice.controllers;

import io.github.seaniestack.coreservice.entities.Book;
import io.github.seaniestack.coreservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalBookControllerContractTest {

    @Mock
    private BookRepository bookRepository;

    private InternalBookController controller;

    @BeforeEach
    void setUp() {
        controller = new InternalBookController(bookRepository);
        ReflectionTestUtils.setField(controller, "internalSecret", "test-internal-secret");
    }

    @Test
    @DisplayName("CONTRACT-BOOK-EXISTS-200 provider returns 200 when the internal exists endpoint is called for a real book")
    void existsEndpoint_returns200ForExistingBook() {
        when(bookRepository.existsById(42L)).thenReturn(true);

        ResponseEntity<Void> response = controller.bookExists(42L, "test-internal-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-EXISTS-404 provider returns 404 when the internal exists endpoint is called for a missing book")
    void existsEndpoint_returns404ForMissingBook() {
        when(bookRepository.existsById(9999L)).thenReturn(false);

        ResponseEntity<Void> response = controller.bookExists(9999L, "test-internal-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-EXISTS-403 provider returns 403 when the internal secret is missing")
    void existsEndpoint_returns403WhenSecretMissing() {
        ResponseEntity<Void> response = controller.bookExists(42L, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-BORROW-200 provider decrements available copies and returns 200")
    void borrowEndpoint_returns200AndDecrementsCopies() {
        Book book = Book.builder()
                .title("Contract Testing")
                .isbn("9780135974445")
                .totalCopies(2)
                .availableCopies(2)
                .active(true)
                .build();
        book.publish();
        when(bookRepository.findById(42L)).thenReturn(Optional.of(book));

        ResponseEntity<Void> response = controller.borrowCopy(42L, "test-internal-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(book.getAvailableCopies()).isEqualTo(1);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-RETURN-200 provider increments available copies and returns 200")
    void returnEndpoint_returns200AndIncrementsCopies() {
        Book book = Book.builder()
                .title("Contract Testing")
                .isbn("9780135974445")
                .totalCopies(2)
                .availableCopies(1)
                .active(true)
                .build();
        book.publish();
        when(bookRepository.findById(42L)).thenReturn(Optional.of(book));

        ResponseEntity<Void> response = controller.returnCopy(42L, "test-internal-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(book.getAvailableCopies()).isEqualTo(2);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-BORROW-404 provider returns 404 when the book is missing")
    void borrowEndpoint_returns404ForMissingBook() {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.borrowCopy(42L, "test-internal-secret"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
