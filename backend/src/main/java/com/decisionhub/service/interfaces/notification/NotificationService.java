package com.decisionhub.service.interfaces.notification;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // Internal API to persist a new notification (called by Event Listeners)
    void createNotification(Long recipientId, String title, String message, 
                            NotificationType type, ReferenceType refType, Long refId, String actionUrl);

    // Public API endpoints
    Page<NotificationResponse> getNotifications(Pageable pageable, boolean unreadOnly);
    
    long getUnreadCount();
    
    NotificationResponse markAsRead(Long notificationId);
    
    void markAllAsRead();
    
    void deleteNotification(Long notificationId);
    
    void clearAllNotifications();
}
