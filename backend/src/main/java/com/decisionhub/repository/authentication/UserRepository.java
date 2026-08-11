package com.decisionhub.repository.authentication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.authentication.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    long countByStatus(UserStatus status);



}