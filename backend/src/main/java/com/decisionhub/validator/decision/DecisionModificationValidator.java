package com.decisionhub.validator.decision;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.DecisionClosedException;
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

    /**
     * Validates that a decision is editable (neither closed nor locked).
     * Enforces the validation precedence:
     * 1. Closed status check (throws DecisionClosedException)
     * 2. Locked status check (throws DecisionLockedException)
     */
    public void validateDecisionEditable(Decision decision) {
        if (decision == null) {
            return;
        }
        // Precedence 1: Check if closed
        if (decision.getStatus() == DecisionStatus.CLOSED) {
            throw new DecisionClosedException("This decision has been closed.");
        }
        // Precedence 2: Check if locked
        if (decision.isLocked()) {
            throw new DecisionLockedException("This decision has been locked by a moderator.");
        }
    }

    /**
     * Overloaded method resolving the decision by ID first.
     */
    public void validateDecisionEditable(Long decisionId) {
        if (decisionRepository == null) {
            throw new IllegalStateException("DecisionRepository is not initialized");
        }
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found with ID: " + decisionId));
        validateDecisionEditable(decision);
    }

    /**
     * Reusable lifecycle validation hook for future modules (e.g., Poll, Vote, Comments).
     * Verifies that the decision is open and not locked.
     */
    public void validateDecisionLifecycleForWrite(Long decisionId) {
        validateDecisionEditable(decisionId);
    }
}
