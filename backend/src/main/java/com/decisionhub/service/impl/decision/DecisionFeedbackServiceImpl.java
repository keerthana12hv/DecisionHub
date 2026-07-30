package com.decisionhub.service.impl.decision;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.dto.request.decision.DecisionFeedbackRequest;
import com.decisionhub.dto.response.decision.DecisionFeedbackResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionFeedback;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.decision.DecisionFeedbackMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionFeedbackRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.service.interfaces.decision.DecisionFeedbackService;

import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.service.interfaces.audit.AuditService; 

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionFeedbackServiceImpl implements DecisionFeedbackService {

    private final DecisionFeedbackRepository decisionFeedbackRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final DecisionFeedbackMapper decisionFeedbackMapper;
    
    private final AuthenticationFacade authenticationFacade;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuditService auditService;

    private Long getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));
    }

    @Override
    @Transactional
    public DecisionFeedbackResponse submitFeedback(
            Long decisionId, 
            DecisionFeedbackRequest request, 
            String ipAddress, 
            String userAgent) {
        
        log.info("Attempting to submit feedback for decision ID: {}", decisionId);

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));

        if (!decisionAuthorizationService.canEditDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Only the decision creator can submit feedback.");
        }

        if (decision.getStatus() != DecisionStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted after the decision is CLOSED.");
        }

        if (decisionFeedbackRepository.findByDecision(decision).isPresent()) {
            throw new BadRequestException("Feedback has already been submitted for this decision.");
        }

        DecisionFeedback feedback = decisionFeedbackMapper.toEntity(request);
        feedback.setDecision(decision);
        feedback.setCreatedAt(LocalDateTime.now()); 

        DecisionFeedback savedFeedback = decisionFeedbackRepository.save(feedback);
        decisionFeedbackRepository.flush(); // Ensures ID is available for audit

        // Note: You will need to add "DECISION_FEEDBACK_SUBMITTED" to your AuditActionType enum 
        // to prevent it from defaulting to ADMIN_ACTION.
        auditService.log(
                currentUser,
                "DECISION_FEEDBACK_SUBMITTED",
                "decision_feedback",
                savedFeedback.getId(),
                null,
                "Rating: " + savedFeedback.getRating(),
                ipAddress,
                userAgent
        );

        log.info("Successfully saved feedback for decision ID: {}", decisionId);
        return decisionFeedbackMapper.toResponse(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionFeedbackResponse getFeedbackByDecision(Long decisionId) {
        log.info("Retrieving feedback for decision ID: {}", decisionId);

        Long currentUserId = getCurrentUserIdOrThrow();

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));

        if (!decisionAuthorizationService.canEditDecision(decisionId, currentUserId)) {
            throw new UnauthorizedActionException("Only the decision creator can view this feedback.");
        }

        DecisionFeedback feedback = decisionFeedbackRepository.findByDecision(decision)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found for decision ID: " + decisionId));

        return decisionFeedbackMapper.toResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionFeedbackResponse> getAllFeedback() {
        log.info("Retrieving all decision feedbacks for admin review");
        
        return decisionFeedbackRepository.findAll()
                .stream()
                .map(decisionFeedbackMapper::toResponse)
                .toList();
    }
}