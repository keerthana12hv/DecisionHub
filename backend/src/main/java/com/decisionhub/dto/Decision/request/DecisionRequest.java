package com.decisionhub.dto;

import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.VotingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DecisionRequest(
    @NotBlank(message = "Decision title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    String title,

    String description,

    UUID categoryId,

    UUID communityId,

    boolean isPublic,

    @NotNull(message = "Voting type is required")
    VotingType votingType,

    @NotNull(message = "Anonymity type is required")
    AnonymityType anonymityType,

    Instant deadline,

    Set<String> tags,

    @NotEmpty(message = "At least two options are required")
    @Size(min = 2, message = "At least two options are required")
    List<@Valid OptionCreateDto> options,

    List<ComparisonFactorRequest> factors
) {}
