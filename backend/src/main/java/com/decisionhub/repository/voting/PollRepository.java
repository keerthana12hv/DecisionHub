package com.decisionhub.repository.voting;

import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    Optional<Poll> findByDecisionId(Long decisionId);

    boolean existsByDecisionId(Long decisionId);

    List<Poll> findByStatusAndEndTimeBefore(
            PollStatus status,
            LocalDateTime time
    );
}
