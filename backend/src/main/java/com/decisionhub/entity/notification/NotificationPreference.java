package com.decisionhub.entity.notification;

import com.decisionhub.entity.authentication.User;
import jakarta.persistence.*;

@Entity
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean emailEnabled;
    private boolean pushEnabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}