package com.decisionhub.validator;

import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.DecisionStatus;
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
     * @param board   The parent decision board.
     * @param option  The decision option being scored.
     * @param factor  The comparison factor being scored.
     * @param request The score request data.
     */
    public void validateSubmit(DecisionBoard board, DecisionOption option, ComparisonFactor factor, ComparisonScoreRequest request) {
        // 1. Decision State Validation
        if (board.getStatus() != DecisionStatus.ACTIVE) {
            throw new BadRequestException("Scores can only be submitted or updated when the decision board is in ACTIVE status");
        }

        if (board.isDeleted()) {
            throw new BadRequestException("Decision board is soft-deleted and cannot accept scores");
        }

        // 2. Score Range Validation
        if (request.score() < 0 || request.score() > 100) {
            throw new BadRequestException("Score must be between 0 and 100 inclusive");
        }

        // 3. Option Association Validation
        if (option.isDeleted()) {
            throw new BadRequestException("Selected option is soft-deleted");
        }
        if (!option.getDecision().getId().equals(board.getId())) {
            throw new BadRequestException("Option with ID " + option.getId() + " does not belong to decision board " + board.getId());
        }

        // 4. Factor Association Validation
        if (!factor.getDecision().getId().equals(board.getId())) {
            throw new BadRequestException("Comparison factor with ID " + factor.getId() + " does not belong to decision board " + board.getId());
        }
    }
}
