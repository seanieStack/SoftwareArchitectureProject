package io.github.seaniestack.supportservice.dtos;

import io.github.seaniestack.supportservice.entities.Notification;
import io.github.seaniestack.supportservice.entities.NotificationType;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        Long userId,
        NotificationType type,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
