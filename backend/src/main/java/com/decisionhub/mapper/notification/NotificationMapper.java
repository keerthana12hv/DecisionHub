package com.decisionhub.mapper.notification;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.entity.notification.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType().name(),
            notification.getReferenceType().name(),
            notification.getReferenceId(),
            notification.getActionUrl(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}
