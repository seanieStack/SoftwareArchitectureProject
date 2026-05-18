package io.github.seaniestack.supportservice.messaging.events;

public record BookRemovedEvent(
        Long bookId,
        String isbn,
        String title
) {
}
