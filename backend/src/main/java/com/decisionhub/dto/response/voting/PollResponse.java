package com.decisionhub.dto.response.voting;

import com.decisionhub.enums.voting.PollStatus;

import java.time.LocalDateTime;

/**
 * Response DTO representing the voting lifecycle information
 * associated with a Decision.
 */
public record PollResponse(

        Long id,

        Long decisionId,

        PollStatus status,

        LocalDateTime endTime,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}