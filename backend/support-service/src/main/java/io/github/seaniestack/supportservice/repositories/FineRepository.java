package io.github.seaniestack.supportservice.repositories;

import io.github.seaniestack.supportservice.entities.Fine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserId(Long userId);
    List<Fine> findByUserIdAndAcknowledgedFalse(Long userId);

    Optional<Fine> findByBorrowIdAndPaidFalse(Long borrowId);

    Long countByPaidFalse();

    Long countByPaidTrue();
}
