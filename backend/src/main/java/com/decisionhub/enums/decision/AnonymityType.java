package com.decisionhub.enums.decision;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Defines the anonymity level of votes cast in this decision. PUBLIC exposes voter details, while ANONYMOUS shields voter identities.",
    example = "PUBLIC"
)
public enum AnonymityType {
    @Schema(description = "All user scores and votes are publicly attributed to their user profile")
    PUBLIC,

    @Schema(description = "User identities are shielded; only aggregate and anonymous scores/votes are visible")
    ANONYMOUS
}
