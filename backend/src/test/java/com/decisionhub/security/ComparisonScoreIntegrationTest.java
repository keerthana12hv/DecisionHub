package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.ComparisonFactorResponse;
import com.decisionhub.dto.ComparisonScoreRequest;
import com.decisionhub.dto.ComparisonScoreResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.administration.AuditLog;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.ComparisonScoreRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.AuditLogRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComparisonScoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

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
        // Clean database tables in reverse order
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register and login Creator
        RegisterRequest creatorReg = new RegisterRequest();
        creatorReg.setUsername("creatoruser");
        creatorReg.setEmail("creator@test.com");
        creatorReg.setPassword("Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorReg)))
                .andExpect(status().isOk());

        LoginRequest creatorLogin = new LoginRequest();
        creatorLogin.setEmail("creator@test.com");
        creatorLogin.setPassword("Password123!");
        String creatorLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        creatorToken = objectMapper.readValue(creatorLoginResponse, LoginResponse.class).getToken();
        User creatorUser = userRepository.findByUsername("creatoruser").orElseThrow();
        creatorId = creatorUser.getId();

        // 2. Register and login Other User
        RegisterRequest otherReg = new RegisterRequest();
        otherReg.setUsername("otheruser");
        otherReg.setEmail("other@test.com");
        otherReg.setPassword("Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherReg)))
                .andExpect(status().isOk());

        LoginRequest otherLogin = new LoginRequest();
        otherLogin.setEmail("other@test.com");
        otherLogin.setPassword("Password123!");
        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        otherUserToken = objectMapper.readValue(otherLoginResponse, LoginResponse.class).getToken();
        otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();

        // 3. Create a default Decision Board in DRAFT state directly in DB
        Decision decision = new Decision();
        decision.setTitle("Framework Comparison");
        decision.setDescription("Select a backend Java framework");
        decision.setCreator(creatorUser);
        decision.setVisibility(DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(java.time.LocalDateTime.now());
        decision = decisionRepository.save(decision);
        decisionId = decision.getId();

        // Create Options directly in DB
        DecisionOption option1 = new DecisionOption();
        option1.setOptionName("Spring Boot");
        option1.setDescription("Enterprise stack");
        option1.setDecision(decision);
        decisionOptionRepository.save(option1);
        optionId = option1.getId();

        DecisionOption option2 = new DecisionOption();
        option2.setOptionName("Quarkus");
        option2.setDescription("Cloud native");
        option2.setDecision(decision);
        decisionOptionRepository.save(option2);

        // 4. Create a default Comparison Factor via REST API
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
    void testSubmitScore_BoardNotActive_ReturnsBadRequest() throws Exception {
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 80, "Validation check");

        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSubmitScore_Success_CreateAndUpdate() throws Exception {
        // 1. Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // 2. Submit score (Create)
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 85, "Excellent startup");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.remarks").value("Excellent startup"));

        // Verify Audit Log for creation
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.CREATE_DECISION));

        // 3. Submit score again (Update via POST/upsert)
        ComparisonScoreRequest updateRequest = new ComparisonScoreRequest(optionId, factorId, 95, "Updated higher score");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(95))
                .andExpect(jsonPath("$.remarks").value("Updated higher score"));

        // Verify Audit Log for update
        List<AuditLog> updatedLogs = auditLogRepository.findAll();
        assertTrue(updatedLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.UPDATE_DECISION));
    }

    @Test
    void testSubmitScore_ValidationFails_InvalidRange() throws Exception {
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Score exceeds 100
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 120, "Out of bounds");

        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetScores_Success() throws Exception {
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Submit score
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 85, "Great");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Fetch scores
        mockMvc.perform(get("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(85));
    }

    @Test
    void testDeleteScore_Success() throws Exception {
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Submit score
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 85, "Great");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Delete score
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/decisions/{decisionId}/scores/{optionId}/{factorId}", decisionId, optionId, factorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Assert deleted from repo
        ComparisonScoreId id = new ComparisonScoreId(optionId, factorId, creatorId);
        assertTrue(comparisonScoreRepository.findById(id).isEmpty());

        // Assert audit log exists for SCORE_DELETED
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.DELETE_DECISION));
    }

    @Test
    void testOptimisticLocking_ConcurrentUpdates() throws Exception {
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Submit score (Create)
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 80, "Initial");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Simulate concurrent updates by reading the score in two threads/transactions
        org.springframework.transaction.support.TransactionTemplate txTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);

        try {
            txTemplate.execute(status -> {
                ComparisonScoreId id = new ComparisonScoreId(optionId, factorId, creatorId);
                ComparisonScore score1 = comparisonScoreRepository.findById(id).orElseThrow();

                // Transaction 2
                org.springframework.transaction.support.TransactionTemplate txNew = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
                txNew.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txNew.executeWithoutResult(status2 -> {
                    ComparisonScore score2 = comparisonScoreRepository.findById(id).orElseThrow();
                    score2.setScore(90);
                    comparisonScoreRepository.saveAndFlush(score2);
                });

                // Try to modify score1
                score1.setScore(95);
                comparisonScoreRepository.saveAndFlush(score1);
                org.junit.jupiter.api.Assertions.fail("Expected OptimisticLockingFailureException");
                return null;
            });
        } catch (org.springframework.transaction.UnexpectedRollbackException | org.springframework.orm.ObjectOptimisticLockingFailureException | org.hibernate.StaleObjectStateException e) {
            // Succeeded! Optimistic locking worked
        }
    }
}
