package com.decisionhub.dto.request.voting;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO used to submit or update a user's complete vote selection.
 *
 * For SINGLE_CHOICE decisions:
 * - Zero option IDs removes the existing vote.
 * - One option ID represents the selected option.
 *
 * For MULTIPLE_CHOICE decisions:
 * - Zero option IDs removes all existing votes.
 * - Multiple option IDs represent the user's complete selection.
 */
public record SubmitVoteRequest(

        @NotNull(message = "Option IDs are required")
        List<Long> optionIds

) {}
