package com.decisionhub.service.interfaces.discussion;

import com.decisionhub.dto.request.discussion.CreateCommentRequest;
import com.decisionhub.dto.request.discussion.UpdateCommentRequest;
import com.decisionhub.dto.response.discussion.CommentResponse;

import java.util.List;

/**
 * Service responsible for managing discussions on Decisions.
 *
 * Supports:
 * - Creating comments
 * - Replying to comments
 * - Editing comments
 * - Soft deleting comments and their reply subtrees
 * - Retrieving discussion threads
 */
public interface CommentService {

    /**
     * Creates a new top-level comment.
     *
     * @param decisionId Decision ID
     * @param request comment payload
     * @return created comment
     */
    CommentResponse createComment(
            Long decisionId,
            CreateCommentRequest request
    );

    /**
     * Creates a reply to an existing comment.
     *
     * @param decisionId Decision ID
     * @param parentCommentId parent comment
     * @param request reply payload
     * @return created reply
     */
    CommentResponse replyToComment(
            Long decisionId,
            Long parentCommentId,
            CreateCommentRequest request
    );

    /**
     * Updates an existing comment.
     *
     * Only the owner may edit.
     */
    CommentResponse updateComment(
            Long commentId,
            UpdateCommentRequest request
    );

    /**
     * Soft deletes a comment and its entire reply subtree.
     *
     * The records are preserved in the database.
     * Deleted comments and their replies are excluded
     * from normal discussion retrieval.
     */
    void deleteComment(Long commentId);

    /**
     * Returns all non-deleted top-level comments
     * belonging to a Decision.
     *
     * Replies are loaded separately.
     */
    List<CommentResponse> getCommentsByDecision(
            Long decisionId
    );

    /**
     * Returns the immediate non-deleted replies
     * of a non-deleted parent comment.
     */
    List<CommentResponse> getReplies(
            Long parentCommentId
    );

    /**
     * Returns a single comment.
     *
     * This method remains separate from the normal
     * discussion listing.
     */
    CommentResponse getComment(
            Long commentId
    );
}