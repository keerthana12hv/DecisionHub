package com.decisionhub.entity.authentication;

import com.decisionhub.entity.community.*;
import com.decisionhub.entity.voting.*;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private PlatformRole role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<OAuthAccount> oauthAccounts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PasswordResetToken> resetTokens;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CommunityMember> communityMemberships;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Vote> votes;
}