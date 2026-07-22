package com.decisionhub.service.impl.voting;

import com.decisionhub.dto.request.voting.SubmitVoteRequest;
import com.decisionhub.dto.response.voting.VoteResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.entity.voting.Vote;
import com.decisionhub.enums.decision.VotingType;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.voting.PollRepository;
import com.decisionhub.repository.voting.VoteRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.validator.voting.PollValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private DecisionOptionRepository decisionOptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PollValidator pollValidator;

    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private VoteServiceImpl voteService;

    private Decision decision;
    private Poll poll;
    private User user;
    private DecisionOption option1;
    private DecisionOption option2;

    private final Long decisionId = 1L;
    private final Long pollId = 10L;
    private final Long userId = 100L;

    @BeforeEach
    void setUp() {

        decision = new Decision();
        decision.setId(decisionId);
        decision.setVotingType(VotingType.SINGLE_CHOICE);

        poll = new Poll();
        poll.setId(pollId);
        poll.setDecision(decision);

        user = new User();
        user.setId(userId);

        option1 = new DecisionOption();
        option1.setId(101L);
        option1.setDecision(decision);

        option2 = new DecisionOption();
        option2.setId(102L);
        option2.setDecision(decision);
    }

    @Test
    void submitVote_singleChoice_shouldCreateVoteSuccessfully() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        when(decisionOptionRepository.findById(101L))
                .thenReturn(Optional.of(option1));

        when(voteRepository.findByPollIdAndUserId(
                pollId,
                userId
        )).thenReturn(List.of());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        VoteResponse response =
                voteService.submitVote(
                        decisionId,
                        request
                );

        assertNotNull(response);
        assertEquals(pollId, response.pollId());
        assertEquals(decisionId, response.decisionId());
        assertEquals(userId, response.userId());
        assertEquals(List.of(101L), response.optionIds());

        verify(pollValidator)
                .validateOpenForParticipation(poll);

        verify(voteRepository)
                .saveAll(anyList());
    }

    @Test
    void submitVote_singleChoice_shouldRejectMultipleOptions() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(
                        List.of(101L, 102L)
                );

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );

        verify(voteRepository, never())
                .saveAll(anyList());
    }

    @Test
    void submitVote_singleChoice_shouldReplaceExistingVote() {

        decision.setVotingType(VotingType.SINGLE_CHOICE);

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(102L));

        Vote existingVote = new Vote();
        existingVote.setPoll(poll);
        existingVote.setUser(user);
        existingVote.setDecisionOption(option1);

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        when(decisionOptionRepository.findById(102L))
                .thenReturn(Optional.of(option2));

        when(voteRepository.findByPollIdAndUserId(
                pollId,
                userId
        )).thenReturn(List.of(existingVote));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        VoteResponse response =
                voteService.submitVote(
                        decisionId,
                        request
                );

        assertEquals(
                List.of(102L),
                response.optionIds()
        );

        verify(voteRepository)
                .deleteAll(anyList());

        verify(voteRepository)
                .saveAll(anyList());
    }

    @Test
    void submitVote_singleChoice_emptySelection_shouldRemoveVote() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of());

        Vote existingVote = new Vote();
        existingVote.setPoll(poll);
        existingVote.setUser(user);
        existingVote.setDecisionOption(option1);

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        when(voteRepository.findByPollIdAndUserId(
                pollId,
                userId
        )).thenReturn(List.of(existingVote));

        VoteResponse response =
                voteService.submitVote(
                        decisionId,
                        request
                );

        assertTrue(response.optionIds().isEmpty());

        verify(voteRepository)
                .deleteAll(anyList());

        verify(voteRepository, never())
                .saveAll(anyList());
    }

    @Test
    void submitVote_multipleChoice_shouldSynchronizeSelections() {

        decision.setVotingType(
                VotingType.MULTIPLE_CHOICE
        );

        DecisionOption option3 =
                new DecisionOption();

        option3.setId(103L);
        option3.setDecision(decision);

        Vote vote1 = new Vote();
        vote1.setPoll(poll);
        vote1.setUser(user);
        vote1.setDecisionOption(option1);

        Vote vote2 = new Vote();
        vote2.setPoll(poll);
        vote2.setUser(user);
        vote2.setDecisionOption(option2);

        SubmitVoteRequest request =
                new SubmitVoteRequest(
                        List.of(102L, 103L)
                );

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        when(decisionOptionRepository.findById(102L))
                .thenReturn(Optional.of(option2));

        when(decisionOptionRepository.findById(103L))
                .thenReturn(Optional.of(option3));

        when(voteRepository.findByPollIdAndUserId(
                pollId,
                userId
        )).thenReturn(
                List.of(vote1, vote2)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        VoteResponse response =
                voteService.submitVote(
                        decisionId,
                        request
                );

        assertEquals(
                List.of(102L, 103L),
                response.optionIds()
        );

        verify(voteRepository)
                .deleteAll(anyList());

        verify(voteRepository)
                .saveAll(anyList());
    }

    @Test
    void submitVote_ratingBased_shouldRejectVoteEndpoint() {

        decision.setVotingType(
                VotingType.RATING_BASED
        );

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );
    }

    @Test
    void submitVote_whenUserNotAuthorized_shouldThrowException() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );

        verify(voteRepository, never())
                .saveAll(anyList());
    }

    @Test
    void submitVote_whenUserNotAuthenticated_shouldThrowException() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedActionException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );
    }

    @Test
    void submitVote_whenPollDoesNotExist_shouldThrowException() {

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );
    }

    @Test
    void submitVote_whenOptionBelongsToAnotherDecision_shouldReject() {

        Decision anotherDecision =
                new Decision();

        anotherDecision.setId(999L);

        DecisionOption foreignOption =
                new DecisionOption();

        foreignOption.setId(101L);
        foreignOption.setDecision(
                anotherDecision
        );

        SubmitVoteRequest request =
                new SubmitVoteRequest(List.of(101L));

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        when(decisionOptionRepository.findById(101L))
                .thenReturn(Optional.of(foreignOption));

        assertThrows(
                BadRequestException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );
    }

    @Test
    void submitVote_duplicateOptionIds_shouldReject() {

        decision.setVotingType(
                VotingType.MULTIPLE_CHOICE
        );

        SubmitVoteRequest request =
                new SubmitVoteRequest(
                        List.of(101L, 101L)
                );

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                userId
        )).thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> voteService.submitVote(
                        decisionId,
                        request
                )
        );
    }

    @Test
    void getMyVote_shouldReturnCurrentUserVotes() {

        Vote vote1 = new Vote();
        vote1.setPoll(poll);
        vote1.setUser(user);
        vote1.setDecisionOption(option1);

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canViewDecision(
                decisionId,
                userId
        )).thenReturn(true);

        when(voteRepository.findByPollIdAndUserId(
                pollId,
                userId
        )).thenReturn(List.of(vote1));

        VoteResponse response =
                voteService.getMyVote(decisionId);

        assertNotNull(response);
        assertEquals(pollId, response.pollId());
        assertEquals(decisionId, response.decisionId());
        assertEquals(userId, response.userId());
        assertEquals(
                List.of(101L),
                response.optionIds()
        );
    }

    @Test
    void getMyVote_whenNotAuthorizedToView_shouldThrowException() {

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canViewDecision(
                decisionId,
                userId
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> voteService.getMyVote(
                        decisionId
                )
        );
    }
}