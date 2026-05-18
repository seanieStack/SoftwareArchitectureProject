package io.github.seaniestack.supportservice.messaging;

import io.github.seaniestack.supportservice.entities.Borrow;
import io.github.seaniestack.supportservice.entities.BorrowStatus;
import io.github.seaniestack.supportservice.entities.NotificationType;
import io.github.seaniestack.supportservice.messaging.events.BookRemovedEvent;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import io.github.seaniestack.supportservice.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreEventListenerTest {

    @Mock
    BorrowRepository borrowRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    CoreEventListener listener;

    @Test
    void handleBookRemoved_cancelsActiveBorrowsAndNotifiesUsers() {
        BookRemovedEvent event = new BookRemovedEvent(42L, "978-0-00-000000-0", "Test Book");

        Borrow borrowed = Borrow.builder().id(1L).userId(10L).bookId(42L).status(BorrowStatus.BORROWED).build();
        Borrow overdue  = Borrow.builder().id(2L).userId(11L).bookId(42L).status(BorrowStatus.OVERDUE).build();

        when(borrowRepository.findByBookIdAndStatusIn(42L, List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE)))
                .thenReturn(List.of(borrowed, overdue));

        listener.handleBookRemoved(event);

        assertThat(borrowed.getStatus()).isEqualTo(BorrowStatus.CANCELLED);
        assertThat(overdue.getStatus()).isEqualTo(BorrowStatus.CANCELLED);
        verify(borrowRepository, times(2)).save(any(Borrow.class));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createNotification(eq(10L), eq(NotificationType.BORROW_CANCELLED), messageCaptor.capture());
        verify(notificationService).createNotification(eq(11L), eq(NotificationType.BORROW_CANCELLED), messageCaptor.capture());
        assertThat(messageCaptor.getAllValues()).allMatch(msg -> msg.contains("Test Book"));
    }

    @Test
    void handleBookRemoved_doesNothingWhenNoActiveBorrows() {
        BookRemovedEvent event = new BookRemovedEvent(42L, "978-0-00-000000-0", "Test Book");

        when(borrowRepository.findByBookIdAndStatusIn(42L, List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE)))
                .thenReturn(List.of());

        listener.handleBookRemoved(event);

        verify(borrowRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }
}
