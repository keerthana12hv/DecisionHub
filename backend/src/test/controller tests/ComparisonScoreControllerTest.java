package com.decisionhub.controller;

import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.service.ComparisonScoreService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ComparisonScoreController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class ComparisonScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComparisonScoreService comparisonScoreService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void submitScore_withValidPayload_returnsOk() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID factorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 85, "Good");
        ComparisonScoreResponse response = new ComparisonScoreResponse(optionId, factorId, userId, 85, "Good", Instant.now(), Instant.now());

        when(comparisonScoreService.submitScore(eq(decisionId), any(ComparisonScoreRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.remarks").value("Good"));
    }

    @Test
    void submitScore_withNullOption_returnsBadRequest() throws Exception {
        UUID decisionId = UUID.randomUUID();
        ComparisonScoreRequest request = new ComparisonScoreRequest(null, UUID.randomUUID(), 85, "Good");

        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateScore_withValidPayload_returnsOk() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID factorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 90, "Better");
        ComparisonScoreResponse response = new ComparisonScoreResponse(optionId, factorId, userId, 90, "Better", Instant.now(), Instant.now());

        when(comparisonScoreService.updateScore(eq(decisionId), eq("score123"), any(ComparisonScoreRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/decisions/{decisionId}/scores/{scoreId}", decisionId, "score123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(90))
                .andExpect(jsonPath("$.remarks").value("Better"));
    }

    @Test
    void getScores_returnsList() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID factorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ComparisonScoreResponse response = new ComparisonScoreResponse(optionId, factorId, userId, 75, "Ok", Instant.now(), Instant.now());

        when(comparisonScoreService.getScoresByDecisionId(eq(decisionId)))
                .thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/decisions/{decisionId}/scores", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(75))
                .andExpect(jsonPath("$[0].remarks").value("Ok"));
    }

    @Test
    void getMyScores_returnsList() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID factorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ComparisonScoreResponse response = new ComparisonScoreResponse(optionId, factorId, userId, 95, "Amazing", Instant.now(), Instant.now());

        when(comparisonScoreService.getMyScoresByDecisionId(eq(decisionId)))
                .thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/decisions/{decisionId}/scores/me", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(95))
                .andExpect(jsonPath("$[0].remarks").value("Amazing"));
    }

    @Test
    void deleteScore_returnsNoContent() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID factorId = UUID.randomUUID();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/decisions/{decisionId}/scores/{optionId}/{factorId}", decisionId, optionId, factorId))
                .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(comparisonScoreService).deleteScore(eq(decisionId), eq(optionId), eq(factorId), any(), any());
    }
}
