package com.decisionhub.dto.response.voting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a user's current vote selection
 * for a Poll.
 */
public record VoteResponse(

        Long pollId,

        Long decisionId,

        Long userId,

        List<Long> optionIds

) {}
