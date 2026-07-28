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
import com.decisionhub.service.interfaces.voting.VoteService;
import com.decisionhub.validator.voting.PollValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementation responsible for managing user votes
 * associated with Decision Polls.
 *
 * Voting behavior depends on the VotingType configured
 * on the parent Decision:
 *
 * SINGLE_CHOICE:
 * - A user may select at most one option.
 * - Selecting another option replaces the existing vote.
 * - Submitting an empty selection removes the existing vote.
 *
 * MULTIPLE_CHOICE:
 * - A user may select multiple options.
 * - The submitted option list represents the user's complete selection.
 * - Existing votes are synchronized with the submitted selection.
 * - Submitting an empty selection removes all existing votes.
 *
 * RATING_BASED:
 * - Participation is handled through ComparisonScore and is therefore
 *   not processed by this service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoteServiceImpl implements VoteService {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final UserRepository userRepository;

    private final PollValidator pollValidator;
    private final DecisionAuthorizationService decisionAuthorizationService;
    private final AuthenticationFacade authenticationFacade;

    /**
     * Submits or updates the complete vote selection of the
     * currently authenticated user.
     */
    @Override
    @Transactional
    public VoteResponse submitVote(
            Long decisionId,
            SubmitVoteRequest request
    ) {

        log.info(
                "Attempting to submit vote for decision ID: {}",
                decisionId
        );

        // 1. Retrieve the Poll associated with the Decision.
        Poll poll = getPollByDecisionIdOrThrow(decisionId);

        // 2. Ensure the Poll is OPEN and has not expired.
        pollValidator.validateOpenForParticipation(poll);

        // 3. Retrieve the currently authenticated user.
        Long currentUserId = getCurrentUserIdOrThrow();

        // 4. Check voting-specific authorization.
        if (!decisionAuthorizationService.canParticipateInVoting(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to participate in voting for this decision"
            );
        }

        Decision decision = poll.getDecision();

        // 5. RATING_BASED decisions use ComparisonScore instead of Vote.
        if (decision.getVotingType() == VotingType.RATING_BASED) {
            throw new BadRequestException(
                    "Rating-based decisions must be participated in through comparison scores"
            );
        }

        List<Long> submittedOptionIds = request.optionIds();

        // @NotNull handles this at controller validation level,
        // but the service also protects itself from direct calls.
        if (submittedOptionIds == null) {
            throw new BadRequestException(
                    "Option IDs are required"
            );
        }

        // 6. Reject duplicate option IDs in the submitted selection.
        Set<Long> uniqueOptionIds = new HashSet<>(submittedOptionIds);

        if (uniqueOptionIds.size() != submittedOptionIds.size()) {
            throw new BadRequestException(
                    "Duplicate option IDs are not allowed"
            );
        }

        if (uniqueOptionIds.contains(null)) {
            throw new BadRequestException(
                    "Option IDs cannot contain null values"
            );
        }

        // 7. Apply VotingType-specific selection rules.
        if (decision.getVotingType() == VotingType.SINGLE_CHOICE
                && submittedOptionIds.size() > 1) {

            throw new BadRequestException(
                    "Single-choice voting allows at most one selected option"
            );
        }

        // 8. Retrieve and validate every submitted option.
        List<DecisionOption> submittedOptions =
                submittedOptionIds.stream()
                        .map(optionId -> getDecisionOptionOrThrow(
                                optionId,
                                decisionId
                        ))
                        .toList();

        // 9. Retrieve the user's currently persisted votes.
        List<Vote> existingVotes =
                voteRepository.findByPollIdAndUserId(
                        poll.getId(),
                        currentUserId
                );

        /*
         * The request represents the user's complete desired vote state.
         *
         * Example:
         *
         * Existing:  [1, 2]
         * Submitted: [1, 3]
         *
         * Result:
         * - Keep vote for option 1
         * - Delete vote for option 2
         * - Create vote for option 3
         */

        // 10. Remove votes that are no longer selected.
        List<Vote> votesToDelete =
                existingVotes.stream()
                        .filter(vote -> !uniqueOptionIds.contains(
                                vote.getDecisionOption().getId()
                        ))
                        .toList();

        if (!votesToDelete.isEmpty()) {
            voteRepository.deleteAll(votesToDelete);
        }

        // IDs that are already persisted and should be kept.
        Set<Long> existingOptionIds = new HashSet<>();

        for (Vote vote : existingVotes) {
            existingOptionIds.add(
                    vote.getDecisionOption().getId()
            );
        }

        // 11. Retrieve the authenticated User entity only when
        // new Vote rows actually need to be created.
        List<DecisionOption> optionsToAdd =
                submittedOptions.stream()
                        .filter(option -> !existingOptionIds.contains(
                                option.getId()
                        ))
                        .toList();

        if (!optionsToAdd.isEmpty()) {

            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with ID: " + currentUserId
                    ));

            LocalDateTime now = LocalDateTime.now();

            List<Vote> votesToCreate =
                    optionsToAdd.stream()
                            .map(option -> {
                                Vote vote = new Vote();
                                vote.setUser(currentUser);
                                vote.setPoll(poll);
                                vote.setDecisionOption(option);
                                vote.setVotedAt(now);
                                return vote;
                            })
                            .toList();

            voteRepository.saveAll(votesToCreate);
        }

        log.info(
                "Vote selection updated successfully for user ID: {} on decision ID: {}",
                currentUserId,
                decisionId
        );

        return buildVoteResponse(
                poll,
                currentUserId,
                submittedOptionIds
        );
    }

    /**
     * Retrieves the currently authenticated user's saved vote
     * selection for the specified Decision.
     */
    @Override
    @Transactional(readOnly = true)
    public VoteResponse getMyVote(Long decisionId) {

        log.info(
                "Retrieving current user's vote for decision ID: {}",
                decisionId
        );

        Poll poll = getPollByDecisionIdOrThrow(decisionId);

        Long currentUserId = getCurrentUserIdOrThrow();

        /*
         * Retrieval does not require the Poll to still be OPEN.
         * A user may retrieve their saved vote after voting has ended.
         *
         * We still apply Decision view authorization to ensure the user
         * is allowed to access the Decision and its voting information.
         */
        if (!decisionAuthorizationService.canViewDecision(
                decisionId,
                currentUserId
        )) {
            throw new UnauthorizedActionException(
                    "Not authorized to view voting information for this decision"
            );
        }

        List<Vote> votes =
                voteRepository.findByPollIdAndUserId(
                        poll.getId(),
                        currentUserId
                );

        List<Long> optionIds =
                votes.stream()
                        .map(vote ->
                                vote.getDecisionOption().getId()
                        )
                        .toList();

        return buildVoteResponse(
                poll,
                currentUserId,
                optionIds
        );
    }

    /**
     * Retrieves the Poll associated with a Decision or throws
     * an exception when no Poll exists.
     */
    private Poll getPollByDecisionIdOrThrow(Long decisionId) {

        return pollRepository.findByDecisionId(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poll not found for decision ID: " + decisionId
                ));
    }

    /**
     * Retrieves a DecisionOption and verifies that it belongs
     * to the Decision being voted on.
     */
    private DecisionOption getDecisionOptionOrThrow(
            Long optionId,
            Long decisionId
    ) {

        DecisionOption option =
                decisionOptionRepository.findById(optionId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Decision option not found with ID: " + optionId
                        ));

        if (!option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException(
                    "Option with ID " + optionId
                            + " does not belong to decision " + decisionId
            );
        }

        return option;
    }

    /**
     * Retrieves the currently authenticated user's ID or rejects
     * the operation when no authenticated user exists.
     */
    private Long getCurrentUserIdOrThrow() {

        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException(
                        "User is not authenticated"
                ));
    }

    /**
     * Builds a VoteResponse representing the user's complete
     * current vote selection.
     */
    private VoteResponse buildVoteResponse(
            Poll poll,
            Long userId,
            List<Long> optionIds
    ) {

        return new VoteResponse(
                poll.getId(),
                poll.getDecision().getId(),
                userId,
                optionIds
        );
    }
}