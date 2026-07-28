package com.decisionhub.service.impl.community;

import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.community.CommunityModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommunityModerationServiceImpl implements CommunityModerationService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;
    private final DecisionMapper decisionMapper;

    @Override
    public DecisionResponse pinDecision(Long decisionId) {
        log.info("Pinning decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setPinned(true);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse unpinDecision(Long decisionId) {
        log.info("Unpinning decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setPinned(false);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse lockDiscussion(Long decisionId) {
        log.info("Locking discussion on decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setLocked(true);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    @Override
    public DecisionResponse unlockDiscussion(Long decisionId) {
        log.info("Unlocking discussion on decision: {}", decisionId);
        Decision decision = getAndValidateDecision(decisionId);
        decision.setLocked(false);
        Decision saved = decisionRepository.save(decision);
        return decisionMapper.toResponse(saved);
    }

    private Decision getAndValidateDecision(Long decisionId) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));

        Community community = decision.getCommunity();
        if (community == null) {
            throw new BadRequestException("Decision does not belong to any community");
        }

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can perform this action");
        }

        return decision;
    }
}
