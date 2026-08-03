package com.decisionhub.controller.notification;

import com.decisionhub.dto.response.notification.NotificationResponse;
import com.decisionhub.service.interfaces.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.decisionhub.config.JwtService;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.decisionhub\\.security\\..*"
        )
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationResponse = new NotificationResponse(
                10L,
                "New Comment",
                "A new comment was posted",
                "COMMENT_CREATED",
                "DECISION",
                100L,
                "/decisions/100",
                false,
                LocalDateTime.now(),
                null
        );
    }

    @Test
    void getNotifications_success() throws Exception {
        Page<NotificationResponse> page = new PageImpl<>(List.of(notificationResponse));
        when(notificationService.getNotifications(any(Pageable.class), eq(false))).thenReturn(page);

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "10")
                        .param("unreadOnly", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].title").value("New Comment"))
                .andExpect(jsonPath("$.content[0].isRead").value(false));

        verify(notificationService, times(1)).getNotifications(eq(PageRequest.of(0, 10)), eq(false));
    }

    @Test
    void getUnreadCount_success() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void markAsRead_success() throws Exception {
        when(notificationService.markAsRead(10L)).thenReturn(notificationResponse);

        mockMvc.perform(patch("/api/notifications/10/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void markAllAsRead_success() throws Exception {
        doNothing().when(notificationService).markAllAsRead();

        mockMvc.perform(patch("/api/notifications/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAllAsRead();
    }

    @Test
    void deleteNotification_success() throws Exception {
        doNothing().when(notificationService).deleteNotification(10L);

        mockMvc.perform(delete("/api/notifications/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(10L);
    }

    @Test
    void clearAllNotifications_success() throws Exception {
        doNothing().when(notificationService).clearAllNotifications();

        mockMvc.perform(delete("/api/notifications/clear-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).clearAllNotifications();
    }
}
