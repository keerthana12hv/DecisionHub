package com.decisionhub.repository.decision;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionFeedback;

@Repository
public interface DecisionFeedbackRepository extends JpaRepository<DecisionFeedback, Long> {

    /**
     * Checks whether feedback already exists for a decision.
     */
    boolean existsByDecision(Decision decision);

    /**
     * Retrieves feedback for a specific decision.
     */
    Optional<DecisionFeedback> findByDecision(Decision decision);

    long countByDecisionId(Long decisionId);

long countByDecisionIdAndRating(Long decisionId, Integer rating);

@Query("""
SELECT COALESCE(AVG(df.rating),0)
FROM DecisionFeedback df
""")
Double getAverageRating();

@Query("""
SELECT COALESCE(AVG(df.rating), 0.0)
FROM DecisionFeedback df
WHERE df.decision.id = :decisionId
""")
Double getAverageRatingByDecisionId(@Param("decisionId") Long decisionId);

long countByRating(Integer rating);
}