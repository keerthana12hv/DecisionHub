package com.decisionhub.security.decision;

import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link DecisionAuthorizationService} verifying security and permission boundaries.
 */
@Service
@RequiredArgsConstructor
public class DecisionAuthorizationServiceImpl implements DecisionAuthorizationService {

    private final DecisionRepository decisionRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateDecision(Long communityId, Long userId) {
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
    public boolean canViewDecision(Long decisionId, Long userId) {
        if (decisionId == null) {
            return false;
        }
        Optional<Decision> decisionOpt = decisionRepository.findById(decisionId);
        if (decisionOpt.isEmpty()) {
            return false;
        }

        Decision decision = decisionOpt.get();
        if (decision.getVisibility() == com.decisionhub.enums.decision.DecisionVisibility.PUBLIC) {
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
    public boolean canEditDecision(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteDecision(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canActivateDecision(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCloseDecision(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageOptions(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageComparisonFactors(Long decisionId, Long userId) {
        return isOwner(decisionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmitScore(Long decisionId, Long userId) {
        if (userId == null || decisionId == null) {
            return false;
        }
        return canViewDecision(decisionId, userId);
    }

    private boolean isOwner(Long decisionId, Long userId) {
        if (decisionId == null || userId == null) {
            return false;
        }
        return decisionRepository.findById(decisionId)
                .map(decision -> decision.getCreator().getId().equals(userId))
                .orElse(false);
    }

    private boolean isUserActiveCommunityMember(Long communityId, Long userId) {
        return communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .map(member -> member.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);
    }
}
