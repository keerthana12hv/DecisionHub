package com.decisionhub.repository.discussion;

import com.decisionhub.entity.discussion.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Retrieves all top-level comments belonging to a Decision,
     * ordered by creation time.
     */
    List<Comment> findByDecisionIdAndParentCommentIsNullOrderByCreatedAtAsc(
            Long decisionId
    );

    /**
     * Retrieves all direct replies of a parent comment,
     * ordered by creation time.
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(
            Long parentCommentId
    );

    /**
     * Returns the number of direct replies
     * for the specified parent comment.
     *
     * Used for lazy-loading discussions.
     */
    int countByParentCommentId(
            Long parentCommentId
    );

    /**
     * Checks whether a comment has at least one reply.
     *
     * Used to determine whether editing is allowed.
     */
    boolean existsByParentCommentId(
            Long parentCommentId
    );
}