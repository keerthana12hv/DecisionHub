package com.decisionhub.controller;

import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.service.ComparisonScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing endpoints for managing comparison scores.
 */
@RestController
@RequestMapping("/decisions/{decisionId}/scores")
@RequiredArgsConstructor
@Tag(name = "Comparison Score", description = "Decision Comparison Scores Management Endpoints")
@Slf4j
public class ComparisonScoreController {

    private final ComparisonScoreService comparisonScoreService;

    @PostMapping
    @Operation(summary = "Submit or update comparison score", description = "Submits a new evaluation score (0-100) or updates it if it exists (requires active decision view permission)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ComparisonScoreResponse> submitScore(
            @PathVariable Long decisionId,
            @Valid @RequestBody ComparisonScoreRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to submit comparison score on decision: {}", decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        ComparisonScoreResponse response = comparisonScoreService.submitScore(decisionId, request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{scoreId}")
    @Operation(summary = "Update comparison score by key", description = "Updates an existing evaluation score for specified option and factor (requires active decision view permission)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ComparisonScoreResponse> updateScore(
            @PathVariable Long decisionId,
            @PathVariable String scoreId,
            @Valid @RequestBody ComparisonScoreRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update comparison score with ID placeholder: {} on decision: {}", scoreId, decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        ComparisonScoreResponse response = comparisonScoreService.updateScore(decisionId, scoreId, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{optionId}/{factorId}")
    @Operation(summary = "Delete comparison score", description = "Deletes an existing comparison score submitted by the current authenticated user (requires active decision view permission)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteScore(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @PathVariable Long factorId,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete comparison score on decision: {} for option: {} and factor: {}", decisionId, optionId, factorId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        comparisonScoreService.deleteScore(decisionId, optionId, factorId, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get comparison scores", description = "Retrieves all comparison scores for a decision (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ComparisonScoreResponse>> getScoresByDecisionId(
            @PathVariable Long decisionId
    ) {
        log.info("REST request to fetch all scores for decision: {}", decisionId);
        List<ComparisonScoreResponse> response = comparisonScoreService.getScoresByDecisionId(decisionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my comparison scores", description = "Retrieves comparison scores submitted by the current authenticated user on this decision (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ComparisonScoreResponse>> getMyScoresByDecisionId(
            @PathVariable Long decisionId
    ) {
        log.info("REST request to fetch current user's scores for decision: {}", decisionId);
        List<ComparisonScoreResponse> response = comparisonScoreService.getMyScoresByDecisionId(decisionId);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
