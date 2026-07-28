package com.decisionhub.security;

import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.VotingType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DecisionRefactoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reg = new RegisterRequest("refactoruser", "refactor@test.com", "Password123!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("refactor@test.com", "Password123!");
        String responseStr = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readValue(responseStr, LoginResponse.class).token();
    }

    @Test
    void testCreateDecision_Validation_Failures() throws Exception {
        // 1. VotingType is missing
        String missingVotingTypeJson = "{\"title\":\"Missing Voting Type\",\"description\":\"Desc\",\"deadline\":\"2030-01-01T12:00:00\",\"votingEndTime\":\"2029-01-01T12:00:00\"}";
        mockMvc.perform(post("/api/decisions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingVotingTypeJson))
                .andExpect(status().isBadRequest());

        // 2. VotingEndTime must be before Deadline
        DecisionRequest invalidTimeRequest = new DecisionRequest(
                "Invalid Times",
                "Desc",
                null,
                false,
                VotingType.SINGLE_CHOICE,
                null,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(4), // deadline is before voting end time
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        mockMvc.perform(post("/api/decisions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTimeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPublishOrchestration_Validation() throws Exception {
        // 1. Create a draft decision (VotingType = RATING_BASED)
        DecisionRequest request = new DecisionRequest(
                "Draft Rating Decision",
                "Desc",
                null,
                false,
                VotingType.RATING_BASED,
                null,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );

        String createStr = mockMvc.perform(post("/api/decisions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        DecisionResponse decision = objectMapper.readValue(createStr, DecisionResponse.class);

        // 2. Try publishing immediately -> should fail since it has no options (Rule 5 requires at least 2)
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/publish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest());

        // 3. Add 2 options
        String option1 = "{\"title\":\"Option A\",\"description\":\"A\"}";
        String option2 = "{\"title\":\"Option B\",\"description\":\"B\"}";
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/options")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(option1))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/options")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(option2))
                .andExpect(status().isCreated());

        // 4. Try publishing rating-based -> should fail because it has no comparison factors (Rule 6)
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/publish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest());

        // 5. Add 1 factor
        String factor = "{\"name\":\"Factor 1\",\"description\":\"F1\",\"weight\":1}";
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/factors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(factor))
                .andExpect(status().isCreated());

        // 6. Publish successfully
        mockMvc.perform(post("/api/decisions/" + decision.id() + "/publish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 7. Try updating locked configuration -> should fail
        DecisionRequest updateRequest = new DecisionRequest(
                "Draft Rating Decision - Updated",
                "Desc",
                null,
                false,
                VotingType.SINGLE_CHOICE, // modified type
                null,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        mockMvc.perform(put("/api/decisions/" + decision.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}
