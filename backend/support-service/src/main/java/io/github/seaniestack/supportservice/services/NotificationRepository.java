package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}
