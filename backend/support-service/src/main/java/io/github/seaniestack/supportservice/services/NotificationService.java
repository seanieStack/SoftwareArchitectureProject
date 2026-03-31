package io.github.seaniestack.supportservice.services;

import io.github.seaniestack.supportservice.dtos.NotificationDTO;
import io.github.seaniestack.supportservice.entities.Notification;
import io.github.seaniestack.supportservice.entities.NotificationType;
import io.github.seaniestack.supportservice.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        log.debug("Fetching notifications for user {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(NotificationDTO::from).toList();
    }

    public List<NotificationDTO> getUnreadNotificationsForUser(Long userId) {
        log.debug("Fetching unread notifications for user {}", userId);
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(NotificationDTO::from).toList();
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found. Id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read", notificationId);
    }

    public void createNotification(Long userId, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("Created {} notification for user {}", type, userId);
    }
}
