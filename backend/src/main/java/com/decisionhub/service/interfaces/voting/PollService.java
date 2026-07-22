package com.decisionhub.service.interfaces.voting;

import com.decisionhub.dto.request.voting.UpdatePollEndTimeRequest;
import com.decisionhub.dto.response.voting.PollResponse;

/**
 * Service interface responsible for managing the lifecycle
 * of Polls associated with Decisions.
 */
public interface PollService {

    /**
     * Retrieves the Poll associated with a Decision.
     *
     * @param decisionId ID of the parent Decision.
     * @return PollResponse containing Poll lifecycle information.
     */
    PollResponse getPollByDecisionId(Long decisionId);

    /**
     * Extends the end time of an active Poll.
     *
     * Only the owner/creator of the parent Decision
     * is allowed to perform this operation.
     *
     * @param decisionId ID of the parent Decision.
     * @param request requested new Poll end time.
     * @return updated PollResponse.
     */
    PollResponse extendPollEndTime(
            Long decisionId,
            UpdatePollEndTimeRequest request
    );

    /**
     * Closes an OPEN Poll before its configured end time.
     *
     * Only the owner/creator of the parent Decision
     * is allowed to perform this operation.
     *
     * @param decisionId ID of the parent Decision.
     * @return updated PollResponse.
     */
    PollResponse closePoll(Long decisionId);
}