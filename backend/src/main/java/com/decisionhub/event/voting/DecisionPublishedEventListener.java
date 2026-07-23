package com.decisionhub.event.voting;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.event.DecisionPublishedEvent;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.voting.PollRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Listener responsible for automatically creating a Poll
 * when a Decision is published.
 *
 * A Decision has no Poll while it is in DRAFT status.
 * Once the Decision is published and transitions to ACTIVE,
 * exactly one OPEN Poll is created for that Decision.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionPublishedEventListener {

    private final DecisionRepository decisionRepository;
    private final PollRepository pollRepository;

    /**
     * Handles DecisionPublishedEvent and automatically creates
     * the Poll associated with the published Decision.
     *
     * @param event event containing the published Decision ID
     */
    @EventListener
    @Transactional
    public void handleDecisionPublished(DecisionPublishedEvent event) {

        Long decisionId = event.getDecisionId();

        log.info(
                "Received DecisionPublishedEvent for decision ID: {}",
                decisionId
        );

        // Prevent duplicate Poll creation.
        if (pollRepository.existsByDecisionId(decisionId)) {
            log.warn(
                    "Poll already exists for decision ID: {}. Skipping Poll creation.",
                    decisionId
            );
            return;
        }

        // Fetch the published Decision.
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Decision not found with ID: " + decisionId
                ));

        LocalDateTime now = LocalDateTime.now();

        // Create the Poll using the voting configuration
        // originally defined while the Decision was in DRAFT.
        Poll poll = new Poll();

        poll.setDecision(decision);
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(decision.getVotingEndTime());
        poll.setCreatedAt(now);
        poll.setUpdatedAt(now);

        Poll savedPoll = pollRepository.save(poll);

        log.info(
                "Poll created successfully with ID: {} for decision ID: {}",
                savedPoll.getId(),
                decisionId
        );
    }
}