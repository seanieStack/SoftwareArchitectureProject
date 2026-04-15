package io.github.seaniestack.coreservice.domain;

import io.github.seaniestack.coreservice.entities.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BookEntityTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .title("Test Book")
                .isbn("9780000000001")
                .totalCopies(3)
                .availableCopies(3)
                .active(false)
                .build();
    }

    // ── publish() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("publish sets active to true")
    void publish_setsActiveTrue() {
        book.publish();
        assertThat(book.isActive()).isTrue();
    }

    @Test
    @DisplayName("publish throws when totalCopies is zero")
    void publish_throwsWhenTotalCopiesZero() {
        book.setTotalCopies(0);
        assertThatThrownBy(() -> book.publish())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("totalCopies");
    }

    @Test
    @DisplayName("publish throws when totalCopies is null")
    void publish_throwsWhenTotalCopiesNull() {
        book.setTotalCopies(null);
        assertThatThrownBy(() -> book.publish())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("publish throws when availableCopies is negative")
    void publish_throwsWhenAvailableCopiesNegative() {
        book.setAvailableCopies(-1);
        assertThatThrownBy(() -> book.publish())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("availableCopies");
    }

    @Test
    @DisplayName("publish throws when availableCopies exceeds totalCopies")
    void publish_throwsWhenAvailableExceedsTotal() {
        book.setTotalCopies(2);
        book.setAvailableCopies(5);
        assertThatThrownBy(() -> book.publish())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("availableCopies");
    }

    @Test
    @DisplayName("publish succeeds when availableCopies equals totalCopies")
    void publish_succeedsWhenAvailableEqualsTotal() {
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        assertThatCode(() -> book.publish()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("publish succeeds when availableCopies is zero (all loaned out)")
    void publish_succeedsWhenAvailableIsZero() {
        book.setAvailableCopies(0);
        assertThatCode(() -> book.publish()).doesNotThrowAnyException();
    }

    // ── borrowCopy() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("borrowCopy decrements availableCopies by 1")
    void borrowCopy_decrementsAvailableCopies() {
        book.publish();
        book.borrowCopy();
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    @DisplayName("borrowCopy can borrow down to zero")
    void borrowCopy_canBorrowAllCopies() {
        book.publish();
        book.borrowCopy();
        book.borrowCopy();
        book.borrowCopy();
        assertThat(book.getAvailableCopies()).isEqualTo(0);
    }

    @Test
    @DisplayName("borrowCopy throws when no copies available")
    void borrowCopy_throwsWhenNoCopiesAvailable() {
        book.publish();
        book.setAvailableCopies(0);
        assertThatThrownBy(() -> book.borrowCopy())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No available copies");
    }

    @Test
    @DisplayName("borrowCopy throws when book is inactive")
    void borrowCopy_throwsWhenBookInactive() {
        // book not published, active = false
        assertThatThrownBy(() -> book.borrowCopy())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inactive");
    }

    // ── returnCopy() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnCopy increments availableCopies by 1")
    void returnCopy_incrementsAvailableCopies() {
        book.publish();
        book.setAvailableCopies(2);
        book.returnCopy();
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    @DisplayName("returnCopy throws when availableCopies already equals totalCopies")
    void returnCopy_throwsWhenAlreadyFull() {
        book.publish();
        // available == total == 3
        assertThatThrownBy(() -> book.returnCopy())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("availableCopies");
    }

    @Test
    @DisplayName("returnCopy throws when totalCopies is null")
    void returnCopy_throwsWhenTotalCopiesNull() {
        book.publish();
        book.setAvailableCopies(1);
        book.setTotalCopies(null);
        assertThatThrownBy(() -> book.returnCopy())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("borrow then return leaves count unchanged")
    void borrowThenReturn_leavesCountUnchanged() {
        book.publish();
        int original = book.getAvailableCopies();
        book.borrowCopy();
        book.returnCopy();
        assertThat(book.getAvailableCopies()).isEqualTo(original);
    }
}
