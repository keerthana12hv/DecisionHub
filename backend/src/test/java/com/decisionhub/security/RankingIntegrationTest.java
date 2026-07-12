package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.AuditLogRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionRepository decisionRepository;

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
    private Long creatorId;
    private Long otherUserId;

    private Long decisionId;
    private Long optionId;
    private Long factorId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database tables
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register & Login Creator (UPDATED FOR RECORDS)
        RegisterRequest creatorReg = new RegisterRequest("creatoruser", "creator@test.com", "Password123!");
        
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorReg)))
                .andExpect(status().isOk());

        LoginRequest creatorLogin = new LoginRequest("creator@test.com", "Password123!");
        
        String creatorLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
                
        // Changed .getToken() to .token()
        creatorToken = objectMapper.readValue(creatorLoginResponse, LoginResponse.class).token();
        User creatorUser = userRepository.findByUsername("creatoruser").orElseThrow();
        creatorId = creatorUser.getId();

        // 2. Register & Login Other User (UPDATED FOR RECORDS)
        RegisterRequest otherReg = new RegisterRequest("otheruser", "other@test.com", "Password123!");
        
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherReg)))
                .andExpect(status().isOk());

        LoginRequest otherLogin = new LoginRequest("other@test.com", "Password123!");
        
        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
                
        // Changed .getToken() to .token()
        otherUserToken = objectMapper.readValue(otherLoginResponse, LoginResponse.class).token();
        otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();

        // 3. Create a DRAFT Decision Board directly in DB
        Decision decision = new Decision();
        decision.setTitle("Framework Comparison");
        decision.setDescription("Select a backend Java framework");
        decision.setCreator(creatorUser);
        decision.setVisibility(DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(java.time.LocalDateTime.now());
        decision = decisionRepository.save(decision);
        decisionId = decision.getId();

        // Create option directly in DB
        DecisionOption option = new DecisionOption();
        option.setOptionName("Spring Boot");
        option.setDescription("Enterprise stack");
        option.setDecision(decision);
        decisionOptionRepository.save(option);
        optionId = option.getId();

        // 4. Create a Comparison Factor
        ComparisonFactorRequest factorRequest = new ComparisonFactorRequest("Performance", "Execution speed");
        String factorResponseJson = mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(factorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        factorId = objectMapper.readValue(factorResponseJson, ComparisonFactorResponse.class).id();
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
        // 1. Transition board to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

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
                .andExpect(jsonPath("$.decisionId").value(decisionId))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(85.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].averageScore").value(85.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].weight").value(1.0))
                .andExpect(jsonPath("$.options[0].factorBreakdown[0].weightedScore").value(85.0));

        // 5. Retrieve Ranking Summary
        mockMvc.perform(get("/decisions/{decisionId}/ranking/summary", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(decisionId))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(85.0));
    }

    @Test
    void testGetRanking_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/decisions/{decisionId}/ranking", decisionId))
                .andExpect(status().isUnauthorized());
    }
}