package com.decisionhub.controller;

import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.config.JwtService;
import com.decisionhub.controller.decision.ComparisonFactorController;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import com.decisionhub.service.interfaces.decision.ComparisonFactorService;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ComparisonFactorController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class ComparisonFactorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComparisonFactorService comparisonFactorService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void createFactor_withValidPayload_returnsCreated() throws Exception {
        Long decisionId = 1L;
        Long factorId = 2L;
        ComparisonFactorRequest request = new ComparisonFactorRequest("Cost", "Cost factor");
        ComparisonFactorResponse response = new ComparisonFactorResponse(factorId, decisionId, "Cost", "Cost factor", Instant.now(), Instant.now(), 0L);

        when(comparisonFactorService.createFactor(eq(decisionId), any(ComparisonFactorRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(factorId))
                .andExpect(jsonPath("$.name").value("Cost"));
    }

    @Test
    void createFactor_withBlankName_returnsBadRequest() throws Exception {
        Long decisionId = 1L;
        ComparisonFactorRequest request = new ComparisonFactorRequest("", "Description");

        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFactor_withValidPayload_returnsOk() throws Exception {
        Long decisionId = 1L;
        Long factorId = 2L;
        ComparisonFactorRequest request = new ComparisonFactorRequest("Security", "Security factor");
        ComparisonFactorResponse response = new ComparisonFactorResponse(factorId, decisionId, "Security", "Security factor", Instant.now(), Instant.now(), 0L);

        when(comparisonFactorService.updateFactor(eq(decisionId), eq(factorId), any(ComparisonFactorRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Security"));
    }

    @Test
    void deleteFactor_returnsNoContent() throws Exception {
        Long decisionId = 1L;
        Long factorId = 2L;

        doNothing().when(comparisonFactorService).deleteFactor(eq(decisionId), eq(factorId), any(), any());

        mockMvc.perform(delete("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFactors_returnsList() throws Exception {
        Long decisionId = 1L;
        Long factorId = 2L;
        ComparisonFactorResponse response = new ComparisonFactorResponse(factorId, decisionId, "Performance", "Performance factor", Instant.now(), Instant.now(), 0L);

        when(comparisonFactorService.getFactorsByDecisionId(eq(decisionId)))
                .thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/decisions/{decisionId}/factors", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(factorId))
                .andExpect(jsonPath("$[0].name").value("Performance"));
    }
}
