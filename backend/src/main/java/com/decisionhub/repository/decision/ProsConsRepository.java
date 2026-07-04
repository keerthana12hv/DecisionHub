package com.decisionhub.repository.decision;

import com.decisionhub.entity.decision.ProsCons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProsConsRepository extends JpaRepository<ProsCons, Long> {

}
