package com.decisionhub.controller.decision;

import com.decisionhub.dto.response.decision.RankingResponse;
import com.decisionhub.dto.response.decision.RankingSummaryResponse;
import com.decisionhub.service.interfaces.decision.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for the Collaborative Decision Ranking Engine.
 */
@RestController
@RequestMapping("/api/decisions/{decisionId}/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "Collaborative Decision Ranking Engine Endpoints")
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    @Operation(summary = "Get final collaborative ranking", description = "Calculates and retrieves the full collaborative decision ranking with factor breakdowns (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RankingResponse> getRanking(
            @PathVariable Long decisionId
    ) {
        log.info("REST request to calculate full ranking for decision: {}", decisionId);
        RankingResponse response = rankingService.getRanking(decisionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get condensed collaborative ranking summary", description = "Calculates and retrieves a condensed summary of the decision ranking (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RankingSummaryResponse> getRankingSummary(
            @PathVariable Long decisionId
    ) {
        log.info("REST request to calculate summary ranking for decision: {}", decisionId);
        RankingSummaryResponse response = rankingService.getRankingSummary(decisionId);
        return ResponseEntity.ok(response);
    }
}