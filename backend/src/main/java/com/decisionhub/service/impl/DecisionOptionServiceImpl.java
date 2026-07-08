package com.decisionhub.service.impl;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.DecisionMapper;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.security.DecisionAuthorizationService;
import com.decisionhub.service.AuditService;
import com.decisionhub.service.DecisionOptionService;
import com.decisionhub.validator.DecisionOptionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for managing decision options.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionOptionServiceImpl implements DecisionOptionService {

    private final DecisionBoardRepository decisionBoardRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final UserRepository userRepository;
    
    private final DecisionMapper decisionMapper;
    private final DecisionOptionValidator decisionOptionValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public OptionResponseDto createOption(UUID decisionId, OptionCreateDto dto, String ipAddress, String userAgent) {
        log.info("Attempting to create decision option on board: {}", decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage options for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 2. Business Validation
        decisionOptionValidator.validateCreate(board, dto);

        // 3. Map & Associate Option
        DecisionOption option = decisionMapper.toEntity(dto);
        board.addOption(option);

        // 4. Save
        DecisionOption savedOption = decisionOptionRepository.saveAndFlush(option);

        // 5. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\"}", savedOption.getTitle());
        auditService.log(currentUser, "OPTION_CREATED", "decision_options", savedOption.getId(), null, newValueJson, ipAddress, userAgent);

        log.info("Option '{}' created successfully with ID '{}'", savedOption.getTitle(), savedOption.getId());
        return decisionMapper.toResponseDto(savedOption);
    }

    @Override
    @Transactional
    public OptionResponseDto updateOption(UUID decisionId, UUID optionId, OptionCreateDto dto, String ipAddress, String userAgent) {
        log.info("Attempting to update option: {} on board: {}", optionId, decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage options for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        DecisionOption option = getActiveOptionOrThrow(optionId);
        
        // Verify option belongs to the decision board
        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Option with ID " + optionId + " does not belong to decision board " + decisionId);
        }

        // 2. Business Validation
        decisionOptionValidator.validateUpdate(board, option, dto);

        // 3. Keep old values for audit log
        String oldValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", option.getTitle(), option.getDescription());

        // 4. Update
        option.setTitle(dto.title().trim());
        option.setDescription(dto.description());
        DecisionOption updatedOption = decisionOptionRepository.save(option);

        // 5. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", updatedOption.getTitle(), updatedOption.getDescription());
        auditService.log(currentUser, "OPTION_UPDATED", "decision_options", optionId, oldValueJson, newValueJson, ipAddress, userAgent);

        log.info("Option '{}' updated successfully", updatedOption.getTitle());
        return decisionMapper.toResponseDto(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOption(UUID decisionId, UUID optionId, String ipAddress, String userAgent) {
        log.info("Attempting to delete option: {} on board: {}", optionId, decisionId);

        DecisionBoard board = getActiveBoardOrThrow(decisionId);
        UUID currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new ForbiddenException("Not authorized to manage options for this decision board");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        DecisionOption option = getActiveOptionOrThrow(optionId);

        // Verify option belongs to the decision board
        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Option with ID " + optionId + " does not belong to decision board " + decisionId);
        }

        // 2. Business Validation
        decisionOptionValidator.validateDelete(board);

        // 3. Keep old value for audit
        String oldValueJson = String.format("{\"title\":\"%s\"}", option.getTitle());

        // 4. Soft delete
        board.removeOption(option);
        decisionOptionRepository.delete(option);

        // 5. Audit Logging
        auditService.log(currentUser, "OPTION_DELETED", "decision_options", optionId, oldValueJson, null, ipAddress, userAgent);

        log.info("Option with ID '{}' deleted successfully", optionId);
    }

    private DecisionBoard getActiveBoardOrThrow(UUID decisionId) {
        DecisionBoard board = decisionBoardRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found with ID: " + decisionId));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("Decision board not found with ID: " + decisionId);
        }
        return board;
    }

    private DecisionOption getActiveOptionOrThrow(UUID optionId) {
        DecisionOption option = decisionOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with ID: " + optionId));
        if (option.isDeleted()) {
            throw new ResourceNotFoundException("Option not found with ID: " + optionId);
        }
        return option;
    }

    private UUID getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }
}
