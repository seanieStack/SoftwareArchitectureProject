package io.github.seaniestack.coreservice.dtos;

import io.github.seaniestack.coreservice.entities.Book;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record BookDTO(
        Long id,
        String title,
        String isbn,
        Set<String> authors,
        Set<String> categories,
        Integer totalCopies,
        Integer availableCopies,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookDTO from(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getAuthors().stream().map(a -> a.getName()).collect(Collectors.toSet()),
                book.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.isActive(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
