package com.decisionhub.validator.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.VotingType;
import com.decisionhub.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validator class enforcing business rules for the Decision module.
 */
@Component
public class DecisionValidator {

    /**
     * Validates decision creation request.
     */
    public void validateCreate(DecisionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        if (request.votingEndTime().isBefore(now)) {
            throw new BadRequestException("Voting end time must be in the future");
        }

        if (request.deadline().isBefore(now)) {
            throw new BadRequestException("Decision deadline must be in the future");
        }

        if (!request.votingEndTime().isBefore(request.deadline())) {
            throw new BadRequestException("Voting end time must be strictly before the decision deadline");
        }

        if (request.votingType() != VotingType.RATING_BASED && request.factors() != null && !request.factors().isEmpty()) {
            throw new BadRequestException("Comparison factors are not allowed for " + request.votingType() + " decisions");
        }
    }

    /**
     * Validates decision updates, ensuring locked configuration isn't modified after publishing.
     */
    public void validateUpdate(Decision decision, DecisionRequest request) {
        if (decision.getStatus() != DecisionStatus.DRAFT) {
            // Configuration locking rules after publishing
            if (request.votingType() != decision.getVotingType()) {
                throw new BadRequestException("Voting type cannot be modified after the decision is published");
            }
            if (!request.votingEndTime().isEqual(decision.getVotingEndTime())) {
                throw new BadRequestException("Voting end time cannot be modified after the decision is published");
            }
            if (!request.deadline().isEqual(decision.getDeadline())) {
                throw new BadRequestException("Decision deadline cannot be modified after the decision is published");
            }
        } else {
            // Draft phase validations
            LocalDateTime now = LocalDateTime.now();

            if (request.votingEndTime().isBefore(now)) {
                throw new BadRequestException("Voting end time must be in the future");
            }

            if (request.deadline().isBefore(now)) {
                throw new BadRequestException("Decision deadline must be in the future");
            }

            if (!request.votingEndTime().isBefore(request.deadline())) {
                throw new BadRequestException("Voting end time must be strictly before the decision deadline");
            }

            if (request.votingType() != VotingType.RATING_BASED && request.factors() != null && !request.factors().isEmpty()) {
                throw new BadRequestException("Comparison factors are not allowed for " + request.votingType() + " decisions");
            }
        }
    }

    /**
     * Validates the publishing checklist for a decision.
     */
    public void validatePublish(Decision decision, java.util.List<com.decisionhub.entity.decision.DecisionOption> options, java.util.List<com.decisionhub.entity.decision.ComparisonFactor> factors) {
        if (decision.getStatus() != DecisionStatus.DRAFT) {
            throw new BadRequestException("Only decisions in DRAFT status can be published");
        }

        LocalDateTime now = LocalDateTime.now();

        if (decision.getVotingEndTime().isBefore(now)) {
            throw new BadRequestException("Cannot publish decision with a voting end time in the past");
        }

        if (decision.getDeadline().isBefore(now)) {
            throw new BadRequestException("Cannot publish decision with a decision deadline in the past");
        }

        if (!decision.getVotingEndTime().isBefore(decision.getDeadline())) {
            throw new BadRequestException("Voting end time must be strictly before the decision deadline");
        }

        // Rule 5: Decision must contain at least 2 options before publishing.
        if (options == null || options.size() < 2) {
            throw new BadRequestException("Cannot publish decision. At least 2 options are required");
        }

        // Rule 6: Comparison Factors required ONLY when VotingType == RATING_BASED
        if (decision.getVotingType() == VotingType.RATING_BASED) {
            if (factors == null || factors.isEmpty()) {
                throw new BadRequestException("Comparison factors are required for rating-based decisions");
            }
        } else {
            // Rule 7: SINGLE_CHOICE, MULTIPLE_CHOICE must publish successfully without Comparison Factors.
            if (factors != null && !factors.isEmpty()) {
                throw new BadRequestException("Comparison factors are not allowed for " + decision.getVotingType() + " decisions");
            }
        }
    }
}
