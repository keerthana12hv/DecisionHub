package com.decisionhub.service.impl.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.mapper.decision.ComparisonMapper;
import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.service.interfaces.decision.DecisionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionServiceImpl implements DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final ComparisonScoreRepository comparisonScoreRepository;
    
    private final DecisionMapper decisionMapper;
    private final ComparisonMapper comparisonMapper;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuthenticationFacade authenticationFacade;
    private final AuditService auditService;

    @Override
    @Transactional
    public DecisionResponse createDecision(DecisionRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to create decision: {}", request.title());

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 1. Validate community integration
        Community community = null;
        if (request.communityId() != null) {
            community = communityRepository.findById(request.communityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + request.communityId()));
            
            if (!decisionAuthorizationService.canCreateDecision(request.communityId(), currentUserId)) {
                throw new UnauthorizedActionException("Not authorized to create a decision in this community");
            }
        }

        // 2. Business Validation
        if (request.deadline() != null && request.deadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Deadline must be in the future");
        }

        // 3. Map & Associate
        Decision decision = decisionMapper.toEntity(request);
        decision.setCreator(currentUser);
        decision.setCommunity(community);
        decision.setStatus(DecisionStatus.DRAFT);
        if (request.communityId() == null) {
            decision.setVisibility(DecisionVisibility.PUBLIC);
        } else {
            decision.setVisibility(DecisionVisibility.COMMUNITY);
        }
        decision.setCreatedAt(LocalDateTime.now());

        // 4. Save
        Decision savedDecision = decisionRepository.save(decision);

        // Save options if provided in the creation request
        if (request.options() != null && !request.options().isEmpty()) {
            List<DecisionOption> optionsList = new java.util.ArrayList<>();
            for (OptionCreateDto optionDto : request.options()) {
                DecisionOption option = decisionMapper.toEntity(optionDto);
                option.setDecision(savedDecision);
                optionsList.add(decisionOptionRepository.save(option));
            }
            savedDecision.setOptions(optionsList);
        }

        // Save factors if provided in the creation request
        if (request.factors() != null && !request.factors().isEmpty()) {
            List<ComparisonFactor> factorsList = new java.util.ArrayList<>();
            for (ComparisonFactorRequest factorDto : request.factors()) {
                ComparisonFactor factor = comparisonMapper.toEntity(factorDto);
                factor.setDecision(savedDecision);
                if (factor.getWeight() == null) {
                    factor.setWeight(1);
                }
                factorsList.add(comparisonFactorRepository.save(factor));
            }
            savedDecision.setComparisonFactors(factorsList);
        }

        decisionRepository.flush();

        // 5. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\"}", savedDecision.getTitle());
        auditService.log(currentUser, "DECISION_CREATED", "decisions", savedDecision.getId(), null, newValueJson, ipAddress, userAgent);

        log.info("Decision '{}' created successfully with ID '{}'", savedDecision.getTitle(), savedDecision.getId());
        return decisionMapper.toResponse(savedDecision);
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(Long id) {
        log.info("Retrieving decision: {}", id);
        
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + id));

        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);
        if (!decisionAuthorizationService.canViewDecision(id, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view this decision");
        }

        return decisionMapper.toResponse(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionResponse> getAllDecisions(Long communityId, String status, Boolean mine) {
        log.info("Retrieving all decisions with filters: communityId={}, status={}, mine={}", communityId, status, mine);

        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);

        // Fetch all decisions
        List<Decision> decisions = decisionRepository.findAll();

        return decisions.stream()
                .filter(decision -> {
                    // Filter by community
                    if (communityId != null) {
                        if (decision.getCommunity() == null || !decision.getCommunity().getId().equals(communityId)) {
                            return false;
                        }
                    }
                    // Filter by status
                    if (status != null && !status.isEmpty()) {
                        if (!decision.getStatus().name().equalsIgnoreCase(status)) {
                            return false;
                        }
                    }
                    // Filter by mine (only decisions created by current user)
                    if (Boolean.TRUE.equals(mine)) {
                        if (currentUserId == null || !decision.getCreator().getId().equals(currentUserId)) {
                            return false;
                        }
                    }
                    // Filter by visibility/authorization (current user can view)
                    return decisionAuthorizationService.canViewDecision(decision.getId(), currentUserId);
                })
                .map(decisionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DecisionResponse updateDecision(Long id, DecisionRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to update decision: {}", id);

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + id));

        // 1. Authorization
        if (!decisionAuthorizationService.canEditDecision(id, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to edit this decision");
        }

        // 2. Validate community integration
        Community community = decision.getCommunity();
        if (request.communityId() != null && (community == null || !community.getId().equals(request.communityId()))) {
            community = communityRepository.findById(request.communityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + request.communityId()));
            
            if (!decisionAuthorizationService.canCreateDecision(request.communityId(), currentUserId)) {
                throw new UnauthorizedActionException("Not authorized to associate decision with this community");
            }
        } else if (request.communityId() == null) {
            community = null;
        }

        // 3. Business Validation
        if (request.deadline() != null && request.deadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Deadline must be in the future");
        }

        // 4. Update fields
        String oldValueJson = String.format("{\"title\":\"%s\"}", decision.getTitle());
        decision.setTitle(request.title().trim());
        decision.setDescription(request.description());
        decision.setCommunity(community);
        if (request.communityId() == null) {
            decision.setVisibility(DecisionVisibility.PUBLIC);
        } else {
            decision.setVisibility(DecisionVisibility.COMMUNITY);
        }
        decision.setDeadline(request.deadline());
        decision.setUpdatedAt(LocalDateTime.now());

        // 5. Save
        Decision updatedDecision = decisionRepository.save(decision);

        // 6. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\"}", updatedDecision.getTitle());
        auditService.log(currentUser, "DECISION_UPDATED", "decisions", id, oldValueJson, newValueJson, ipAddress, userAgent);

        log.info("Decision with ID '{}' updated successfully", id);
        return decisionMapper.toResponse(updatedDecision);
    }

    @Override
    @Transactional
    public void deleteDecision(Long id, String ipAddress, String userAgent) {
        log.info("Attempting to delete decision: {}", id);

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + id));

        // 1. Authorization
        if (!decisionAuthorizationService.canDeleteDecision(id, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to delete this decision");
        }

        // 2. Cascade delete dependent entities manually as cascade is not configured in DB/entity
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(id);
        comparisonScoreRepository.deleteAll(scores);

        List<ComparisonFactor> factors = comparisonFactorRepository.findByDecisionId(id);
        comparisonFactorRepository.deleteAll(factors);

        List<DecisionOption> options = decisionOptionRepository.findByDecisionId(id);
        decisionOptionRepository.deleteAll(options);

        // 3. Keep old value for audit log
        String oldValueJson = String.format("{\"title\":\"%s\"}", decision.getTitle());

        // 4. Delete decision
        decisionRepository.delete(decision);

        // 5. Audit Logging
        auditService.log(currentUser, "DECISION_DELETED", "decisions", id, oldValueJson, null, ipAddress, userAgent);

        log.info("Decision with ID '{}' deleted successfully", id);
    }

    private Long getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));
    }
}
