package com.decisionhub.dto.response.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String type,
    String referenceType,
    Long referenceId,
    String actionUrl,
    boolean isRead,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {}
