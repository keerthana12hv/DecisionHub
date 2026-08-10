package com.decisionhub.repository.community;

import java.util.List;
import java.util.Optional;
import com.decisionhub.entity.authentication.User;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.community.Community;
import com.decisionhub.enums.community.CommunityVisibility;
public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findBySlug(String slug);

    List<Community> findByOwner(User owner);
    long countByVisibility(CommunityVisibility visibility);
    Community findTopByOrderByMemberCountDesc();
    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // Returns only active (not soft-deleted) communities
    List<Community> findByDeletedAtIsNull();

}