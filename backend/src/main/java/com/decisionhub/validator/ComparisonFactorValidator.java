package com.decisionhub.validator;
 
import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.ComparisonFactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Validator class enforcing business rules for the Comparison Factor module.
 */
@Component
@RequiredArgsConstructor
public class ComparisonFactorValidator {

    private final ComparisonFactorRepository comparisonFactorRepository;

    /**
     * Validates factor creation within a decision board.
     *
     * @param board   The target decision board.
     * @param request The factor creation request data.
     */
    public void validateCreate(DecisionBoard board, ComparisonFactorRequest request) {
        validateBoardStatus(board);
        
        String trimmedName = validateAndTrimName(request.name());
        
        // Enforce factor name uniqueness within the same board
        boolean exists = comparisonFactorRepository.existsByDecisionIdAndNameIgnoreCase(board.getId(), trimmedName);
        if (exists) {
            throw new BadRequestException("A comparison factor with the name '" + trimmedName + "' already exists in this decision board");
        }
    }

    /**
     * Validates factor updates.
     *
     * @param board          The target decision board.
     * @param existingFactor The factor to be updated.
     * @param request        The new factor data.
     */
    public void validateUpdate(DecisionBoard board, ComparisonFactor existingFactor, ComparisonFactorRequest request) {
        validateBoardStatus(board);

        String trimmedName = validateAndTrimName(request.name());

        // Enforce factor name uniqueness excluding the factor being updated
        if (!existingFactor.getName().equalsIgnoreCase(trimmedName)) {
            boolean exists = comparisonFactorRepository.existsByDecisionIdAndNameIgnoreCase(board.getId(), trimmedName);
            if (exists) {
                throw new BadRequestException("A comparison factor with the name '" + trimmedName + "' already exists in this decision board");
            }
        }
    }

    /**
     * Validates factor deletions.
     *
     * @param board The target decision board.
     */
    public void validateDelete(DecisionBoard board) {
        validateBoardStatus(board);
    }

    private void validateBoardStatus(DecisionBoard board) {
        if (board.getStatus() != DecisionStatus.DRAFT) {
            throw new BadRequestException("Comparison factors can only be managed when the decision board is in DRAFT status");
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
