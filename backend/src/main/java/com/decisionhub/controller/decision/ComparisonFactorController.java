package com.decisionhub.controller.decision;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.service.interfaces.decision.ComparisonFactorService;

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
 * REST controller exposing endpoints for managing comparison factors.
 */
@RestController
@RequestMapping("/api/decisions/{decisionId}/factors")
@RequiredArgsConstructor
@Tag(name = "Comparison Factor", description = "Decision Comparison Factors Management Endpoints")
@Slf4j
public class ComparisonFactorController {

    private final ComparisonFactorService comparisonFactorService;

    @PostMapping
    @Operation(summary = "Add comparison factor to decision", description = "Creates and adds a new evaluation factor to a draft decision (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ComparisonFactorResponse> createFactor(
            @PathVariable Long decisionId,
            @Valid @RequestBody ComparisonFactorRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to create comparison factor on decision: {}", decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        ComparisonFactorResponse response = comparisonFactorService.createFactor(decisionId, request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{factorId}")
    @Operation(summary = "Update comparison factor", description = "Updates name and description of an existing factor (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ComparisonFactorResponse> updateFactor(
            @PathVariable Long decisionId,
            @PathVariable Long factorId,
            @Valid @RequestBody ComparisonFactorRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update factor: {} on decision: {}", factorId, decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        ComparisonFactorResponse response = comparisonFactorService.updateFactor(decisionId, factorId, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{factorId}")
    @Operation(summary = "Delete comparison factor", description = "Deletes a factor from a draft decision (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteFactor(
            @PathVariable Long decisionId,
            @PathVariable Long factorId,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete factor: {} on decision: {}", factorId, decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        comparisonFactorService.deleteFactor(decisionId, factorId, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{factorId}")
    @Operation(summary = "Get comparison factor details", description = "Retrieves a single comparison factor by ID (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ComparisonFactorResponse> getFactor(
            @PathVariable Long decisionId,
            @PathVariable Long factorId
    ) {
        log.info("REST request to fetch factor: {} for decision: {}", factorId, decisionId);
        ComparisonFactorResponse response = comparisonFactorService.getFactor(decisionId, factorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get comparison factors", description = "Retrieves all comparison factors for a decision (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ComparisonFactorResponse>> getFactorsByDecisionId(
            @PathVariable Long decisionId
    ) {
        log.info("REST request to fetch all factors for decision: {}", decisionId);
        List<ComparisonFactorResponse> response = comparisonFactorService.getFactorsByDecisionId(decisionId);
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
