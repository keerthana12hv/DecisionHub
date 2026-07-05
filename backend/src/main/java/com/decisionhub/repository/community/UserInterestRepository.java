package com.decisionhub.repository.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.UserInterest;

public interface UserInterestRepository
        extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUser(User user);

    boolean existsByUserAndCategory(
            User user,
            Category category
    );

}