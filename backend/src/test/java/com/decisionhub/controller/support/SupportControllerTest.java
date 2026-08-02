package com.decisionhub.controller.support;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.decisionhub.dto.request.support.SupportRequest;
import com.decisionhub.dto.response.support.SupportResponse;
import com.decisionhub.enums.support.SupportTicketType;
import com.decisionhub.exception.UnauthorizedActionException;
import com.decisionhub.service.interfaces.support.SupportService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SupportController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupportControllerTest {

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
    void submitTicket_Returns201AndResponse() throws Exception {
        SupportRequest request = new SupportRequest();
        request.setType(SupportTicketType.BUG_REPORT);
        request.setSubject("Test Subject");
        request.setDescription("Test Description");

        SupportResponse response = new SupportResponse();
        response.setId(1L);
        response.setSubject("Test Subject");

        when(supportService.submitTicket(any(SupportRequest.class), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "Test-Agent")
                .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    void submitTicket_ValidationFails_Returns400() throws Exception {
        // Missing required fields assuming @NotNull on DTO
        SupportRequest request = new SupportRequest();
        request.setSubject("No Type Subject");

        mockMvc.perform(post("/api/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyTickets_Returns200AndList() throws Exception {
        SupportResponse response = new SupportResponse();
        response.setId(1L);
        List<SupportResponse> responseList = List.of(response);

        when(supportService.getMyTickets()).thenReturn(responseList);

        mockMvc.perform(get("/api/support/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getMyTickets_ReturnsEmptyList() throws Exception {
        when(supportService.getMyTickets()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/support/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMyTickets_UserNotAuthenticated_Returns403() throws Exception {
        when(supportService.getMyTickets()).thenThrow(new UnauthorizedActionException("Not logged in"));

        mockMvc.perform(get("/api/support/my"))
                .andExpect(status().isForbidden()); // ✅ Changed from isUnauthorized() to isForbidden()
    }

    @Test
    void submitTicket_GeneralFeedback_Returns201() throws Exception {
        SupportRequest request = new SupportRequest();
        request.setType(SupportTicketType.GENERAL_FEEDBACK);
        request.setDescription("Nice application");
        request.setRating(5);

        SupportResponse response = new SupportResponse();
        response.setId(2L);
        response.setRating(5);

        when(supportService.submitTicket(any(SupportRequest.class), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "Test-Agent")
                .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rating").value(5));
    }
}