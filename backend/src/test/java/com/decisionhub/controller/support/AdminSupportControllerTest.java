package com.decisionhub.controller.support;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.decisionhub.dto.request.support.UpdateSupportStatusRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.enums.support.SupportTicketStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.service.interfaces.support.SupportService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AdminSupportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupportService supportService;

    @MockBean
    private com.decisionhub.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.decisionhub.service.impl.authentication.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void getAllTickets_Returns200AndList() throws Exception {
        SupportResponse response = new SupportResponse();
        response.setId(10L);
        List<SupportResponse> responseList = List.of(response);

        when(supportService.getAllTickets()).thenReturn(responseList);

        mockMvc.perform(get("/api/admin/support"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getTicketById_Returns200AndResponse() throws Exception {
        Long ticketId = 10L;
        SupportResponse response = new SupportResponse();
        response.setId(ticketId);

        when(supportService.getTicketById(ticketId)).thenReturn(response);

        mockMvc.perform(get("/api/admin/support/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId));
    }

    @Test
    void getTicketById_NotFound_Returns404() throws Exception {
        Long ticketId = 99L;
        when(supportService.getTicketById(ticketId)).thenThrow(new ResourceNotFoundException("Ticket not found"));

        mockMvc.perform(get("/api/admin/support/{id}", ticketId))
                .andExpect(status().isNotFound()); // Assuming your GlobalExceptionHandler maps this to 404
    }

    @Test
    void updateTicketStatus_Returns200AndUpdatedResponse() throws Exception {
        Long ticketId = 10L;
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        request.setStatus(SupportTicketStatus.IN_PROGRESS);

        SupportResponse response = new SupportResponse();
        response.setId(ticketId);
        response.setStatus(SupportTicketStatus.IN_PROGRESS);

        when(supportService.updateTicketStatus(eq(ticketId), any(UpdateSupportStatusRequest.class), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/admin/support/{id}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "Admin-Agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTicketStatus_ValidationFails_Returns400() throws Exception {
        Long ticketId = 10L;
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        request.setStatus(SupportTicketStatus.IN_PROGRESS);

        when(supportService.updateTicketStatus(eq(ticketId), any(UpdateSupportStatusRequest.class), anyString(), anyString()))
                .thenThrow(new BadRequestException("Invalid transition"));

        mockMvc.perform(patch("/api/admin/support/{id}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "Admin-Agent"))
                .andExpect(status().isBadRequest()); // Assuming mapped to 400
    }

    @Test
    void updateTicketStatus_InvalidRequest_Returns400() throws Exception {
        // Missing the required status field
        UpdateSupportStatusRequest request = new UpdateSupportStatusRequest();
        
        mockMvc.perform(patch("/api/admin/support/10/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }
}