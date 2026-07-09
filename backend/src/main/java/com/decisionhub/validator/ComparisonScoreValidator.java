package com.decisionhub.validator;

import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import org.springframework.stereotype.Component;

/**
 * Validator class enforcing business rules for the Comparison Score module.
 */
@Component
public class ComparisonScoreValidator {

    /**
     * Validates a comparison score submission.
     *
     * @param board   The parent decision.
     * @param option  The decision option being scored.
     * @param factor  The comparison factor being scored.
     * @param request The score request data.
     */
    public void validateSubmit(Decision board, DecisionOption option, ComparisonFactor factor, ComparisonScoreRequest request) {
        // 1. Decision State Validation
        if (board.getStatus() != DecisionStatus.ACTIVE) {
            throw new BadRequestException("Scores can only be submitted or updated when the decision is in ACTIVE status");
        }

        // 2. Score Range Validation
        if (request.score() < 0 || request.score() > 100) {
            throw new BadRequestException("Score must be between 0 and 100 inclusive");
        }

        // 3. Option Association Validation
        if (!option.getDecision().getId().equals(board.getId())) {
            throw new BadRequestException("Option with ID " + option.getId() + " does not belong to decision " + board.getId());
        }

        // 4. Factor Association Validation
        if (!factor.getDecision().getId().equals(board.getId())) {
            throw new BadRequestException("Comparison factor with ID " + factor.getId() + " does not belong to decision " + board.getId());
        }
    }
}
