package io.github.seaniestack.coreservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seaniestack.coreservice.CoreServiceApplication;
import io.github.seaniestack.coreservice.dtos.BookDTO;
import io.github.seaniestack.coreservice.dtos.BookRequest;
import io.github.seaniestack.coreservice.messaging.BookEventPublisher;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import io.github.seaniestack.coreservice.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoreServiceApplication.class)
class BookControllerIntegrationTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookService bookService;
    @Autowired private BookRepository bookRepository;
    @Autowired private WebApplicationContext context;

    @MockitoBean
    private BookEventPublisher bookEventPublisher;

    /** Headers for a regular authenticated user (student) */
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // also add this
                .build();
        bookRepository.deleteAll();
    }

    // ── GET /api/books ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books returns 200 and list of books for authenticated user")
    void getBooks_authenticated_returns200() throws Exception {
        createBook("Clean Code", "9780132350884");

        mockMvc.perform(get("/api/books")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/books returns 401 when no authentication headers are present")
    void getBooks_noHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/books returns empty array when no books exist")
    void getBooks_noBooks_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/{id} returns 200 and the correct book")
    void getBookById_existingBook_returns200() throws Exception {
        BookDTO book = createBook("Refactoring", "9780201485677");

        mockMvc.perform(get("/api/books/" + book.id())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"))
                .andExpect(jsonPath("$.isbn").value("9780201485677"));
    }

    @Test
    @DisplayName("GET /api/books/{id} returns 404 for a non-existent book")
    void getBookById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/books/99999")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STAFF"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/books/{id}/availability ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/{id}/availability returns correct availability")
    void getBookAvailability_returns200WithCorrectData() throws Exception {
        BookDTO book = createBook("Design Patterns", "9780201633610");

        mockMvc.perform(get("/api/books/" + book.id() + "/availability")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.availableCopies").value(2));
    }

    // ── GET /api/books/search ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/search with keyword returns matching books")
    void searchBooks_byKeyword_returnsMatches() throws Exception {
        createBook("Clean Code", "9780132350884");
        createBook("Clean Architecture", "9780134494166");
        createBook("Refactoring", "9780201485677");

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "Clean")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/books/search with no params returns all books")
    void searchBooks_noParams_returnsAllBooks() throws Exception {
        createBook("Book A", "9780000000001");
        createBook("Book B", "9780000000002");

        mockMvc.perform(get("/api/books/search")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── POST /api/books ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/books returns 201 for an ADMIN user")
    void createBook_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "New Book",
                                "isbn", "9780000000099",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 3,
                                "availableCopies", 3
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/books returns 403 for a STUDENT user")
    void createBook_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "New Book",
                                "isbn", "9780000000099",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 3,
                                "availableCopies", 3
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/books returns 400 when totalCopies is zero")
    void createBook_zeroTotalCopies_returns400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Bad Book",
                                "isbn", "9780000000098",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 0,
                                "availableCopies", 0
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/books returns 409 for duplicate ISBN")
    void createBook_duplicateIsbn_returns409() throws Exception {
        createBook("Existing Book", "9780000000001");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Duplicate",
                                "isbn", "9780000000001",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 1,
                                "availableCopies", 1
                        ))))
                .andExpect(status().isConflict());
    }

    // ── PUT /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/books/{id} returns 200 and updated book for ADMIN")
    void updateBook_asAdmin_returns200() throws Exception {
        BookDTO book = createBook("Old Title", "9780000000003");

        mockMvc.perform(put("/api/books/" + book.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "New Title",
                                "isbn", "9780000000004",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 2,
                                "availableCopies", 2
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @DisplayName("PUT /api/books/{id} returns 404 for non-existent book")
    void updateBook_nonExistent_returns404() throws Exception {
        mockMvc.perform(put("/api/books/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Title",
                                "isbn", "9780000000099",
                                "authors", Set.of("Author"),
                                "categories", Set.of("Category"),
                                "totalCopies", 1,
                                "availableCopies", 1
                        ))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/books/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/books/{id} returns 204 when all copies are available")
    void deleteBook_asAdmin_returns204() throws Exception {
        BookDTO book = createBook("To Delete", "9780000000005");

        mockMvc.perform(delete("/api/books/" + book.id())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} returns 403 for non-admin user")
    void deleteBook_asStudent_returns403() throws Exception {
        BookDTO book = createBook("Protected Book", "9780000000006");

        mockMvc.perform(delete("/api/books/" + book.id())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} returns 409 when copies are borrowed")
    void deleteBook_withBorrowedCopies_returns409() throws Exception {
        // Create with 1 available, 1 borrowed (total=2, available=1)
        BookDTO book = bookService.createBook(new BookRequest(
                "Borrowed Book", "9780000000007",
                Set.of("Author"), Set.of("Cat"), 2, 1));

        mockMvc.perform(delete("/api/books/" + book.id())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/books/{id}/retire ──────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/books/{id}/retire returns 204 when all copies are available (deletes book)")
    void retireBook_noBorrowedCopies_returns204() throws Exception {
        BookDTO book = createBook("Retire Me", "9780000000008");

        mockMvc.perform(patch("/api/books/" + book.id() + "/retire")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/books/{id}/retire returns 200 and updated book when some copies are borrowed")
    void retireBook_withBorrowedCopies_returns200() throws Exception {
        BookDTO book = bookService.createBook(new BookRequest(
                "Partial Retire", "9780000000009",
                Set.of("Author"), Set.of("Cat"), 3, 1)); // 2 borrowed

        mockMvc.perform(patch("/api/books/" + book.id() + "/retire")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BookDTO createBook(String title, String isbn) {
        return bookService.createBook(new BookRequest(
                title, isbn, Set.of("Test Author"), Set.of("Test Category"), 2, 2));
    }
}
