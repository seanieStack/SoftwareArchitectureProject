package io.github.seaniestack.coreservice.messaging.events;

public record BookRemovedEvent(
        Long bookId,
        String isbn,
        String title
) {
}
