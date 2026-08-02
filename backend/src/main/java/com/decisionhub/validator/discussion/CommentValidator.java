package com.decisionhub.validator.discussion;

import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;

import org.springframework.stereotype.Component;

/**
 * Validator responsible for enforcing Discussion module
 * business rules.
 */
@Component
public class CommentValidator {

    private static final int MAX_REPLY_DEPTH = 5;

    /**
     * Ensures comments can only be added to ACTIVE decisions.
     */
    public void validateDecisionAllowsComments(Decision decision) {

        if (decision.getStatus() != DecisionStatus.ACTIVE) {
            throw new BadRequestException(
                    "Comments are only allowed on ACTIVE decisions"
            );
        }
    }

    /**
     * A comment cannot be edited after it has replies.
     */
    public void validateEditable(Comment comment) {

        if (comment.getDeletedAt() != null) {
            throw new BadRequestException(
                    "Deleted comments cannot be edited"
            );
        }

        if (comment.getReplies() != null
                && !comment.getReplies().isEmpty()) {

            throw new BadRequestException(
                    "Comments with replies cannot be edited"
            );
        }
    }

    /**
     * Deleted comments cannot receive new replies.
     */
    public void validateReplyAllowed(Comment parentComment) {

        if (parentComment.getDeletedAt() != null) {
            throw new BadRequestException(
                    "Replies cannot be added to deleted comments"
            );
        }
    }

    /**
     * Validates that replying to the specified parent
     * will not exceed the maximum reply depth.
     */
    public void validateReplyDepth(Comment parentComment) {

        if (parentComment.getDepth() >= MAX_REPLY_DEPTH) {
            throw new BadRequestException(
                    "Maximum reply depth of 5 has been reached"
            );
        }
    }
}