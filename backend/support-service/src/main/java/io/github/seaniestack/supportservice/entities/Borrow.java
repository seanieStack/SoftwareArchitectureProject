package io.github.seaniestack.supportservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrows")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long bookId;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    private LocalDateTime borrowedAt;

    private LocalDateTime deadline;

    private LocalDateTime returnedAt;

}
