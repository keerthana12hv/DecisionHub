package com.decisionhub.service.impl.notification;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.notification.Notification;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.notification.NotificationMapper;
import com.decisionhub.repository.notification.NotificationRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(Long recipientId, String title, String message, 
                                  NotificationType type, ReferenceType refType, Long refId, String actionUrl) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found with ID: " + recipientId));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceType(refType);
        notification.setReferenceId(refId);
        notification.setActionUrl(actionUrl);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Pageable pageable, boolean unreadOnly) {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to retrieve notifications"));

        Page<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUserId, pageable);
        } else {
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);
        }

        return notifications.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to check unread count"));

        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUserId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to mark notifications as read"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You are not authorized to view or modify this notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to mark all as read"));

        notificationRepository.markAllAsRead(currentUserId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to delete notifications"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You are not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void clearAllNotifications() {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated to clear notifications"));

        notificationRepository.deleteAllByRecipientId(currentUserId);
    }
}
