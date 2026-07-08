package com.decisionhub.validator;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.DecisionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Validator class enforcing business rules for the Decision Option module.
 */
@Component
@RequiredArgsConstructor
public class DecisionOptionValidator {

    private final DecisionOptionRepository decisionOptionRepository;

    /**
     * Validates Option creation within a decision board.
     *
     * @param board The target decision board.
     * @param dto   The option creation data.
     */
    public void validateCreate(DecisionBoard board, OptionCreateDto dto) {
        validateBoardStatus(board);
        
        String trimmedTitle = validateAndTrimTitle(dto.title());
        
        // Enforce option title uniqueness within the same board
        boolean exists = decisionOptionRepository.existsByDecisionIdAndTitleIgnoreCaseAndDeletedAtIsNull(
                board.getId(), trimmedTitle
        );
        if (exists) {
            throw new BadRequestException("An option with the title '" + trimmedTitle + "' already exists in this decision board");
        }
    }

    /**
     * Validates Option updates.
     *
     * @param board          The target decision board.
     * @param existingOption The option to be updated.
     * @param dto            The new option data.
     */
    public void validateUpdate(DecisionBoard board, DecisionOption existingOption, OptionCreateDto dto) {
        validateBoardStatus(board);

        String trimmedTitle = validateAndTrimTitle(dto.title());

        // Enforce option title uniqueness excluding the option being updated
        if (!existingOption.getTitle().equalsIgnoreCase(trimmedTitle)) {
            boolean exists = decisionOptionRepository.existsByDecisionIdAndTitleIgnoreCaseAndDeletedAtIsNull(
                    board.getId(), trimmedTitle
            );
            if (exists) {
                throw new BadRequestException("An option with the title '" + trimmedTitle + "' already exists in this decision board");
            }
        }
    }

    /**
     * Validates Option deletions.
     *
     * @param board The target decision board.
     */
    public void validateDelete(DecisionBoard board) {
        validateBoardStatus(board);

        // A decision board must maintain a minimum of 2 active options.
        List<DecisionOption> activeOptions = decisionOptionRepository.findByDecisionIdAndDeletedAtIsNull(board.getId());
        if (activeOptions.size() <= 2) {
            throw new BadRequestException("Cannot delete option. A decision board must have at least two active options");
        }
    }

    private void validateBoardStatus(DecisionBoard board) {
        if (board.getStatus() != DecisionStatus.DRAFT) {
            throw new BadRequestException("Options can only be managed when the decision board is in DRAFT status");
        }
    }

    private String validateAndTrimTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("Option title is required and cannot be blank");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 150) {
            throw new BadRequestException("Option title must be less than or equal to 150 characters");
        }
        return trimmed;
    }
}
