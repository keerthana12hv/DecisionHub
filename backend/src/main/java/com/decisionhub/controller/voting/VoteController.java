package com.decisionhub.controller.voting;

import com.decisionhub.dto.request.voting.SubmitVoteRequest;
import com.decisionhub.dto.response.voting.VoteResponse;
import com.decisionhub.service.interfaces.voting.VoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for submitting,
 * updating, removing, and retrieving user votes.
 *
 * Voting behavior depends on the VotingType configured
 * for the parent Decision.
 */
@RestController
@RequestMapping("/api/decisions/{decisionId}/votes")
@RequiredArgsConstructor
@Tag(
        name = "Vote",
        description = "Decision Voting Management Endpoints"
)
@Slf4j
public class VoteController {

    private final VoteService voteService;

    /**
     * Submits or updates the currently authenticated user's
     * complete vote selection for a Decision.
     *
     * SINGLE_CHOICE:
     * - One option ID selects or changes the vote.
     * - An empty option list removes the existing vote.
     *
     * MULTIPLE_CHOICE:
     * - The submitted option IDs represent the complete selection.
     * - An empty option list removes all existing selections.
     *
     * RATING_BASED decisions are handled through comparison scores
     * and cannot use this endpoint.
     */
    @PutMapping
    @Operation(
            summary = "Submit or update vote",
            description = "Submits or updates the current authenticated user's complete vote selection for a decision. "
                    + "For SINGLE_CHOICE, at most one option may be selected. "
                    + "For MULTIPLE_CHOICE, multiple options may be selected. "
                    + "Submitting an empty option list removes the user's existing vote selection.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<VoteResponse> submitVote(
            @PathVariable Long decisionId,
            @Valid @RequestBody SubmitVoteRequest request
    ) {

        log.info(
                "REST request to submit or update vote for decision ID: {}",
                decisionId
        );

        VoteResponse response =
                voteService.submitVote(
                        decisionId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the currently authenticated user's
     * saved vote selection for a Decision.
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get my vote",
            description = "Retrieves the current authenticated user's saved vote selection for a decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<VoteResponse> getMyVote(
            @PathVariable Long decisionId
    ) {

        log.info(
                "REST request to retrieve current user's vote for decision ID: {}",
                decisionId
        );

        VoteResponse response =
                voteService.getMyVote(decisionId);

        return ResponseEntity.ok(response);
    }
}