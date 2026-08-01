package com.decisionhub.validator.discussion;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.discussion.Comment;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentValidatorTest {

    private CommentValidator commentValidator;

    @BeforeEach
    void setUp() {
        commentValidator = new CommentValidator();
    }

    // =========================================================
    // validateDecisionAllowsComments()
    // =========================================================

    @Test
    void validateDecisionAllowsComments_shouldPass_whenDecisionIsActive() {

        Decision decision = new Decision();
        decision.setStatus(DecisionStatus.ACTIVE);

        assertDoesNotThrow(
                () -> commentValidator.validateDecisionAllowsComments(
                        decision
                )
        );
    }

    @Test
    void validateDecisionAllowsComments_shouldThrow_whenDecisionIsDraft() {

        Decision decision = new Decision();
        decision.setStatus(DecisionStatus.DRAFT);

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateDecisionAllowsComments(
                        decision
                )
        );
    }

    @Test
    void validateDecisionAllowsComments_shouldThrow_whenDecisionIsClosed() {

        Decision decision = new Decision();
        decision.setStatus(DecisionStatus.CLOSED);

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateDecisionAllowsComments(
                        decision
                )
        );
    }

    // =========================================================
    // validateEditable()
    // =========================================================

    @Test
    void validateEditable_shouldPass_whenCommentIsEditable() {

        Comment comment = new Comment();
        comment.setReplies(List.of());

        assertDoesNotThrow(
                () -> commentValidator.validateEditable(comment)
        );
    }

    @Test
    void validateEditable_shouldThrow_whenCommentIsDeleted() {

        Comment comment = new Comment();
        comment.setDeletedAt(LocalDateTime.now());

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateEditable(comment)
        );
    }

    @Test
    void validateEditable_shouldThrow_whenCommentHasReplies() {

        Comment parent = new Comment();

        Comment reply = new Comment();

        parent.setReplies(List.of(reply));

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateEditable(parent)
        );
    }

    // =========================================================
    // validateReplyAllowed()
    // =========================================================

    @Test
    void validateReplyAllowed_shouldPass_whenParentCommentIsActive() {

        Comment parent = new Comment();

        assertDoesNotThrow(
                () -> commentValidator.validateReplyAllowed(parent)
        );
    }

    @Test
    void validateReplyAllowed_shouldThrow_whenParentCommentIsDeleted() {

        Comment parent = new Comment();
        parent.setDeletedAt(LocalDateTime.now());

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateReplyAllowed(parent)
        );
    }

    // =========================================================
    // validateReplyDepth()
    // =========================================================

    @Test
    void validateReplyDepth_shouldPass_whenDepthIsZero() {

        Comment comment = new Comment();
        comment.setDepth(0);

        assertDoesNotThrow(
                () -> commentValidator.validateReplyDepth(comment)
        );
    }

    @Test
    void validateReplyDepth_shouldPass_whenDepthIsFour() {

        Comment comment = new Comment();
        comment.setDepth(4);

        assertDoesNotThrow(
                () -> commentValidator.validateReplyDepth(comment)
        );
    }

    @Test
    void validateReplyDepth_shouldThrow_whenDepthIsFive() {

        Comment comment = new Comment();
        comment.setDepth(5);

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateReplyDepth(comment)
        );
    }

    @Test
    void validateReplyDepth_shouldThrow_whenDepthExceedsFive() {

        Comment comment = new Comment();
        comment.setDepth(6);

        assertThrows(
                BadRequestException.class,
                () -> commentValidator.validateReplyDepth(comment)
        );
    }
}