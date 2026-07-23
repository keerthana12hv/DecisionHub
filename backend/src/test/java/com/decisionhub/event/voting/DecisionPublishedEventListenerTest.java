package com.decisionhub.event.voting;

import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.event.DecisionPublishedEvent;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.voting.PollRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DecisionPublishedEventListener}.
 *
 * Verifies automatic Poll creation when a Decision is published,
 * duplicate Poll prevention, and handling of missing Decisions.
 */
@ExtendWith(MockitoExtension.class)
class DecisionPublishedEventListenerTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private PollRepository pollRepository;

    @InjectMocks
    private DecisionPublishedEventListener listener;

    private Decision decision;
    private LocalDateTime votingEndTime;

    @BeforeEach
    void setUp() {

        votingEndTime = LocalDateTime.now().plusDays(5);

        decision = new Decision();
        decision.setId(1L);
        decision.setVotingEndTime(votingEndTime);
    }

    /**
     * Verifies that publishing a Decision creates exactly one Poll
     * when no Poll already exists.
     */
    @Test
    void handleDecisionPublished_shouldCreatePoll_whenPollDoesNotExist() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        when(pollRepository.save(any(Poll.class)))
                .thenAnswer(invocation -> {
                    Poll poll = invocation.getArgument(0);
                    poll.setId(100L);
                    return poll;
                });

        listener.handleDecisionPublished(event);

        verify(pollRepository, times(1))
                .save(any(Poll.class));
    }

    /**
     * Verifies that the automatically created Poll is associated
     * with the published Decision.
     */
    @Test
    void handleDecisionPublished_shouldAssociatePollWithDecision() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        when(pollRepository.save(any(Poll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener.handleDecisionPublished(event);

        ArgumentCaptor<Poll> pollCaptor =
                ArgumentCaptor.forClass(Poll.class);

        verify(pollRepository).save(pollCaptor.capture());

        Poll savedPoll = pollCaptor.getValue();

        assertSame(decision, savedPoll.getDecision());
    }

    /**
     * Verifies that a newly created Poll starts in OPEN status.
     */
    @Test
    void handleDecisionPublished_shouldCreatePollWithOpenStatus() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        when(pollRepository.save(any(Poll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener.handleDecisionPublished(event);

        ArgumentCaptor<Poll> pollCaptor =
                ArgumentCaptor.forClass(Poll.class);

        verify(pollRepository).save(pollCaptor.capture());

        Poll savedPoll = pollCaptor.getValue();

        assertEquals(
                PollStatus.OPEN,
                savedPoll.getStatus()
        );
    }

    /**
     * Verifies that the Poll end time is copied from the
     * Decision's configured voting end time.
     */
    @Test
    void handleDecisionPublished_shouldCopyVotingEndTimeFromDecision() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        when(pollRepository.save(any(Poll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener.handleDecisionPublished(event);

        ArgumentCaptor<Poll> pollCaptor =
                ArgumentCaptor.forClass(Poll.class);

        verify(pollRepository).save(pollCaptor.capture());

        Poll savedPoll = pollCaptor.getValue();

        assertEquals(
                votingEndTime,
                savedPoll.getEndTime()
        );
    }

    /**
     * Verifies that creation and update timestamps are initialized
     * when the Poll is automatically created.
     */
    @Test
    void handleDecisionPublished_shouldSetCreatedAtAndUpdatedAt() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.of(decision));

        when(pollRepository.save(any(Poll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener.handleDecisionPublished(event);

        ArgumentCaptor<Poll> pollCaptor =
                ArgumentCaptor.forClass(Poll.class);

        verify(pollRepository).save(pollCaptor.capture());

        Poll savedPoll = pollCaptor.getValue();

        assertNotNull(savedPoll.getCreatedAt());
        assertNotNull(savedPoll.getUpdatedAt());

        assertEquals(
                savedPoll.getCreatedAt(),
                savedPoll.getUpdatedAt()
        );
    }

    /**
     * Verifies that a duplicate Poll is not created if a Poll
     * already exists for the published Decision.
     */
    @Test
    void handleDecisionPublished_shouldNotCreateDuplicatePoll_whenPollAlreadyExists() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(true);

        listener.handleDecisionPublished(event);

        verify(decisionRepository, never())
                .findById(anyLong());

        verify(pollRepository, never())
                .save(any(Poll.class));
    }

    /**
     * Verifies that a ResourceNotFoundException is thrown when
     * the Decision referenced by the event does not exist.
     */
    @Test
    void handleDecisionPublished_shouldThrowException_whenDecisionNotFound() {

        DecisionPublishedEvent event =
                new DecisionPublishedEvent(this, 1L);

        when(pollRepository.existsByDecisionId(1L))
                .thenReturn(false);

        when(decisionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> listener.handleDecisionPublished(event)
        );

        verify(pollRepository, never())
                .save(any(Poll.class));
    }
}