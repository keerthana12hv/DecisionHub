package com.decisionhub.repository.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.OAuthAccount;

public interface OAuthAccountRepository
        extends JpaRepository<OAuthAccount, Long> {

}