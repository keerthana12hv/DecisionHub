package com.decisionhub.service.impl.community;

import com.decisionhub.dto.request.community.CommunityRuleRequest;
import com.decisionhub.dto.response.community.CommunityRuleResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityRule;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.community.CommunityRuleMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.community.CommunityRuleRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.community.CommunityRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommunityRuleServiceImpl implements CommunityRuleService {

    private final CommunityRuleRepository communityRuleRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public CommunityRuleResponse createRule(Long communityId, CommunityRuleRequest request) {
        log.info("Creating rule for community ID: {}", communityId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + communityId));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found with ID: " + communityId);
        }

        validateModerator(community);

        CommunityRule rule = new CommunityRule();
        rule.setCommunity(community);
        rule.setTitle(request.title().trim());
        rule.setDescription(request.description().trim());

        CommunityRule saved = communityRuleRepository.save(rule);
        return CommunityRuleMapper.toResponse(saved);
    }

    @Override
    public CommunityRuleResponse updateRule(Long ruleId, CommunityRuleRequest request) {
        log.info("Updating rule ID: {}", ruleId);
        CommunityRule rule = communityRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with ID: " + ruleId));

        Community community = rule.getCommunity();
        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found");
        }

        validateModerator(community);

        rule.setTitle(request.title().trim());
        rule.setDescription(request.description().trim());

        CommunityRule saved = communityRuleRepository.save(rule);
        return CommunityRuleMapper.toResponse(saved);
    }

    @Override
    public void deleteRule(Long ruleId) {
        log.info("Deleting rule ID: {}", ruleId);
        CommunityRule rule = communityRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with ID: " + ruleId));

        Community community = rule.getCommunity();
        validateModerator(community);

        communityRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityRuleResponse> getRulesByCommunity(Long communityId) {
        log.info("Fetching rules for community ID: {}", communityId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + communityId));

        if (community.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Community not found with ID: " + communityId);
        }

        return communityRuleRepository.findByCommunityId(communityId)
                .stream()
                .map(CommunityRuleMapper::toResponse)
                .toList();
    }

    private void validateModerator(Community community) {
        Long currentUserId = authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User must be authenticated"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!community.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the community moderator can perform this action");
        }
    }
}
