package com.decisionhub.service.impl.decision;

import com.decisionhub.dto.request.decision.OptionCreateDto;
import com.decisionhub.dto.response.decision.OptionResponseDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.service.interfaces.decision.DecisionOptionService;
import com.decisionhub.validator.decision.DecisionOptionValidator;
import com.decisionhub.validator.decision.DecisionModificationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing decision options.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionOptionServiceImpl implements DecisionOptionService {

    private final DecisionRepository decisionRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final UserRepository userRepository;
    
    private final DecisionMapper decisionMapper;
    private final DecisionOptionValidator decisionOptionValidator;
    private final DecisionModificationValidator decisionModificationValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public OptionResponseDto createOption(Long decisionId, OptionCreateDto dto, String ipAddress, String userAgent) {
        log.info("Attempting to create decision option on board: {}", decisionId);

        Decision board = getActiveBoardOrThrow(decisionId);
        decisionModificationValidator.validateDecisionEditable(board);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to manage options for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        // 2. Business Validation
        decisionOptionValidator.validateCreate(board, dto);

        // 3. Map & Associate Option
        DecisionOption option = decisionMapper.toEntity(dto);
        option.setDecision(board);

        // 4. Save
        DecisionOption savedOption = decisionOptionRepository.saveAndFlush(option);

        // 5. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\"}", savedOption.getOptionName());
        auditService.log(currentUser, "OPTION_CREATED", "decision_options", savedOption.getId(), null, newValueJson, ipAddress, userAgent);

        log.info("Option '{}' created successfully with ID '{}'", savedOption.getOptionName(), savedOption.getId());
        return decisionMapper.toResponseDto(savedOption);
    }

    @Override
    @Transactional
    public OptionResponseDto updateOption(Long decisionId, Long optionId, OptionCreateDto dto, String ipAddress, String userAgent) {
        log.info("Attempting to update option: {} on board: {}", optionId, decisionId);

        Decision board = getActiveBoardOrThrow(decisionId);
        decisionModificationValidator.validateDecisionEditable(board);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to manage options for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        DecisionOption option = getActiveOptionOrThrow(optionId);
        
        // Verify option belongs to the decision
        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Option with ID " + optionId + " does not belong to decision " + decisionId);
        }

        // 2. Business Validation
        decisionOptionValidator.validateUpdate(board, option, dto);

        // 3. Keep old values for audit log
        String oldValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", option.getOptionName(), option.getDescription());

        // 4. Update
        option.setOptionName(dto.title().trim());
        option.setDescription(dto.description());
        DecisionOption updatedOption = decisionOptionRepository.save(option);

        // 5. Audit Logging
        String newValueJson = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", updatedOption.getOptionName(), updatedOption.getDescription());
        auditService.log(currentUser, "OPTION_UPDATED", "decision_options", optionId, oldValueJson, newValueJson, ipAddress, userAgent);

        log.info("Option '{}' updated successfully", updatedOption.getOptionName());
        return decisionMapper.toResponseDto(updatedOption);
    }

    @Override
    @Transactional
    public void deleteOption(Long decisionId, Long optionId, String ipAddress, String userAgent) {
        log.info("Attempting to delete option: {} on board: {}", optionId, decisionId);

        Decision board = getActiveBoardOrThrow(decisionId);
        decisionModificationValidator.validateDecisionEditable(board);
        Long currentUserId = getCurrentUserIdOrThrow();

        // 1. Authorization
        if (!decisionAuthorizationService.canManageOptions(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to manage options for this decision");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        DecisionOption option = getActiveOptionOrThrow(optionId);

        // Verify option belongs to the decision
        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Option with ID " + optionId + " does not belong to decision " + decisionId);
        }

        // 2. Business Validation
        decisionOptionValidator.validateDelete(board);

        // 3. Keep old value for audit
        String oldValueJson = String.format("{\"title\":\"%s\"}", option.getOptionName());

        // 4. Delete
        decisionOptionRepository.delete(option);

        // 5. Audit Logging
        auditService.log(currentUser, "OPTION_DELETED", "decision_options", optionId, oldValueJson, null, ipAddress, userAgent);

        log.info("Option with ID '{}' deleted successfully", optionId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<OptionResponseDto> getOptions(Long decisionId) {
        log.info("Retrieving decision options for board: {}", decisionId);

        getActiveBoardOrThrow(decisionId);
        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);

        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view decision details");
        }

        return decisionOptionRepository.findByDecisionId(decisionId).stream()
                .map(decisionMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OptionResponseDto getOption(Long decisionId, Long optionId) {
        log.info("Retrieving decision option: {} for board: {}", optionId, decisionId);

        getActiveBoardOrThrow(decisionId);
        Long currentUserId = authenticationFacade.getCurrentUserId().orElse(null);

        if (!decisionAuthorizationService.canViewDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Not authorized to view decision details");
        }

        DecisionOption option = getActiveOptionOrThrow(optionId);
        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException("Option with ID " + optionId + " does not belong to decision " + decisionId);
        }

        return decisionMapper.toResponseDto(option);
    }

    private Decision getActiveBoardOrThrow(Long decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));
    }

    private DecisionOption getActiveOptionOrThrow(Long optionId) {
        return decisionOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with ID: " + optionId));
    }

    private Long getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));
    }
}
