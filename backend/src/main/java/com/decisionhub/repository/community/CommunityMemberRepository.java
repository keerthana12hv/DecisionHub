package com.decisionhub.repository.community;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.enums.community.MembershipStatus;

public interface CommunityMemberRepository
        extends JpaRepository<CommunityMember, Long> {

    Optional<CommunityMember> findByCommunityAndUser(
            Community community,
            User user
    );

    Optional<CommunityMember> findByCommunityIdAndUserId(
            Long communityId,
            Long userId
    );

    Optional<CommunityMember> findByIdAndCommunity(
            Long id,
            Community community
    );

    List<CommunityMember> findByCommunity(
            Community community
    );

    List<CommunityMember> findByCommunityAndStatus(
            Community community,
            MembershipStatus status
    );

    List<CommunityMember> findByUser(
            User user
    );

    List<CommunityMember> findByUserAndStatus(
            User user,
            MembershipStatus status
    );

    boolean existsByCommunityAndUser(
            Community community,
            User user
    );
}