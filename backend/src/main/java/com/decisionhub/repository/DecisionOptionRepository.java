package com.decisionhub.repository;

import com.decisionhub.entity.DecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionOptionRepository extends JpaRepository<DecisionOption, UUID> {
    List<DecisionOption> findByDecisionId(UUID decisionId);
    List<DecisionOption> findByDecisionIdAndDeletedAtIsNull(UUID decisionId);
    boolean existsByDecisionIdAndTitleIgnoreCaseAndDeletedAtIsNull(UUID decisionId, String title);
}

