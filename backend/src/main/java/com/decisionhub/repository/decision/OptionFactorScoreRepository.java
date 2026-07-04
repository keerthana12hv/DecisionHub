package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.OptionFactorScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionFactorScoreRepository extends JpaRepository<OptionFactorScore, Long> {

}
