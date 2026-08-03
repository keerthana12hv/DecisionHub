package com.decisionhub.controller.support;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.decisionhub.dto.request.support.UpdateSupportStatusRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.service.interfaces.support.SupportService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportService supportService;

    /**
     * Retrieve all support tickets (Admin view).
     */
    @GetMapping
    public ResponseEntity<List<SupportResponse>> getAllTickets() {
        log.info("REST request to retrieve all support tickets for Admin view");
        return ResponseEntity.ok(supportService.getAllTickets());
    }

    /**
     * Retrieve a specific support ticket by its ID (Admin view).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupportResponse> getTicketById(@PathVariable Long id) {
        log.info("REST request to retrieve support ticket by ID: {}", id);
        return ResponseEntity.ok(supportService.getTicketById(id));
    }

    /**
     * Update the status of a specific Bug Report ticket.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SupportResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportStatusRequest request,
            HttpServletRequest httpRequest) {

        log.info("REST request to update status for support ticket ID: {}", id);

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        SupportResponse response = supportService.updateTicketStatus(id, request, ipAddress, userAgent);

        return ResponseEntity.ok(response);
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