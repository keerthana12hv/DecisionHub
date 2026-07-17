package com.decisionhub.controller.decision;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.service.interfaces.decision.DecisionService;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for Decision CRUD (Sprint 6).
 */
@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
@Tag(name = "Decision CRUD", description = "Endpoints for managing decisions")
@Slf4j
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping
    @Operation(summary = "Create a decision", description = "Creates a new decision (requires authentication)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody DecisionRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to create decision: {}", request.title());
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.createDecision(request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get decisions", description = "Retrieves all decisions matching the filters (requires view authorization for each)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DecisionResponse>> getAllDecisions(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean mine
    ) {
        log.info("REST request to get decisions with filters: communityId={}, status={}, mine={}", communityId, status, mine);
        List<DecisionResponse> response = decisionService.getAllDecisions(communityId, status, mine);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get decision details", description = "Retrieves decision details by ID (requires view authorization)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> getDecisionById(
            @PathVariable Long id
    ) {
        log.info("REST request to get decision: {}", id);
        DecisionResponse response = decisionService.getDecisionById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update decision details", description = "Updates an existing decision details (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update decision: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.updateDecision(id, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish Decision", description = "Changes a decision from DRAFT to ACTIVE. A published decision can receive comparison scores.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> publishDecision(
            @PathVariable Long id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to publish decision: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.publishDecision(id, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a decision", description = "Deletes a decision and associated entities (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteDecision(
            @PathVariable Long id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete decision: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        decisionService.deleteDecision(id, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}