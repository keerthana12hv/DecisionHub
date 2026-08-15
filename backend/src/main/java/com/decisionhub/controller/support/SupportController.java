package com.decisionhub.controller.support;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.support.SupportRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.service.interfaces.support.SupportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    /**
     * Submit a new support ticket (Bug Report, Suggestion, or General Feedback).
     */
    @PostMapping
    public ResponseEntity<SupportResponse> submitTicket(
            @Valid @RequestBody SupportRequest request,
            HttpServletRequest httpRequest) {

        log.info("REST request to submit a support ticket of type: {}", request.getType());

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        SupportResponse response = supportService.submitTicket(request, ipAddress, userAgent);
        
        log.info("Support ticket submitted successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve all support tickets submitted by the currently authenticated user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<SupportResponse>> getMyTickets() {
        log.info("REST request to retrieve current user's support tickets");
        
        List<SupportResponse> tickets = supportService.getMyTickets();
        log.info("Retrieved {} support tickets for current user.", tickets.size());
        
        return ResponseEntity.ok(tickets);
    }

    /**
     * Helper method to accurately extract the client's IP Address.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isBlank()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}