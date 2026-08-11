package com.decisionhub.repository.discussion;

import com.decisionhub.entity.discussion.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(Long decisionId);

    List<Comment> findByDecisionId(Long decisionId);
    List<Comment> findByDecisionIdAndDeletedAtIsNull(Long decisionId);

    long countByDecisionIdAndParentCommentIsNull(Long decisionId);

    long countByDecisionIdAndParentCommentIsNotNull(Long decisionId);

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
     */
    int countByParentCommentId(Long parentCommentId);

    /**
     * Checks whether a comment has at least one reply.
     */
    boolean existsByParentCommentId(Long parentCommentId);

    long countByDecisionCommunityIdAndParentCommentIsNull(Long communityId);

    long countByDecisionCommunityIdAndParentCommentIsNotNull(Long communityId);

    long countByDecisionCommunityIdAndDeletedAtIsNotNull(Long communityId);

    long countByParentCommentIsNull();

    long countByParentCommentIsNotNull();

    @Query("""
        SELECT c.user.id,
               c.user.username,
               COUNT(c)
        FROM Comment c
        WHERE c.decision.community.id = :communityId
        GROUP BY c.user.id, c.user.username
        ORDER BY COUNT(c) DESC
        """)
    List<Object[]> getMostActiveMembers(
            @Param("communityId") Long communityId
    );

    @Query("""
        SELECT COUNT(c)
        FROM Comment c
        WHERE c.parentComment IS NULL
        """)
    long countTopLevelComments();

    @Query("""
        SELECT COUNT(c)
        FROM Comment c
        WHERE c.parentComment IS NOT NULL
        """)
    long countReplies();
}
