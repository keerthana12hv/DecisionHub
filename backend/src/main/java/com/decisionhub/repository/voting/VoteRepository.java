package com.decisionhub.repository.voting;

import com.decisionhub.entity.voting.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByPollIdAndUserId(Long pollId, Long userId);

    Optional<Vote> findByPollIdAndUserIdAndDecisionOptionId(
            Long pollId,
            Long userId,
            Long optionId
    );

    boolean existsByPollId(Long pollId);

    long countByPollId(Long pollId);

    /** Count votes cast for a specific option across all polls for a decision. */
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.decisionOption.id = :optionId")
    long countByDecisionOptionId(@Param("optionId") Long optionId);

    /** Count votes per option for a given decision, returned as [optionId, count] pairs. */
    @Query("SELECT v.decisionOption.id, COUNT(v) FROM Vote v " +
           "WHERE v.poll.decision.id = :decisionId GROUP BY v.decisionOption.id")
    List<Object[]> countByDecisionIdGroupByOption(@Param("decisionId") Long decisionId);
}
