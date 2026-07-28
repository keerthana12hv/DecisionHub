package com.decisionhub.service.impl.voting;

import com.decisionhub.dto.request.voting.UpdatePollEndTimeRequest;
import com.decisionhub.dto.response.voting.PollResponse;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.voting.PollMapper;
import com.decisionhub.repository.voting.PollRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.security.decision.DecisionAuthorizationService;
import com.decisionhub.validator.voting.PollValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollServiceImplTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollMapper pollMapper;

    @Mock
    private PollValidator pollValidator;

    @Mock
    private DecisionAuthorizationService decisionAuthorizationService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private PollServiceImpl pollService;

    private Long decisionId;
    private Long userId;
    private Poll poll;
    private PollResponse pollResponse;

    @BeforeEach
    void setUp() {

        decisionId = 1L;
        userId = 10L;

        LocalDateTime now = LocalDateTime.now();

        poll = new Poll();
        poll.setId(100L);
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(now.plusDays(1));
        poll.setCreatedAt(now);
        poll.setUpdatedAt(now);

        pollResponse = new PollResponse(
                100L,
                decisionId,
                PollStatus.OPEN,
                poll.getEndTime(),
                poll.getCreatedAt(),
                poll.getUpdatedAt()
        );
    }

    @Test
    void getPollByDecisionId_WhenAuthorized_ShouldReturnPoll() {

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canViewDecision(
                decisionId,
                userId
        )).thenReturn(true);

        when(pollMapper.toResponse(poll))
                .thenReturn(pollResponse);

        PollResponse result =
                pollService.getPollByDecisionId(decisionId);

        assertNotNull(result);
        assertEquals(pollResponse, result);

        verify(pollRepository)
                .findByDecisionId(decisionId);

        verify(decisionAuthorizationService)
                .canViewDecision(decisionId, userId);

        verify(pollMapper)
                .toResponse(poll);
    }

    @Test
    void getPollByDecisionId_WhenUnauthorized_ShouldThrowException() {

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
                () -> pollService.getPollByDecisionId(decisionId)
        );

        verify(pollMapper, never())
                .toResponse(any());
    }

    @Test
    void getPollByDecisionId_WhenPollNotFound_ShouldThrowException() {

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> pollService.getPollByDecisionId(decisionId)
        );

        verifyNoInteractions(
                decisionAuthorizationService,
                pollMapper
        );
    }

    @Test
    void extendPollEndTime_WhenAuthorized_ShouldUpdateAndReturnPoll() {

        LocalDateTime newEndTime =
                poll.getEndTime().plusHours(5);

        UpdatePollEndTimeRequest request =
                new UpdatePollEndTimeRequest(newEndTime);

        PollResponse updatedResponse =
                new PollResponse(
                        100L,
                        decisionId,
                        PollStatus.OPEN,
                        newEndTime,
                        poll.getCreatedAt(),
                        LocalDateTime.now()
                );

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canManagePoll(
                decisionId,
                userId
        )).thenReturn(true);

        when(pollRepository.save(poll))
                .thenReturn(poll);

        when(pollMapper.toResponse(poll))
                .thenReturn(updatedResponse);

        PollResponse result =
                pollService.extendPollEndTime(
                        decisionId,
                        request
                );

        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals(newEndTime, poll.getEndTime());
        assertNotNull(poll.getUpdatedAt());

        verify(pollValidator)
                .validateEndTimeExtension(
                        poll,
                        newEndTime
                );

        verify(pollRepository)
                .save(poll);

        verify(pollMapper)
                .toResponse(poll);
    }

    @Test
    void extendPollEndTime_WhenUnauthenticated_ShouldThrowException() {

        LocalDateTime newEndTime =
                poll.getEndTime().plusHours(5);

        UpdatePollEndTimeRequest request =
                new UpdatePollEndTimeRequest(newEndTime);

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedActionException.class,
                () -> pollService.extendPollEndTime(
                        decisionId,
                        request
                )
        );

        verify(decisionAuthorizationService, never())
                .canManagePoll(anyLong(), anyLong());

        verify(pollValidator, never())
                .validateEndTimeExtension(any(), any());

        verify(pollRepository, never())
                .save(any());
    }

    @Test
    void extendPollEndTime_WhenNotOwner_ShouldThrowException() {

        LocalDateTime newEndTime =
                poll.getEndTime().plusHours(5);

        UpdatePollEndTimeRequest request =
                new UpdatePollEndTimeRequest(newEndTime);

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canManagePoll(
                decisionId,
                userId
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> pollService.extendPollEndTime(
                        decisionId,
                        request
                )
        );

        verify(pollValidator, never())
                .validateEndTimeExtension(any(), any());

        verify(pollRepository, never())
                .save(any());
    }

    @Test
    void closePoll_WhenAuthorized_ShouldCloseAndReturnPoll() {

        PollResponse closedResponse =
                new PollResponse(
                        100L,
                        decisionId,
                        PollStatus.CLOSED,
                        poll.getEndTime(),
                        poll.getCreatedAt(),
                        LocalDateTime.now()
                );

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canManagePoll(
                decisionId,
                userId
        )).thenReturn(true);

        when(pollRepository.save(poll))
                .thenReturn(poll);

        when(pollMapper.toResponse(poll))
                .thenReturn(closedResponse);

        PollResponse result =
                pollService.closePoll(decisionId);

        assertNotNull(result);
        assertEquals(closedResponse, result);
        assertEquals(PollStatus.CLOSED, poll.getStatus());
        assertNotNull(poll.getUpdatedAt());

        verify(pollValidator)
                .validateClose(poll);

        verify(pollRepository)
                .save(poll);

        verify(pollMapper)
                .toResponse(poll);
    }

    @Test
    void closePoll_WhenUnauthenticated_ShouldThrowException() {

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedActionException.class,
                () -> pollService.closePoll(decisionId)
        );

        verify(decisionAuthorizationService, never())
                .canManagePoll(anyLong(), anyLong());

        verify(pollValidator, never())
                .validateClose(any());

        verify(pollRepository, never())
                .save(any());
    }

    @Test
    void closePoll_WhenNotOwner_ShouldThrowException() {

        when(pollRepository.findByDecisionId(decisionId))
                .thenReturn(Optional.of(poll));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(Optional.of(userId));

        when(decisionAuthorizationService.canManagePoll(
                decisionId,
                userId
        )).thenReturn(false);

        assertThrows(
                UnauthorizedActionException.class,
                () -> pollService.closePoll(decisionId)
        );

        verify(pollValidator, never())
                .validateClose(any());

        verify(pollRepository, never())
                .save(any());
    }
}