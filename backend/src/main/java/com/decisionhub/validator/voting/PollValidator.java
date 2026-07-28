package com.decisionhub.validator.voting;

import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.exception.BadRequestException;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validator class enforcing business rules for Poll lifecycle operations.
 */
@Component
public class PollValidator {

    /**
     * Validates whether the end time of an existing Poll can be extended.
     *
     * Rules:
     * 1. The Poll must currently be OPEN.
     * 2. The Poll must not already be expired.
     * 3. The new end time must be in the future.
     * 4. The new end time must be later than the current Poll end time.
     * 5. The new end time must not exceed the parent Decision deadline,
     *    when a Decision deadline is configured.
     *
     * @param poll       The Poll whose end time is being extended.
     * @param newEndTime The requested new voting end time.
     */
    public void validateEndTimeExtension(Poll poll, LocalDateTime newEndTime) {

        if (poll.getStatus() != PollStatus.OPEN) {
            throw new BadRequestException(
                    "Poll end time can only be extended while the poll is OPEN"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (poll.getEndTime() == null) {
            throw new BadRequestException(
                    "Poll does not have a configured end time"
            );
        }

        if (!now.isBefore(poll.getEndTime())) {
            throw new BadRequestException(
                    "Cannot extend the end time of an expired poll"
            );
        }

        if (newEndTime == null) {
            throw new BadRequestException(
                    "New poll end time is required"
            );
        }

        if (!newEndTime.isAfter(now)) {
            throw new BadRequestException(
                    "New poll end time must be in the future"
            );
        }

        if (!newEndTime.isAfter(poll.getEndTime())) {
            throw new BadRequestException(
                    "New poll end time must be later than the current poll end time"
            );
        }

        LocalDateTime decisionDeadline = poll.getDecision().getDeadline();

        if (decisionDeadline != null && newEndTime.isAfter(decisionDeadline)) {
            throw new BadRequestException(
                    "Poll end time cannot exceed the decision deadline"
            );
        }
    }

    /**
     * Validates whether an OPEN Poll can be closed early.
     *
     * @param poll The Poll to be closed.
     */
    public void validateClose(Poll poll) {

        if (poll.getStatus() != PollStatus.OPEN) {
            throw new BadRequestException(
                    "Only an OPEN poll can be closed"
            );
        }
    }

    /**
     * Validates whether the Poll is currently open for participation.
     *
     * This validation must be used by voting and rating operations.
     * It checks both the persisted Poll status and the actual end time,
     * ensuring correctness even if an expired Poll has not yet been
     * automatically updated to CLOSED in the database.
     *
     * @param poll The Poll being participated in.
     */
    public void validateOpenForParticipation(Poll poll) {

        if (poll.getStatus() != PollStatus.OPEN) {
            throw new BadRequestException(
                    "Poll is closed and no longer accepts participation"
            );
        }

        if (poll.getEndTime() == null) {
            throw new BadRequestException(
                    "Poll does not have a configured end time"
            );
        }

        if (!LocalDateTime.now().isBefore(poll.getEndTime())) {
            throw new BadRequestException(
                    "Poll voting period has ended"
            );
        }
    }
}