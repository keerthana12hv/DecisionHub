package com.decisionhub.service.impl.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class SupportServiceImplTest {

    @Mock private SupportRepository supportRepository;
    @Mock private UserRepository userRepository;
    @Mock private SupportMapper supportMapper;
    @Mock private AuthenticationFacade authenticationFacade;
    @Mock private AuditService auditService;

    @InjectMocks
    private SupportServiceImpl supportService;

    private User mockUser;
    private SupportTicket mockTicket;
    private final Long USER_ID = 1L;
    private final Long TICKET_ID = 100L;
    private final String IP_ADDRESS = "192.168.1.1";
    private final String USER_AGENT = "Mozilla/5.0";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(USER_ID);

        mockTicket = new SupportTicket();
        mockTicket.setId(TICKET_ID);
        mockTicket.setUser(mockUser);
        mockTicket.setCreatedAt(LocalDateTime.now());
    }

    // ==========================================
    // submitTicket() Tests
    // ==========================================

    @Test
    void submitTicket_BugReport_Success() {
        SupportRequest request = new SupportRequest();
        request.setType(SupportTicketType.BUG_REPORT);
        request.setSubject("Login Issue");
        request.setDescription("Cannot login with valid credentials");

        SupportResponse expectedResponse = new SupportResponse();
        expectedResponse.setId(TICKET_ID);

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportMapper.toEntity(request)).thenReturn(mockTicket);
        when(supportRepository.saveAndFlush(any(SupportTicket.class))).thenReturn(mockTicket);
        when(supportMapper.toResponse(mockTicket)).thenReturn(expectedResponse);

        SupportResponse result = supportService.submitTicket(request, IP_ADDRESS, USER_AGENT);

        assertNotNull(result);
        assertEquals(TICKET_ID, result.getId());
        assertEquals(SupportTicketStatus.OPEN, mockTicket.getStatus());
        assertNull(mockTicket.getRating());
        
        verify(auditService).log(eq(mockUser), eq("SUPPORT_TICKET_CREATED"), eq("support_ticket"), eq(TICKET_ID), isNull(), anyString(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    void submitTicket_GeneralFeedback_MissingRating_ThrowsBadRequest() {
        SupportRequest request = new SupportRequest();
        request.setType(SupportTicketType.GENERAL_FEEDBACK);
        request.setDescription("Great app!");
        request.setRating(null); 

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportMapper.toEntity(request)).thenReturn(mockTicket);

        assertThrows(BadRequestException.class, () -> 
            supportService.submitTicket(request, IP_ADDRESS, USER_AGENT)
        );
        verify(supportRepository, never()).saveAndFlush(any());
    }

    @Test
    void submitTicket_UserNotAuthenticated_ThrowsUnauthorized() {
        // Arrange: Provide a valid request to bypass the initial null checks
        SupportRequest request = new SupportRequest();
        request.setType(SupportTicketType.BUG_REPORT);
        request.setSubject("Login Issue");
        request.setDescription("Cannot login");

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedActionException.class, () -> 
            supportService.submitTicket(request, IP_ADDRESS, USER_AGENT)
        );
    }

    // ==========================================
    // updateTicketStatus() Tests
    // ==========================================

    @Test
    void updateTicketStatus_Success() {
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        request.setStatus(SupportTicketStatus.IN_PROGRESS);

        mockTicket.setType(SupportTicketType.BUG_REPORT);
        mockTicket.setStatus(SupportTicketStatus.OPEN);

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportRepository.findById(TICKET_ID)).thenReturn(Optional.of(mockTicket));
        when(supportRepository.saveAndFlush(any(SupportTicket.class))).thenReturn(mockTicket);
        
        SupportResponse expectedResponse = new SupportResponse();
        when(supportMapper.toResponse(mockTicket)).thenReturn(expectedResponse);

        SupportResponse result = supportService.updateTicketStatus(TICKET_ID, request, IP_ADDRESS, USER_AGENT);

        assertNotNull(result);
        assertEquals(SupportTicketStatus.IN_PROGRESS, mockTicket.getStatus());
        verify(auditService).log(eq(mockUser), eq("SUPPORT_TICKET_STATUS_UPDATED"), eq("support_ticket"), eq(TICKET_ID), anyString(), anyString(), eq(IP_ADDRESS), eq(USER_AGENT));
    }

    @Test
    void updateTicketStatus_NotBugReport_ThrowsBadRequest() {
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        request.setStatus(SupportTicketStatus.RESOLVED);

        mockTicket.setType(SupportTicketType.SUGGESTION); 

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportRepository.findById(TICKET_ID)).thenReturn(Optional.of(mockTicket));

        assertThrows(BadRequestException.class, () -> 
            supportService.updateTicketStatus(TICKET_ID, request, IP_ADDRESS, USER_AGENT)
        );
    }

    @Test
    void updateTicketStatus_SameStatus_ThrowsBadRequest() {
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        request.setStatus(SupportTicketStatus.OPEN);

        mockTicket.setType(SupportTicketType.BUG_REPORT);
        mockTicket.setStatus(SupportTicketStatus.OPEN); 

        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportRepository.findById(TICKET_ID)).thenReturn(Optional.of(mockTicket));

        assertThrows(BadRequestException.class, () -> 
            supportService.updateTicketStatus(TICKET_ID, request, IP_ADDRESS, USER_AGENT)
        );
    }

    // ==========================================
    // Retrieval Tests
    // ==========================================

    @Test
    void getMyTickets_Success() {
        when(authenticationFacade.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(supportRepository.findByUserOrderByCreatedAtDesc(mockUser)).thenReturn(List.of(mockTicket));
        when(supportMapper.toResponse(mockTicket)).thenReturn(new SupportResponse());

        List<SupportResponse> results = supportService.getMyTickets();

        assertEquals(1, results.size());
        verify(supportRepository).findByUserOrderByCreatedAtDesc(mockUser);
    }

    @Test
    void getAllTickets_Success() {
        when(supportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(mockTicket));
        when(supportMapper.toResponse(mockTicket)).thenReturn(new SupportResponse());

        List<SupportResponse> results = supportService.getAllTickets();

        assertEquals(1, results.size());
        verify(supportRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getTicketById_NotFound_ThrowsException() {
        when(supportRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            supportService.getTicketById(TICKET_ID)
        );
    }
}