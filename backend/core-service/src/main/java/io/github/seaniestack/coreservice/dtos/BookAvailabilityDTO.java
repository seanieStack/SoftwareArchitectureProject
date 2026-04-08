package io.github.seaniestack.coreservice.dtos;

import io.github.seaniestack.coreservice.entities.Book;

public record BookAvailabilityDTO(
        Long bookId,
        String title,
        boolean available,
        Integer availableCopies
) {
    public static BookAvailabilityDTO from(Book book) {
        return new BookAvailabilityDTO(
                book.getId(),
                book.getTitle(),
                book.getAvailableCopies() > 0,
                book.getAvailableCopies()
        );
    }
}
