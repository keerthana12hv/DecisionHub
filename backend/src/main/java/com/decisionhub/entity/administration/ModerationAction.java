package com.decisionhub.entity.administration;

import com.decisionhub.entity.authentication.User;
import jakarta.persistence.*;

@Entity
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actionType;
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;
}