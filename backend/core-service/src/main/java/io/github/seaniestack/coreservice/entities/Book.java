package io.github.seaniestack.coreservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String isbn;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @Column(nullable = false)
    private Integer totalCopies;

    @Column(nullable = false)
    private Integer availableCopies;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void publish() {
        if (totalCopies == null || totalCopies <= 0) {
            throw new IllegalStateException("A book must have totalCopies > 0 to be published");
        }
        if (availableCopies == null || availableCopies < 0) {
            throw new IllegalStateException("availableCopies must be 0 or more");
        }
        if (availableCopies > totalCopies) {
            throw new IllegalStateException("availableCopies can never exceed totalCopies");
        }
        this.active = true;
    }

    public void borrowCopy() {
        if (!active) {
            throw new IllegalStateException("Cannot borrow an inactive book");
        }
        if (availableCopies == null || availableCopies <= 0) {
            throw new IllegalStateException("No available copies");
        }
        availableCopies = availableCopies - 1;
    }

    public void returnCopy() {
        if (availableCopies == null || totalCopies == null) {
            throw new IllegalStateException("Book inventory is not initialized");
        }
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("availableCopies can never exceed totalCopies");
        }
        availableCopies = availableCopies + 1;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        publish();
    }

    @PreUpdate
    void onUpdate() {
        if (totalCopies == null || totalCopies <= 0) {
            throw new IllegalStateException("A book must have totalCopies > 0 to be published");
        }
        if (availableCopies == null || availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalStateException("availableCopies can never exceed totalCopies");
        }
        this.updatedAt = LocalDateTime.now();
    }
}
