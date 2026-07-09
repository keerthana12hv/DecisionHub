package com.decisionhub.validator;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.DecisionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator class enforcing business rules for the Decision Option module.
 */
@Component
@RequiredArgsConstructor
public class DecisionOptionValidator {

    private final DecisionOptionRepository decisionOptionRepository;

    /**
     * Validates Option creation within a decision.
     *
     * @param board The target decision.
     * @param dto   The option creation data.
     */
    public void validateCreate(Decision board, OptionCreateDto dto) {
        validateBoardStatus(board);
        
        String trimmedTitle = validateAndTrimTitle(dto.title());
        
        // Enforce option title uniqueness within the same decision
        boolean exists = decisionOptionRepository.existsByDecisionIdAndOptionNameIgnoreCase(
                board.getId(), trimmedTitle
        );
        if (exists) {
            throw new BadRequestException("An option with the title '" + trimmedTitle + "' already exists in this decision");
        }
    }

    /**
     * Validates Option updates.
     *
     * @param board          The target decision.
     * @param existingOption The option to be updated.
     * @param dto            The new option data.
     */
    public void validateUpdate(Decision board, DecisionOption existingOption, OptionCreateDto dto) {
        validateBoardStatus(board);

        String trimmedTitle = validateAndTrimTitle(dto.title());

        // Enforce option title uniqueness excluding the option being updated
        if (!existingOption.getOptionName().equalsIgnoreCase(trimmedTitle)) {
            boolean exists = decisionOptionRepository.existsByDecisionIdAndOptionNameIgnoreCase(
                    board.getId(), trimmedTitle
            );
            if (exists) {
                throw new BadRequestException("An option with the title '" + trimmedTitle + "' already exists in this decision");
            }
        }
    }

    /**
     * Validates Option deletions.
     *
     * @param board The target decision.
     */
    public void validateDelete(Decision board) {
        validateBoardStatus(board);

        // A decision must maintain a minimum of 2 options.
        List<DecisionOption> activeOptions = decisionOptionRepository.findByDecisionId(board.getId());
        if (activeOptions.size() <= 2) {
            throw new BadRequestException("Cannot delete option. A decision must have at least two options");
        }
    }

    private void validateBoardStatus(Decision board) {
        if (board.getStatus() != DecisionStatus.DRAFT) {
            throw new BadRequestException("Options can only be managed when the decision is in DRAFT status");
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
