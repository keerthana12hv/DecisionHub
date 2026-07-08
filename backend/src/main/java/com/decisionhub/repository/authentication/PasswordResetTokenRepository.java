package com.decisionhub.repository.authentication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.PasswordResetToken;
import com.decisionhub.entity.authentication.User;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);
}