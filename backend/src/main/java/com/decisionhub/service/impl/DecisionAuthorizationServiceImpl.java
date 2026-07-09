package com.decisionhub.security;

import com.decisionhub.entity.CommunityMember;
import com.decisionhub.entity.CommunityMemberId;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.repository.CommunityMemberRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link DecisionAuthorizationService} verifying security and permission boundaries.
 */
@Service
@RequiredArgsConstructor
public class DecisionAuthorizationServiceImpl implements DecisionAuthorizationService {

    private final DecisionBoardRepository decisionBoardRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateDecision(UUID communityId, UUID userId) {
        if (userId == null) {
            return false;
        }
        if (communityId == null) {
            // Standalone decisions can be created by any authenticated user
            return true;
        }
        // Community decisions can only be created by active community members
        return isUserActiveCommunityMember(communityId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canViewDecision(UUID decisionId, UUID userId) {
        if (decisionId == null) {
            return false;
        }
        Optional<DecisionBoard> decisionOpt = decisionBoardRepository.findById(decisionId);
        if (decisionOpt.isEmpty()) {
            return false;
        }

        DecisionBoard decision = decisionOpt.get();
        if (decision.isPublic()) {
            return true;
        }

        // Private decision checks
        if (userId == null) {
            return false;
        }

        // Creator can always view
        if (decision.getCreator().getId().equals(userId)) {
            return true;
        }

        // If it's a private community decision, active community members can view
        if (decision.getCommunity() != null) {
            return isUserActiveCommunityMember(decision.getCommunity().getId(), userId);
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditDecision(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteDecision(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canActivateDecision(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCloseDecision(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageOptions(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageComparisonFactors(UUID decisionId, UUID userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmitScore(UUID decisionId, UUID userId) {
        if (userId == null || decisionId == null) {
            return false;
        }
        return canViewDecision(decisionId, userId);
    }

    private boolean isOwner(UUID decisionId, UUID userId) {
        if (decisionId == null || userId == null) {
            return false;
        }
        return decisionBoardRepository.findById(decisionId)
                .map(decision -> decision.getCreator().getId().equals(userId))
                .orElse(false);
    }

    private boolean isUserActiveCommunityMember(UUID communityId, UUID userId) {
        CommunityMemberId memberId = new CommunityMemberId(communityId, userId);
        return communityMemberRepository.findById(memberId)
                .map(member -> member.getStatus() == CommunityStatus.ACTIVE)
                .orElse(false);
    }
}
