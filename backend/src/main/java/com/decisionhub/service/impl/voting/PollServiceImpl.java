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
import com.decisionhub.service.interfaces.voting.PollService;
import com.decisionhub.validator.voting.PollValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation responsible for managing the lifecycle
 * of Polls associated with Decisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final PollMapper pollMapper;
    private final PollValidator pollValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuthenticationFacade authenticationFacade;

    /**
     * Retrieves the Poll associated with the specified Decision.
     */
    @Override
    @Transactional(readOnly = true)
    public PollResponse getPollByDecisionId(Long decisionId) {

        log.info("Retrieving Poll for decision ID: {}", decisionId);

        Poll poll = getPollByDecisionIdOrThrow(decisionId);

        Long currentUserId = authenticationFacade
                .getCurrentUserId()
                .orElse(null);

        if (!decisionAuthorizationService.canViewDecision(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to view this poll"
            );
        }

        return pollMapper.toResponse(poll);
    }

    /**
     * Extends the voting end time of an existing OPEN Poll.
     *
     * Poll lifecycle management follows ownership of the
     * parent Decision.
     */
    @Override
    @Transactional
    public PollResponse extendPollEndTime(
            Long decisionId,
            UpdatePollEndTimeRequest request
    ) {

        log.info(
                "Attempting to extend Poll end time for decision ID: {}",
                decisionId
        );

        Poll poll = getPollByDecisionIdOrThrow(decisionId);

        Long currentUserId = getCurrentUserIdOrThrow();

        // Only the creator/owner of the parent Decision
        // may manage the Poll lifecycle.
        if (!decisionAuthorizationService.canManagePoll(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to manage this poll"
            );
        }

        // Validate Poll lifecycle and requested new end time.
        pollValidator.validateEndTimeExtension(
                poll,
                request.endTime()
        );

        poll.setEndTime(request.endTime());
        poll.setUpdatedAt(LocalDateTime.now());

        Poll updatedPoll = pollRepository.save(poll);

        log.info(
                "Poll end time extended successfully for decision ID: {} to {}",
                decisionId,
                updatedPoll.getEndTime()
        );

        return pollMapper.toResponse(updatedPoll);
    }

    /**
     * Closes an OPEN Poll before its configured end time.
     */
    @Override
    @Transactional
    public PollResponse closePoll(Long decisionId) {

        log.info(
                "Attempting to close Poll for decision ID: {}",
                decisionId
        );

        Poll poll = getPollByDecisionIdOrThrow(decisionId);

        Long currentUserId = getCurrentUserIdOrThrow();

        // Only the creator/owner of the parent Decision
        // may manage the Poll lifecycle.
        if (!decisionAuthorizationService.canManagePoll(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to manage this poll"
            );
        }

        // Ensure the Poll is currently OPEN.
        pollValidator.validateClose(poll);

        poll.setStatus(PollStatus.CLOSED);
        poll.setUpdatedAt(LocalDateTime.now());

        Poll closedPoll = pollRepository.save(poll);

        log.info(
                "Poll closed successfully for decision ID: {}",
                decisionId
        );

        return pollMapper.toResponse(closedPoll);
    }

    /**
     * Retrieves the Poll associated with a Decision
     * or throws an exception if no Poll exists.
     */
    private Poll getPollByDecisionIdOrThrow(Long decisionId) {

        return pollRepository.findByDecisionId(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poll not found for decision ID: " + decisionId
                ));
    }

    /**
     * Retrieves the currently authenticated user's ID
     * or rejects the operation when no authenticated user exists.
     */
    private Long getCurrentUserIdOrThrow() {

        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException(
                        "User is not authenticated"
                ));
    }
}