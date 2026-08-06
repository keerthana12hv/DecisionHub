package com.decisionhub.service.impl;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.notification.Notification;
import com.decisionhub.enums.notification.NotificationType;
import com.decisionhub.enums.notification.ReferenceType;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.notification.NotificationMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.notification.NotificationRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.impl.notification.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User recipient;
    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(1L);
        recipient.setUsername("recipientUser");

        notification = new Notification();
        notification.setId(10L);
        notification.setRecipient(recipient);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setType(NotificationType.COMMENT_CREATED);
        notification.setReferenceType(ReferenceType.DECISION);
        notification.setReferenceId(100L);
        notification.setActionUrl("/decisions/100");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationResponse = new NotificationResponse(
                10L,
                "Title",
                "Message",
                "COMMENT_CREATED",
                "DECISION",
                100L,
                "/decisions/100",
                false,
                notification.getCreatedAt(),
                null
        );
    }

    @Test
    void createNotification_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createNotification(
                1L, "Title", "Message",
                NotificationType.COMMENT_CREATED, ReferenceType.DECISION, 100L, "/decisions/100"
        );

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_recipientNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.createNotification(
                1L, "Title", "Message",
                NotificationType.COMMENT_CREATED, ReferenceType.DECISION, 100L, "/decisions/100"
        ));

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotifications_all_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        Page<NotificationResponse> result = notificationService.getNotifications(pageable, false);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(notificationResponse, result.getContent().get(0));
        verify(notificationRepository, times(1)).findByRecipientIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    void getNotifications_unreadOnly_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        Page<NotificationResponse> result = notificationService.getNotifications(pageable, true);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository, times(1)).findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    void getNotifications_unauthenticated() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedActionException.class, () -> notificationService.getNotifications(pageable, false));
    }

    @Test
    void getUnreadCount_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount();

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(10L);

        assertNotNull(result);
        verify(notificationRepository, times(1)).save(notification);
        assertTrue(notification.isRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void markAsRead_unauthorized() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(99L)); // Different user
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedActionException.class, () -> notificationService.markAsRead(10L));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));

        notificationService.markAllAsRead();

        verify(notificationRepository, times(1)).markAllAsRead(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void deleteNotification_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(10L);

        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void deleteNotification_unauthorized() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(99L));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedActionException.class, () -> notificationService.deleteNotification(10L));
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    void clearAllNotifications_success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(1L));

        notificationService.clearAllNotifications();

        verify(notificationRepository, times(1)).deleteAllByRecipientId(1L);
    }
}
