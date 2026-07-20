package com.decisionhub.controller;

import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.service.interfaces.community.CommunityModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderation/decisions")
@RequiredArgsConstructor
@Tag(name = "Community Moderation", description = "Endpoints for community moderation actions on decisions")
public class CommunityModerationController {

    private final CommunityModerationService communityModerationService;

    @PutMapping("/{decisionId}/pin")
    @Operation(summary = "Pin decision", description = "Pins a decision in the community (requires moderator)")
    public ResponseEntity<DecisionResponse> pinDecision(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.pinDecision(decisionId));
    }

    @PutMapping("/{decisionId}/unpin")
    @Operation(summary = "Unpin decision", description = "Unpins a decision in the community (requires moderator)")
    public ResponseEntity<DecisionResponse> unpinDecision(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.unpinDecision(decisionId));
    }

    @PutMapping("/{decisionId}/lock")
    @Operation(summary = "Lock discussion", description = "Locks discussion on a decision (requires moderator)")
    public ResponseEntity<DecisionResponse> lockDiscussion(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.lockDiscussion(decisionId));
    }

    @PutMapping("/{decisionId}/unlock")
    @Operation(summary = "Unlock discussion", description = "Unlocks discussion on a decision (requires moderator)")
    public ResponseEntity<DecisionResponse> unlockDiscussion(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.unlockDiscussion(decisionId));
    }
}
