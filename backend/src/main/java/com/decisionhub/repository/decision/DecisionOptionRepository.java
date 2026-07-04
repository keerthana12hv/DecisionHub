package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.DecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionOptionRepository extends JpaRepository<DecisionOption, Long> {

}
