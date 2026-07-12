package com.decisionhub.validator.decision;
 
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.decision.ComparisonFactorRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator class enforcing business rules for the Comparison Factor module.
 */
@Component
@RequiredArgsConstructor
public class ComparisonFactorValidator {

    private final ComparisonFactorRepository comparisonFactorRepository;

    /**
     * Validates factor creation within a decision.
     *
     * @param board   The target decision.
     * @param request The factor creation request data.
     */
    public void validateCreate(Decision board, ComparisonFactorRequest request) {
        validateBoardStatus(board);
        
        String trimmedName = validateAndTrimName(request.name());
        
        // Enforce factor name uniqueness within the same board
        boolean exists = comparisonFactorRepository.existsByDecisionIdAndNameIgnoreCase(board.getId(), trimmedName);
        if (exists) {
            throw new BadRequestException("A comparison factor with the name '" + trimmedName + "' already exists in this decision");
        }
    }

    /**
     * Validates factor updates.
     *
     * @param board          The target decision.
     * @param existingFactor The factor to be updated.
     * @param request        The new factor data.
     */
    public void validateUpdate(Decision board, ComparisonFactor existingFactor, ComparisonFactorRequest request) {
        validateBoardStatus(board);

        String trimmedName = validateAndTrimName(request.name());

        // Enforce factor name uniqueness excluding the factor being updated
        if (!existingFactor.getName().equalsIgnoreCase(trimmedName)) {
            boolean exists = comparisonFactorRepository.existsByDecisionIdAndNameIgnoreCase(board.getId(), trimmedName);
            if (exists) {
                throw new BadRequestException("A comparison factor with the name '" + trimmedName + "' already exists in this decision");
            }
        }
    }

    /**
     * Validates factor deletions.
     *
     * @param board The target decision.
     */
    public void validateDelete(Decision board) {
        validateBoardStatus(board);
    }

    private void validateBoardStatus(Decision board) {
        if (board.getStatus() != DecisionStatus.DRAFT) {
            throw new BadRequestException("Comparison factors can only be managed when the decision is in DRAFT status");
        }
    }

    private String validateAndTrimName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Comparison factor name is required and cannot be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 100) {
            throw new BadRequestException("Comparison factor name must be less than or equal to 100 characters");
        }
        return trimmed;
    }
}
