package com.decisionhub.service.impl.decision;

import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.decision.ComparisonScoreResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.ComparisonMapper;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.service.interfaces.decision.ComparisonScoreService;
import com.decisionhub.validator.decision.ComparisonScoreValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.decisionhub.event.ComparisonScoreChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing comparison scores.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComparisonScoreServiceImpl implements ComparisonScoreService {

    private final DecisionRepository decisionRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final ComparisonScoreRepository comparisonScoreRepository;
    private final UserRepository userRepository;

    private final ComparisonMapper comparisonMapper;
    private final ComparisonScoreValidator comparisonScoreValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ComparisonScoreResponse submitScore(Long decisionId, ComparisonScoreRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to submit comparison score on board: {}", decisionId);

        Decision board = getActiveBoardOrThrow(decisionId);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canSubmitScore(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to submit scores for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        DecisionOption option = decisionOptionRepository.findById(request.optionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with ID: " + request.optionId()));

        ComparisonFactor factor = comparisonFactorRepository.findById(request.factorId())
                .orElseThrow(() -> new ResourceNotFoundException("Comparison factor not found with ID: " + request.factorId()));

        // 2. Business Validation
        comparisonScoreValidator.validateSubmit(board, option, factor, request);

        // 3. Check for existing score to perform Upsert
        Optional<ComparisonScore> existingScoreOpt = comparisonScoreRepository.findById(
                new ComparisonScoreId(request.optionId(), request.factorId(), currentUserId)
        );

        ComparisonScore savedScore;
        boolean isUpdate = existingScoreOpt.isPresent();
        if (isUpdate) {
            ComparisonScore scoreEntity = existingScoreOpt.get();
            String oldValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", scoreEntity.getScore(), scoreEntity.getRemarks());

            // Update fields
            scoreEntity.setScore(request.score());
            scoreEntity.setRemarks(request.remarks());
            savedScore = comparisonScoreRepository.saveAndFlush(scoreEntity);

            // Audit Log
            String newValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", savedScore.getScore(), savedScore.getRemarks());
            auditService.log(currentUser, "SCORE_UPDATED", "comparison_scores", option.getId(), oldValueJson, newValueJson, ipAddress, userAgent);
            log.info("Comparison score updated successfully for option: {} and factor: {}", request.optionId(), request.factorId());
        } else {
            // Create new
            ComparisonScore scoreEntity = comparisonMapper.toEntity(request);
            scoreEntity.setOption(option);
            scoreEntity.setFactor(factor);
            scoreEntity.setUser(currentUser);

            savedScore = comparisonScoreRepository.saveAndFlush(scoreEntity);

            // Audit Log
            String newValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", savedScore.getScore(), savedScore.getRemarks());
            auditService.log(currentUser, "SCORE_CREATED", "comparison_scores", option.getId(), null, newValueJson, ipAddress, userAgent);
            log.info("Comparison score created successfully for option: {} and factor: {}", request.optionId(), request.factorId());
        }

        eventPublisher.publishEvent(new ComparisonScoreChangedEvent(this, decisionId, currentUserId, isUpdate ? "SCORE_UPDATED" : "SCORE_CREATED"));

        return comparisonMapper.toResponse(savedScore);
    }

    @Override
    @Transactional
    public ComparisonScoreResponse updateScore(Long decisionId, String scoreId, ComparisonScoreRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to update comparison score on board: {}", decisionId);

        Decision board = getActiveBoardOrThrow(decisionId);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canSubmitScore(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to manage scores for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 2. Fetch existing score or throw ResourceNotFoundException
        ComparisonScore scoreEntity = comparisonScoreRepository.findById(
                new ComparisonScoreId(request.optionId(), request.factorId(), currentUserId)
        ).orElseThrow(() -> new ResourceNotFoundException("Comparison score not found for the specified Option and Factor by this user"));

        DecisionOption option = decisionOptionRepository.findById(request.optionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with ID: " + request.optionId()));

        ComparisonFactor factor = comparisonFactorRepository.findById(request.factorId())
                .orElseThrow(() -> new ResourceNotFoundException("Comparison factor not found with ID: " + request.factorId()));

        // 3. Business Validation
        comparisonScoreValidator.validateSubmit(board, option, factor, request);

        // 4. Update
        String oldValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", scoreEntity.getScore(), scoreEntity.getRemarks());
        scoreEntity.setScore(request.score());
        scoreEntity.setRemarks(request.remarks());
        ComparisonScore savedScore = comparisonScoreRepository.saveAndFlush(scoreEntity);

        // Audit Log
        String newValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", savedScore.getScore(), savedScore.getRemarks());
        auditService.log(currentUser, "SCORE_UPDATED", "comparison_scores", option.getId(), oldValueJson, newValueJson, ipAddress, userAgent);

        eventPublisher.publishEvent(new ComparisonScoreChangedEvent(this, decisionId, currentUserId, "SCORE_UPDATED"));

        log.info("Comparison score updated successfully via PUT for option: {} and factor: {}", request.optionId(), request.factorId());
        return comparisonMapper.toResponse(savedScore);
    }

    @Override
    @Transactional
    public void deleteScore(Long decisionId, Long optionId, Long factorId, String ipAddress, String userAgent) {
        log.info("Attempting to delete comparison score on board: {} for option: {} and factor: {}", decisionId, optionId, factorId);

        getActiveBoardOrThrow(decisionId);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canSubmitScore(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to manage scores for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 2. Fetch existing score or throw ResourceNotFoundException
        ComparisonScore scoreEntity = comparisonScoreRepository.findById(
                new ComparisonScoreId(optionId, factorId, currentUserId)
        ).orElseThrow(() -> new ResourceNotFoundException("Comparison score not found for the specified Option and Factor by this user"));

        // 3. Delete
        String oldValueJson = String.format("{\"score\":%d,\"remarks\":\"%s\"}", scoreEntity.getScore(), scoreEntity.getRemarks());
        comparisonScoreRepository.delete(scoreEntity);
        comparisonScoreRepository.flush();

        // Audit Log
        auditService.log(currentUser, "SCORE_DELETED", "comparison_scores", optionId, oldValueJson, null, ipAddress, userAgent);

        eventPublisher.publishEvent(new ComparisonScoreChangedEvent(this, decisionId, currentUserId, "SCORE_DELETED"));

        log.info("Comparison score deleted successfully for option: {} and factor: {}", optionId, factorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonScoreResponse> getScoresByDecisionId(Long decisionId) {
        log.info("Retrieving all comparison scores for board: {}", decisionId);

        getActiveBoardOrThrow(decisionId);
        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);

        // Authorization to view decision details
        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view decision details");
        }

        // TODO: Future Sprint 7 (Voting/Score Anonymity rules)
        // Future rules:
        // - owner-only visibility: only the decision creator/owner can view individual scores.
        // - anonymous visibility: score submitters are anonymized (user details hidden/stripped).
        // - admin visibility: administrators can always see all scores and submitters.

        return comparisonScoreRepository.findByOptionDecisionId(decisionId).stream()
                .map(comparisonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonScoreResponse> getMyScoresByDecisionId(Long decisionId) {
        log.info("Retrieving current user's comparison scores for board: {}", decisionId);

        getActiveBoardOrThrow(decisionId);
        Long currentUserId = getCurrentUserIdOrThrow();

        // Authorization to view decision details
        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view decision details");
        }

        return comparisonScoreRepository.findByOptionDecisionIdAndUserId(decisionId, currentUserId).stream()
                .map(comparisonMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Decision getActiveBoardOrThrow(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + id));
    }

    private Long getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));
    }
}
