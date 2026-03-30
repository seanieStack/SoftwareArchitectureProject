package io.github.seaniestack.supportservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long borrowId;

    private Long userId;

    private BigDecimal amount;

    private LocalDateTime issuedAt;

    private boolean paid;

    private boolean acknowledged;

    private LocalDateTime paidAt;

    //todo: column names and stuff yno
}
