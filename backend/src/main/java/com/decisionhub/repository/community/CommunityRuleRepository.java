package com.decisionhub.repository.community;

import com.decisionhub.entity.community.CommunityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRuleRepository extends JpaRepository<CommunityRule, Long> {
    List<CommunityRule> findByCommunityId(Long communityId);
}
