package com.decisionhub.controller;

import com.decisionhub.dto.request.community.CommunityRuleRequest;
import com.decisionhub.dto.response.community.CommunityRuleResponse;
import com.decisionhub.service.interfaces.community.CommunityRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Community Rule Management", description = "Endpoints for managing community rules")
public class CommunityRuleController {

    private final CommunityRuleService communityRuleService;

    @PostMapping("/api/moderation/communities/{communityId}/rules")
    @Operation(summary = "Create community rule", description = "Creates a new rule for a community (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommunityRuleResponse> createRule(
            @PathVariable Long communityId,
            @Valid @RequestBody CommunityRuleRequest request
    ) {
        return new ResponseEntity<>(communityRuleService.createRule(communityId, request), HttpStatus.CREATED);
    }

    @PutMapping("/api/moderation/rules/{ruleId}")
    @Operation(summary = "Update community rule", description = "Updates an existing community rule (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommunityRuleResponse> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody CommunityRuleRequest request
    ) {
        return ResponseEntity.ok(communityRuleService.updateRule(ruleId, request));
    }

    @DeleteMapping("/api/moderation/rules/{ruleId}")
    @Operation(summary = "Delete community rule", description = "Deletes a community rule (requires moderator)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        communityRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/communities/{communityId}/rules")
    @Operation(summary = "Get community rules", description = "Retrieves all rules of a community")
    public ResponseEntity<List<CommunityRuleResponse>> getRulesByCommunity(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityRuleService.getRulesByCommunity(communityId));
    }
}
