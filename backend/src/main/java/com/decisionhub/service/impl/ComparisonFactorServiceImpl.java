package com.decisionhub.service.impl;

import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.ComparisonMapper;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.AuditService;
import com.decisionhub.service.ComparisonFactorService;
import com.decisionhub.validator.ComparisonFactorValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing comparison factors.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComparisonFactorServiceImpl implements ComparisonFactorService {

    private final DecisionBoardRepository decisionBoardRepository;
    private final ComparisonFactorRepository comparisonFactorRepository;
    private final UserRepository userRepository;
    
    private final ComparisonMapper comparisonMapper;
    private final ComparisonFactorValidator comparisonFactorValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public ComparisonFactorResponse createFactor(UUID decisionId, ComparisonFactorRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to create comparison factor on board: {}", decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageComparisonFactors(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage comparison factors for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 2. Business Validation
        comparisonFactorValidator.validateCreate(board, request);

        // 3. Map & Associate
        ComparisonFactor factor = comparisonMapper.toEntity(request);
        board.addComparisonFactor(factor);

        // 4. Save and Flush to obtain UUID ID instantly
        ComparisonFactor savedFactor = comparisonFactorRepository.saveAndFlush(factor);

        // 5. Audit Logging
        String newValueJson = String.format("{\"name\":\"%s\"}", savedFactor.getName());
        auditService.log(currentUser, "FACTOR_CREATED", "comparison_factors", savedFactor.getId(), null, newValueJson, ipAddress, userAgent);

        log.info("Comparison factor '{}' created successfully with ID '{}'", savedFactor.getName(), savedFactor.getId());
        return comparisonMapper.toResponse(savedFactor);
    }

    @Override
    @Transactional
    public ComparisonFactorResponse updateFactor(UUID decisionId, UUID factorId, ComparisonFactorRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to update comparison factor: {} on board: {}", factorId, decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageComparisonFactors(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage comparison factors for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        ComparisonFactor factor = getFactorOrThrow(factorId);

        // Verify factor belongs to the decision board
        if (!factor.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Comparison factor with ID " + factorId + " does not belong to decision board " + decisionId);
        }

        // 2. Business Validation
        comparisonFactorValidator.validateUpdate(board, factor, request);

        // 3. Keep old values for audit logging
        String oldValueJson = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", factor.getName(), factor.getDescription());

        // 4. Update
        factor.setName(request.name().trim());
        factor.setDescription(request.description());
        ComparisonFactor updatedFactor = comparisonFactorRepository.saveAndFlush(factor);

        // 5. Audit Logging
        String newValueJson = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", updatedFactor.getName(), updatedFactor.getDescription());
        auditService.log(currentUser, "FACTOR_UPDATED", "comparison_factors", factorId, oldValueJson, newValueJson, ipAddress, userAgent);

        log.info("Comparison factor '{}' updated successfully", updatedFactor.getName());
        return comparisonMapper.toResponse(updatedFactor);
    }

    @Override
    @Transactional
    public void deleteFactor(UUID decisionId, UUID factorId, String ipAddress, String userAgent) {
        log.info("Attempting to delete comparison factor: {} on board: {}", factorId, decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageComparisonFactors(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage comparison factors for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        ComparisonFactor factor = getFactorOrThrow(factorId);

        // Verify factor belongs to the decision board
        if (!factor.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Comparison factor with ID " + factorId + " does not belong to decision board " + decisionId);
        }

        // 2. Business Validation
        comparisonFactorValidator.validateDelete(board);

        // 3. Keep old values for audit logging
        String oldValueJson = String.format("{\"name\":\"%s\"}", factor.getName());

        // 4. Delete
        board.removeComparisonFactor(factor);
        comparisonFactorRepository.delete(factor);

        // 5. Audit Logging
        auditService.log(currentUser, "FACTOR_DELETED", "comparison_factors", factorId, oldValueJson, null, ipAddress, userAgent);

        log.info("Comparison factor with ID '{}' deleted successfully", factorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonFactorResponse> getFactorsByDecisionId(UUID decisionId) {
        log.info("Retrieving comparison factors for board: {}", decisionId);

        getActiveBoardOrThrow(decisionId);
        UUID currentUserId = authenticationFacade.getCurrentUserId().orElse(null);

        // Authorization to view decision details
        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to view decision board details");
        }

        return comparisonFactorRepository.findByDecisionId(decisionId).stream()
                .map(comparisonMapper::toResponse)
                .collect(Collectors.toList());
    }

    private DecisionBoard getActiveBoardOrThrow(UUID id) {
        DecisionBoard board = decisionBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found with ID: " + id));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("Decision board not found with ID: " + id);
        }
        return board;
    }

    private ComparisonFactor getFactorOrThrow(UUID id) {
        return comparisonFactorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comparison factor not found with ID: " + id));
    }

    private UUID getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }
}
