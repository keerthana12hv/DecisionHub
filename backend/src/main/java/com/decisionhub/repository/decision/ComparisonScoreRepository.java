package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComparisonScoreRepository extends JpaRepository<ComparisonScore, ComparisonScoreId> {
    List<ComparisonScore> findByOptionId(Long optionId);
    List<ComparisonScore> findByFactorId(Long factorId);
    List<ComparisonScore> findByOptionDecisionId(Long decisionId);
    Optional<ComparisonScore> findByOptionIdAndFactorIdAndUserId(Long optionId, Long factorId, Long userId);
    List<ComparisonScore> findByOptionDecisionIdAndUserId(Long decisionId, Long userId);

    @Query("SELECT COUNT(DISTINCT CONCAT(c.user.id, '-', c.option.decision.id)) FROM ComparisonScore c")
    long countDistinctUserDecisions();

    @Query("SELECT COUNT(DISTINCT CONCAT(c.user.id, '-', c.option.decision.id)) FROM ComparisonScore c WHERE c.option.decision.community.id = :communityId")
    long countDistinctUserDecisionsByCommunityId(@Param("communityId") Long communityId);
}
