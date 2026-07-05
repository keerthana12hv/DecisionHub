package com.decisionhub.repository.community;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;

public interface CommunityMemberRepository
        extends JpaRepository<CommunityMember, Long> {

    Optional<CommunityMember> findByCommunityAndUser(
            Community community,
            User user
    );

    List<CommunityMember> findByCommunity(Community community);

    List<CommunityMember> findByUser(User user);

    boolean existsByCommunityAndUser(
            Community community,
            User user
    );

}