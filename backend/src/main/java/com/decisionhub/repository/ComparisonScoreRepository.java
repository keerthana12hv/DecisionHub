package com.decisionhub.repository;

import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComparisonScoreRepository extends JpaRepository<ComparisonScore, ComparisonScoreId> {
    List<ComparisonScore> findByOptionId(Long optionId);
    List<ComparisonScore> findByFactorId(Long factorId);
    List<ComparisonScore> findByOptionDecisionId(Long decisionId);
    Optional<ComparisonScore> findByOptionIdAndFactorIdAndUserId(Long optionId, Long factorId, Long userId);
    List<ComparisonScore> findByOptionDecisionIdAndUserId(Long decisionId, Long userId);
}
