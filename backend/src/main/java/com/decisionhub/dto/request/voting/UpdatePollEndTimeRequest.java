package com.decisionhub.dto.request.voting;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO used to extend the voting end time of an active Poll.
 *
 * The initial voting end time is configured while the Decision is in DRAFT
 * status. Once the Decision is published and its Poll is created, subsequent
 * extensions to the voting period are managed through Poll Management.
 */
public record UpdatePollEndTimeRequest(

        @NotNull(message = "Poll end time is required")
        LocalDateTime endTime

) {}