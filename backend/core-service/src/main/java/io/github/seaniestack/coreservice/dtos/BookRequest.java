package io.github.seaniestack.coreservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

public record BookRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "ISBN is required")
        String isbn,
        @NotEmpty(message = "At least one author is required")
        Set<@NotBlank(message = "Author name is required") String> authors,
        @NotEmpty(message = "At least one category is required")
        Set<@NotBlank(message = "Category name is required") String> categories,
        @NotNull(message = "totalCopies is required")
        @Min(value = 1, message = "totalCopies must be at least 1")
        Integer totalCopies,
        @NotNull(message = "availableCopies is required")
        @PositiveOrZero(message = "availableCopies must be 0 or more")
        Integer availableCopies
) {
}
