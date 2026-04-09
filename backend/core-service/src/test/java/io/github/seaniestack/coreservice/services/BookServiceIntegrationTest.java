package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.CoreServiceApplication;
import io.github.seaniestack.coreservice.dtos.BookDTO;
import io.github.seaniestack.coreservice.dtos.BookRequest;
import io.github.seaniestack.coreservice.messaging.BookEventPublisher;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = CoreServiceApplication.class
)
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @MockitoBean
    private BookEventPublisher bookEventPublisher;

    @Test
    void createBook_shouldPersistToDatabase() {
        BookRequest request = new BookRequest(
                "Clean Architecture",
                "9780134494166",
                Set.of("Robert C. Martin"),
                Set.of("Software Architecture"),
                3,
                3
        );

        BookDTO created = bookService.createBook(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Clean Architecture");
        assertThat(bookRepository.findById(created.id())).isPresent();
    }

    @Test
    void createBook_withZeroTotalCopies_shouldThrowException() {
        BookRequest request = new BookRequest(
                "Invalid Book",
                "9780134494999",
                Set.of("Robert C. Martin"),
                Set.of("Software Architecture"),
                0,
                0
        );

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(request));
    }
}
