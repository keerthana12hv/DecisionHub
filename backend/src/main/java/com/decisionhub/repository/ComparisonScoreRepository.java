package com.decisionhub.repository;

import com.decisionhub.entity.ComparisonScore;
import com.decisionhub.entity.ComparisonScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComparisonScoreRepository extends JpaRepository<ComparisonScore, ComparisonScoreId> {
    List<ComparisonScore> findByOptionId(UUID optionId);
    List<ComparisonScore> findByFactorId(UUID factorId);
    List<ComparisonScore> findByOptionDecisionId(UUID decisionId);
    Optional<ComparisonScore> findByOptionIdAndFactorIdAndUserId(UUID optionId, UUID factorId, UUID userId);
    List<ComparisonScore> findByOptionDecisionIdAndUserId(UUID decisionId, UUID userId);
}
