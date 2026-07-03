package com.decisionhub.entity.authentication;

import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.entity.notification.Notification;
import com.decisionhub.entity.notification.NotificationPreference;
import com.decisionhub.entity.reports.ReportExport;
import com.decisionhub.entity.voting.Vote;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ===========================
    // Authentication
    // ===========================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<OAuthAccount> oauthAccounts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PasswordResetToken> passwordResetTokens;

    // ===========================
    // Community
    // ===========================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CommunityMember> communityMemberships;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Community> ownedCommunities;

    // ===========================
    // Decisions
    // ===========================

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Decision> createdDecisions;

    // ===========================
    // Voting
    // ===========================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Vote> votes;

    // ===========================
    // Discussions
    // ===========================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Comment> comments;

    // ===========================
    // Notifications
    // ===========================

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private NotificationPreference notificationPreference;

    // ===========================
    // Reports
    // ===========================

    @OneToMany(mappedBy = "requestedBy", fetch = FetchType.LAZY)
    private List<ReportExport> reportExports;
    
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}