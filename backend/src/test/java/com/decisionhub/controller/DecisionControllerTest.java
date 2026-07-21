package com.decisionhub.controller;

import com.decisionhub.config.JwtService;
import com.decisionhub.controller.decision.DecisionController;
import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import com.decisionhub.service.interfaces.decision.DecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DecisionController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DecisionService decisionService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private DecisionRequest request;
    private DecisionResponse response;

    @BeforeEach
    void setUp() {
        request = new DecisionRequest(
            "Test Decision Title",
            "Test Description",
            null,
            null,
            true,
            null,
            null,
            LocalDateTime.now().plusDays(2),
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        response = new DecisionResponse(
            1L,
            "Test Decision Title",
            "Test Description",
            null,
            null,
            null,
            DecisionStatus.DRAFT,
            request.deadline(),
            Collections.emptyList(),
            Collections.emptyList(),
            LocalDateTime.now(),
            false,
            false
        );
    }

    @Test
    void createDecision_withValidPayload_returnsCreated() throws Exception {
        when(decisionService.createDecision(any(DecisionRequest.class), anyString(), any())).thenReturn(response);

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Decision Title"));
    }

    @Test
    void createDecision_withBlankTitle_returnsBadRequest() throws Exception {
        DecisionRequest invalidRequest = new DecisionRequest(
            "", "Desc", null, null, true, null, null, null, null, Collections.emptyList(), Collections.emptyList()
        );

        mockMvc.perform(post("/api/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllDecisions_returnsOk() throws Exception {
        when(decisionService.getAllDecisions(any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Decision Title"));
    }

    @Test
    void getDecisionById_returnsOk() throws Exception {
        when(decisionService.getDecisionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/decisions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Decision Title"));
    }

    @Test
    void updateDecision_withValidPayload_returnsOk() throws Exception {
        when(decisionService.updateDecision(eq(1L), any(DecisionRequest.class), anyString(), any())).thenReturn(response);

        mockMvc.perform(put("/api/decisions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Decision Title"));
    }

    @Test
    void deleteDecision_returnsNoContent() throws Exception {
        doNothing().when(decisionService).deleteDecision(eq(1L), anyString(), any());

        mockMvc.perform(delete("/api/decisions/1"))
                .andExpect(status().isNoContent());
    }
}
