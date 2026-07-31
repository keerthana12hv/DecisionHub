package com.decisionhub.controller.decision;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.decision.DecisionFeedbackRequest;
import com.decisionhub.dto.response.decision.DecisionFeedbackResponse;
import com.decisionhub.service.interfaces.decision.DecisionFeedbackService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DecisionFeedbackController {

    private final DecisionFeedbackService decisionFeedbackService;

    /**
     * Submit feedback for a closed decision.
     */
    @PostMapping("/decisions/{decisionId}/feedback")
    public ResponseEntity<DecisionFeedbackResponse> submitFeedback(
            @PathVariable Long decisionId,
            @Valid @RequestBody DecisionFeedbackRequest request,
            HttpServletRequest httpRequest) {

        log.info("REST request to submit feedback for decision ID: {}", decisionId);

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        DecisionFeedbackResponse response = decisionFeedbackService.submitFeedback(
                decisionId, 
                request, 
                ipAddress, 
                userAgent
        );

        // Updated to return 201 Created instead of 200 OK
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get feedback for a specific decision. (Owner only)
     */
    @GetMapping("/decisions/{decisionId}/feedback")
    public ResponseEntity<DecisionFeedbackResponse> getFeedbackByDecision(
            @PathVariable Long decisionId) {
        
        log.info("REST request to get feedback for decision ID: {}", decisionId);
        
        return ResponseEntity.ok(decisionFeedbackService.getFeedbackByDecision(decisionId));
    }

    /**
     * Get all decision feedbacks. (Admin only)
     */
    @GetMapping("/admin/decision-feedback")
    public ResponseEntity<List<DecisionFeedbackResponse>> getAllFeedback() {
        
        log.info("REST request to retrieve all decision feedbacks (Admin view)");
        
        return ResponseEntity.ok(decisionFeedbackService.getAllFeedback());
    }

    /**
     * Helper method to accurately extract the client's IP Address.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}