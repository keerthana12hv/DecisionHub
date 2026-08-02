package com.decisionhub.controller.community;

import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.dto.response.discussion.CommentResponse;
import com.decisionhub.service.interfaces.community.CommunityModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@Tag(name = "Community Moderation", description = "Endpoints for community moderation actions on decisions and comments")
public class CommunityModerationController {

    private final CommunityModerationService communityModerationService;

    @PutMapping("/decisions/{decisionId}/pin")
    @Operation(summary = "Pin decision", description = "Pins a decision in the community (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> pinDecision(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.pinDecision(decisionId));
    }

    @PutMapping("/decisions/{decisionId}/unpin")
    @Operation(summary = "Unpin decision", description = "Unpins a decision in the community (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> unpinDecision(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.unpinDecision(decisionId));
    }

    @PutMapping("/decisions/{decisionId}/lock")
    @Operation(summary = "Lock discussion", description = "Locks discussion on a decision (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> lockDiscussion(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.lockDiscussion(decisionId));
    }

    @PutMapping("/decisions/{decisionId}/unlock")
    @Operation(summary = "Unlock discussion", description = "Unlocks discussion on a decision (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> unlockDiscussion(@PathVariable Long decisionId) {
        return ResponseEntity.ok(communityModerationService.unlockDiscussion(decisionId));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete inappropriate comment", description = "Soft-deletes a comment (requires moderator or admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommentResponse> deleteComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(communityModerationService.deleteComment(commentId));
    }

    @PutMapping("/comments/{commentId}/pin")
    @Operation(summary = "Pin comment", description = "Pins a comment to the top of a decision discussion (requires moderator or admin). Only one comment can be pinned at a time.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommentResponse> pinComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(communityModerationService.pinComment(commentId));
    }

    @PutMapping("/comments/{commentId}/unpin")
    @Operation(summary = "Unpin comment", description = "Unpins a comment from a decision discussion (requires moderator or admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommentResponse> unpinComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(communityModerationService.unpinComment(commentId));
    }

    @GetMapping("/decisions/{decisionId}/comments/pinned")
    @Operation(summary = "Get pinned comment", description = "Retrieves the active pinned comment on a decision discussion. Returns 204 No Content if none is pinned.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommentResponse> getPinnedComment(@PathVariable Long decisionId) {
        return communityModerationService.getPinnedComment(decisionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
