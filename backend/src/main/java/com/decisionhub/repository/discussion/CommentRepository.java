package com.decisionhub.repository.discussion;

import com.decisionhub.entity.discussion.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findFirstByDecisionIdAndPinnedTrueAndDeletedAtIsNull(Long decisionId);
    List<Comment> findByDecisionId(Long decisionId);
    List<Comment> findByDecisionIdAndDeletedAtIsNull(Long decisionId);
}
