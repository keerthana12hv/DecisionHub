package com.decisionhub.repository.voting;

import com.decisionhub.entity.voting.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote,Long> {

    List<Vote> findByPollIdAndUserId(Long pollId, Long userId);

    Optional<Vote> findByPollIdAndUserIdAndDecisionOptionId(
            Long pollId,
            Long userId,
            Long optionId
    );

    boolean existsByPollId(Long pollId);

    long countByPollId(Long pollId);
}
