package com.decisionhub.controller.notification;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.service.interfaces.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationService.getNotifications(pageable, unreadOnly);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllNotifications() {
        notificationService.clearAllNotifications();
        return ResponseEntity.noContent().build();
    }
}
