package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.VotingType;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.ComparisonScoreRepository;
import com.decisionhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/decisionhub_test",
    "spring.datasource.username=decisionhub_app",
    "spring.datasource.password=dh_dev_sec_pwd_2026",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionBoardRepository decisionBoardRepository;

    @Autowired
    private DecisionOptionRepository decisionOptionRepository;

    @Autowired
    private ComparisonFactorRepository comparisonFactorRepository;

    @Autowired
    private ComparisonScoreRepository comparisonScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String otherUserToken;
    private UUID creatorId;
    private UUID otherUserId;

    private UUID decisionId;
    private UUID optionId;
    private UUID factorId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database tables
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionBoardRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        // 1. Register & Login Creator
        UserRegisterRequest creatorReg = new UserRegisterRequest(
                "creatoruser", "creator@test.com", "Password123!", "Creator", "User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorReg)))
                .andExpect(status().isCreated());

        UserLoginRequest creatorLogin = new UserLoginRequest("creatoruser", "Password123!");
        String creatorLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        creatorToken = objectMapper.readValue(creatorLoginResponse, AuthTokenResponse.class).accessToken();
        creatorId = userRepository.findByUsername("creatoruser").orElseThrow().getId();

        // 2. Register & Login Other User
        UserRegisterRequest otherReg = new UserRegisterRequest(
                "otheruser", "other@test.com", "Password123!", "Other", "User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherReg)))
                .andExpect(status().isCreated());

        UserLoginRequest otherLogin = new UserLoginRequest("otheruser", "Password123!");
        String otherLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        otherUserToken = objectMapper.readValue(otherLoginResponse, AuthTokenResponse.class).accessToken();
        otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();

        // 3. Create a DRAFT Decision Board
        DecisionRequest createRequest = new DecisionRequest(
                "Framework Comparison", "Select a backend Java framework", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                new HashSet<>(Arrays.asList("java", "framework")),
                Arrays.asList(new OptionCreateDto("Spring Boot", "Enterprise stack", Collections.emptyList())),
                Collections.emptyList()
        );

        String createResponseJson = mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        DecisionResponse createdDecision = objectMapper.readValue(createResponseJson, DecisionResponse.class);
        decisionId = createdDecision.id();

        optionId = decisionOptionRepository.findByDecisionIdAndDeletedAtIsNull(decisionId).get(0).getId();

        // 4. Create a Comparison Factor
        ComparisonFactorRequest factorRequest = new ComparisonFactorRequest("Performance", "Execution speed");
        String factorResponseJson = mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(factorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        factorId = UUID.fromString(objectMapper.readTree(factorResponseJson).get("id").asText());
    }

    @Test
    void testGetRanking_BoardNotActive_ReturnsBadRequest() throws Exception {
        // Board is in DRAFT state
        mockMvc.perform(get("/decisions/{decisionId}/ranking", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRanking_Success() throws Exception {
        // 1. Transition board to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        // 2. Submit score as Creator
        ComparisonScoreRequest requestCreator = new ComparisonScoreRequest(optionId, factorId, 90, "Excellent performance");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreator)))
                .andExpect(status().isOk());

        // 3. Submit score as Other User
        ComparisonScoreRequest requestOther = new ComparisonScoreRequest(optionId, factorId, 80, "Good performance");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestOther)))
                .andExpect(status().isOk());

        // 4. Retrieve Ranking (Average should be (90 + 80) / 2 = 85.0)
        mockMvc.perform(get("/decisions/{decisionId}/ranking", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(decisionId.toString()))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(85.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].averageScore").value(85.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].weight").value(1.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].weightedScore").value(85.0));

        // 5. Retrieve Ranking Summary
        mockMvc.perform(get("/decisions/{decisionId}/ranking/summary", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(decisionId.toString()))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(85.0));
    }

    @Test
    void testGetRanking_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/decisions/{decisionId}/ranking", decisionId))
                .andExpect(status().isUnauthorized());
    }
}
