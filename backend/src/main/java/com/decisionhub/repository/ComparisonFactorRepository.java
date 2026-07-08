package com.decisionhub.repository;

import com.decisionhub.entity.ComparisonFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComparisonFactorRepository extends JpaRepository<ComparisonFactor, UUID> {
    List<ComparisonFactor> findByDecisionId(UUID decisionId);
    boolean existsByDecisionIdAndNameIgnoreCase(UUID decisionId, String name);
}
