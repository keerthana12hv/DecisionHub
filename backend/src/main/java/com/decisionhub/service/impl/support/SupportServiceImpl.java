package com.decisionhub.service.impl.support;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.dto.request.support.SupportRequest;
import com.decisionhub.dto.request.support.UpdateSupportStatusRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.support.SupportTicket;
import com.decisionhub.enums.support.SupportTicketStatus;
import com.decisionhub.enums.support.SupportTicketType;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.mapper.support.SupportMapper;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.support.SupportRepository;
import com.decisionhub.security.decision.AuthenticationFacade;
import com.decisionhub.service.interfaces.audit.AuditService;
import com.decisionhub.service.interfaces.support.SupportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final SupportRepository supportRepository;
    private final UserRepository userRepository;
    private final SupportMapper supportMapper;
    
    private final AuthenticationFacade authenticationFacade;
    private final AuditService auditService;

    // --- Helper Methods ---
    private Long getCurrentUserIdOrThrow() {
        return authenticationFacade.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));
    }

    private void validateBugReport(SupportRequest request) {
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new BadRequestException("Subject is required for Bug Reports.");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description is required for Bug Reports.");
        }
        if (request.getRating() != null) {
            throw new BadRequestException("Rating is not applicable for Bug Reports.");
        }
    }

    private void validateSuggestion(SupportRequest request) {
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new BadRequestException("Subject is required for Suggestions.");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description is required for Suggestions.");
        }
        if (request.getRating() != null) {
            throw new BadRequestException("Rating is not applicable for Suggestions.");
        }
    }

    private void validateGeneralFeedback(SupportRequest request) {
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description is required for General Feedback.");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("A valid rating (1-5) is required for General Feedback.");
        }
    }

    // --- 1. submitTicket() ---
    @Override
    @Transactional
    public SupportResponse submitTicket(SupportRequest request, String ipAddress, String userAgent) {
        // Defensive check against missing enum type mapping
        if (request.getType() == null) {
            throw new BadRequestException("Support ticket type is required.");
        }

        log.info("Attempting to submit {} support ticket.", request.getType());

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        SupportTicket ticket = supportMapper.toEntity(request);

        switch (request.getType()) {
            case BUG_REPORT:
                validateBugReport(request);
                ticket.setRating(null);
                ticket.setStatus(SupportTicketStatus.OPEN);
                break;

            case SUGGESTION:
                validateSuggestion(request);
                ticket.setRating(null);
                ticket.setStatus(null);
                break;

            case GENERAL_FEEDBACK:
                validateGeneralFeedback(request);
                ticket.setSubject(null);
                ticket.setStatus(null);
                break;
                
            default:
                throw new BadRequestException("Unsupported support ticket type.");
        }

        ticket.setUser(currentUser);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(ticket.getCreatedAt());

        SupportTicket savedTicket = supportRepository.saveAndFlush(ticket);

        auditService.log(
                currentUser,
                "SUPPORT_TICKET_CREATED",
                "support_ticket", 
                savedTicket.getId(),
                null,
                "Type: " + savedTicket.getType(),
                ipAddress,
                userAgent
        );

        log.info("Successfully created support ticket with ID: {}", savedTicket.getId());
        return supportMapper.toResponse(savedTicket);
    }

    // --- 2. getMyTickets() ---
    @Override
    @Transactional(readOnly = true)
    public List<SupportResponse> getMyTickets() {
        Long currentUserId = getCurrentUserIdOrThrow();
        log.info("Retrieving support tickets for user ID: {}", currentUserId);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        return supportRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(supportMapper::toResponse)
                .toList();
    }

    // --- 3. getAllTickets() ---
    @Override
    @Transactional(readOnly = true)
    public List<SupportResponse> getAllTickets() {
        log.info("Admin retrieving all support tickets.");
        
        return supportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(supportMapper::toResponse)
                .toList();
    }

    // --- 4. getTicketById() ---
    @Override
    @Transactional(readOnly = true)
    public SupportResponse getTicketById(Long id) {
        log.info("Retrieving support ticket with ID: {}", id);
        
        SupportTicket ticket = supportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));
                
        return supportMapper.toResponse(ticket);
    }

    // --- 5. updateTicketStatus() ---
    @Override
    @Transactional
    public SupportResponse updateTicketStatus(Long id, UpdateSupportStatusRequest request, String ipAddress, String userAgent) {
        log.info("Admin attempting to update status for ticket ID: {}", id);

        Long currentUserId = getCurrentUserIdOrThrow();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        SupportTicket ticket = supportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));

        if (ticket.getType() != SupportTicketType.BUG_REPORT) {
            throw new BadRequestException("Only Bug Reports have a status lifecycle. Suggestions and Feedback cannot be updated.");
        }
        
        // Strict lifecycle enforcement
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) {
            throw new BadRequestException("This ticket is already resolved and its status cannot be modified.");
        }
        if (ticket.getStatus() == request.getStatus()) {
            throw new BadRequestException("Support ticket is already in the requested status.");
        }
        
        String oldStatus = ticket.getStatus() != null ? ticket.getStatus().name() : "null";
        
        ticket.setStatus(request.getStatus());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        SupportTicket updatedTicket = supportRepository.saveAndFlush(ticket);

        auditService.log(
                currentUser,
                "SUPPORT_TICKET_STATUS_UPDATED",
                "support_ticket",
                updatedTicket.getId(),
                "Status: " + oldStatus,
                "Status: " + updatedTicket.getStatus().name(),
                ipAddress,
                userAgent
        );

        log.info("Successfully updated status for ticket ID: {} to {}", id, updatedTicket.getStatus());
        return supportMapper.toResponse(updatedTicket);
    }
}