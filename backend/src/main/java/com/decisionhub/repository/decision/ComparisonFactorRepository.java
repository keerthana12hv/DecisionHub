package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.ComparisonFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComparisonFactorRepository extends JpaRepository<ComparisonFactor, Long> {
    List<ComparisonFactor> findByDecisionId(Long decisionId);
    boolean existsByDecisionIdAndNameIgnoreCase(Long decisionId, String name);
}
