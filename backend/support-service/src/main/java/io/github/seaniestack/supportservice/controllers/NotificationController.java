package io.github.seaniestack.supportservice.controllers;

import io.github.seaniestack.supportservice.dtos.NotificationDTO;
import io.github.seaniestack.supportservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        log.trace("GET /api/notifications/user/{} unreadOnly={}", userId, unreadOnly);
        List<NotificationDTO> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnreadNotificationsForUser(userId);
        } else {
            notifications = notificationService.getNotificationsForUser(userId);
        }
        log.debug("Returning {} notifications for user {}", notifications.size(), userId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        log.trace("PATCH /api/notifications/{}/read", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }
}
