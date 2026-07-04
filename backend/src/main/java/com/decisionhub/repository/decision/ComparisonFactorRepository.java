package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.ComparisonFactorTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComparisonFactorRepository extends JpaRepository<ComparisonFactorTemplate, Long> {

}
