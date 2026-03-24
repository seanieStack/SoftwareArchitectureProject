package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.entities.Fine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserId(Long userId);
    List<Fine> findByUserIdAndAcknowledgedFalse(Long userId);
}
