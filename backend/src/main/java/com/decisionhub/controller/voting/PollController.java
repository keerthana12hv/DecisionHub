package com.decisionhub.controller.voting;

import com.decisionhub.dto.request.voting.UpdatePollEndTimeRequest;
import com.decisionhub.dto.response.voting.PollResponse;
import com.decisionhub.service.interfaces.voting.PollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for managing the lifecycle
 * of Polls associated with Decisions.
 *
 * Poll creation is not exposed through this controller.
 * A Poll is created automatically when its parent Decision
 * transitions from DRAFT to ACTIVE.
 */
@RestController
@RequestMapping("/api/decisions/{decisionId}/poll")
@RequiredArgsConstructor
@Tag(
        name = "Poll Management",
        description = "Endpoints for viewing and managing the voting lifecycle of Decisions"
)
@Slf4j
public class PollController {

    private final PollService pollService;

    /**
     * Retrieves the Poll associated with a Decision.
     *
     * Access follows the visibility rules of the parent Decision.
     */
    @GetMapping
    @Operation(
            summary = "Get poll for a decision",
            description = "Retrieves the poll associated with the specified decision. "
                    + "Access follows the visibility authorization of the parent decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PollResponse> getPoll(
            @PathVariable Long decisionId
    ) {

        log.info(
                "REST request to retrieve Poll for decision ID: {}",
                decisionId
        );

        PollResponse response =
                pollService.getPollByDecisionId(decisionId);

        return ResponseEntity.ok(response);
    }

    /**
     * Extends the voting end time of an active Poll.
     *
     * Only the creator/owner of the parent Decision may perform
     * this operation.
     */
    @PatchMapping("/end-time")
    @Operation(
            summary = "Extend poll voting end time",
            description = "Extends the voting end time of an OPEN poll. "
                    + "The new end time must be later than the current end time "
                    + "and must not exceed the parent decision deadline. "
                    + "Requires ownership of the parent decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PollResponse> extendPollEndTime(
            @PathVariable Long decisionId,
            @Valid @RequestBody UpdatePollEndTimeRequest request
    ) {

        log.info(
                "REST request to extend Poll end time for decision ID: {}",
                decisionId
        );

        PollResponse response =
                pollService.extendPollEndTime(decisionId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Closes an OPEN Poll before its configured end time.
     *
     * Only the creator/owner of the parent Decision may perform
     * this operation.
     */
    @PostMapping("/close")
    @Operation(
            summary = "Close poll early",
            description = "Closes an OPEN poll before its configured voting end time. "
                    + "Requires ownership of the parent decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PollResponse> closePoll(
            @PathVariable Long decisionId
    ) {

        log.info(
                "REST request to close Poll for decision ID: {}",
                decisionId
        );

        PollResponse response =
                pollService.closePoll(decisionId);

        return ResponseEntity.ok(response);
    }
}