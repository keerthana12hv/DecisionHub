package com.decisionhub.validator.decision;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.exception.DecisionLockedException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.decision.DecisionRepository;
import org.springframework.stereotype.Component;

@Component
public class DecisionModificationValidator {

    private final DecisionRepository decisionRepository;

    public DecisionModificationValidator(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    // Default constructor for mockito test support
    protected DecisionModificationValidator() {
        this.decisionRepository = null;
    }

    public void validateDecisionUnlocked(Decision decision) {
        if (decision != null && decision.isLocked()) {
            throw new DecisionLockedException("This decision has been locked by a moderator.");
        }
    }

    public void validateDecisionUnlocked(Long decisionId) {
        if (decisionRepository == null) {
            throw new IllegalStateException("DecisionRepository is not initialized");
        }
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));
        validateDecisionUnlocked(decision);
    }
}
