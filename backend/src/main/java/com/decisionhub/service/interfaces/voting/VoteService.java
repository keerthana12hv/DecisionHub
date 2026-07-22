package com.decisionhub.service.interfaces.voting;

import com.decisionhub.dto.request.voting.SubmitVoteRequest;
import com.decisionhub.dto.response.voting.VoteResponse;

/**
 * Service interface responsible for vote submission,
 * modification, removal, and retrieval.
 */
public interface VoteService {

    /**
     * Submits or updates the complete vote selection of the
     * currently authenticated user for a Decision.
     *
     * For SINGLE_CHOICE decisions, zero or one option may be selected.
     * For MULTIPLE_CHOICE decisions, zero or more options may be selected.
     *
     * @param decisionId ID of the Decision being voted on.
     * @param request    Complete option selection submitted by the user.
     * @return the user's resulting vote selection.
     */
    VoteResponse submitVote(
            Long decisionId,
            SubmitVoteRequest request
    );

    /**
     * Retrieves the current authenticated user's vote selection
     * for a Decision.
     *
     * @param decisionId ID of the Decision.
     * @return the user's current vote selection.
     */
    VoteResponse getMyVote(Long decisionId);
}