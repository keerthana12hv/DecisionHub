package com.decisionhub.controller;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.service.interfaces.DecisionOptionService;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/decisions/{decisionId}/options")
@RequiredArgsConstructor
@Tag(name = "Decision Option", description = "Decision Options Management Endpoints")
@Slf4j
public class DecisionOptionController {

    private final DecisionOptionService decisionOptionService;

    @PostMapping
    @Operation(summary = "Add option to decision", description = "Creates and adds a new option to a draft decision (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OptionResponseDto> createOption(
            @PathVariable Long decisionId,
            @Valid @RequestBody OptionCreateDto request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to create option on decision: {}", decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        OptionResponseDto response = decisionOptionService.createOption(decisionId, request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{optionId}")
    @Operation(summary = "Update decision option", description = "Updates title and description of an existing option (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OptionResponseDto> updateOption(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @Valid @RequestBody OptionCreateDto request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update option: {} on decision: {}", optionId, decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        OptionResponseDto response = decisionOptionService.updateOption(decisionId, optionId, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{optionId}")
    @Operation(summary = "Delete decision option", description = "Deletes an option from a draft decision (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteOption(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete option: {} on decision: {}", optionId, decisionId);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        decisionOptionService.deleteOption(decisionId, optionId, ipAddress, userAgent);
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
