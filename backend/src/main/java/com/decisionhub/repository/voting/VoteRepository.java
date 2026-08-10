package com.decisionhub.repository.voting;

import com.decisionhub.entity.voting.Vote;
import com.decisionhub.enums.community.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByPollIdAndUserId(Long pollId, Long userId);

    List<Vote> findByPollId(Long pollId);

    Optional<Vote> findByPollIdAndUserIdAndDecisionOptionId(
            Long pollId,
            Long userId,
            Long optionId
    );

    boolean existsByPollId(Long pollId);

    long countByPollId(Long pollId);

    @Query("""
        SELECT v.decisionOption.id,
               v.decisionOption.optionName,
               COUNT(v)
        FROM Vote v
        WHERE v.poll.decision.id = :decisionId
        GROUP BY v.decisionOption.id,
                 v.decisionOption.optionName
        ORDER BY COUNT(v) DESC,
                 v.decisionOption.optionName ASC
        """)
    List<Object[]> getVoteDistribution(
            @Param("decisionId") Long decisionId
    );

    @Query("""
        SELECT COUNT(DISTINCT v.user.id)
        FROM Vote v
        JOIN CommunityMember cm
          ON cm.user.id = v.user.id
        WHERE v.poll.decision.id = :decisionId
          AND cm.community.id = :communityId
          AND cm.status = :status
        """)
    Long countParticipants(
            @Param("decisionId") Long decisionId,
            @Param("communityId") Long communityId,
            @Param("status") MembershipStatus status
    );

    @Query("""
        SELECT COUNT(DISTINCT v.user.id)
        FROM Vote v
        WHERE v.poll.id = :pollId
        """)
    Long countDistinctByPollId(
            @Param("pollId") Long pollId
    );

    @Query("""
        SELECT COUNT(v)
        FROM Vote v
        WHERE v.poll.decision.community.id = :communityId
        """)
    Long countVotesByCommunity(
            @Param("communityId") Long communityId
    );

    @Query("""
SELECT v.user.id,
       v.user.username,
       COUNT(v)
FROM Vote v
WHERE v.poll.decision.community.id = :communityId
GROUP BY v.user.id, v.user.username
ORDER BY COUNT(v) DESC
""")
List<Object[]> getHighestParticipationMembers(
        @Param("communityId") Long communityId
);



}
