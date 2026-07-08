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
import com.decisionhub.entity.AuditLog;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ComparisonScoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

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
        // Clean database tables in reverse order
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionBoardRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        // 1. Register and login Creator
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

        // 2. Register and login Other User
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

        // 3. Create a default Decision Board in DRAFT state
        DecisionRequest createRequest = new DecisionRequest(
                "Framework Comparison", "Select a backend Java framework", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                new HashSet<>(Arrays.asList("java", "framework")),
                Arrays.asList(new OptionCreateDto("Spring Boot", "Enterprise stack", Collections.emptyList()), new OptionCreateDto("Quarkus", "Cloud native", Collections.emptyList())),
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

        // Retrieve generated option ID
        optionId = decisionOptionRepository.findByDecisionIdAndDeletedAtIsNull(decisionId).get(0).getId();

        // 4. Create a default Comparison Factor
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
    void testSubmitScore_BoardNotActive_ReturnsBadRequest() throws Exception {
        // Board is currently in DRAFT status, should throw BadRequestException
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 80, "Validation check");

        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSubmitScore_Success_CreateAndUpdate() throws Exception {
        // 1. Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

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
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("SCORE_CREATED")));

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
        assertTrue(updatedLogs.stream().anyMatch(l -> l.getAction().equals("SCORE_UPDATED")));
    }

    @Test
    void testSubmitScore_ValidationFails_InvalidRange() throws Exception {
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

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
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

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
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

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
        com.decisionhub.entity.ComparisonScoreId id = new com.decisionhub.entity.ComparisonScoreId(optionId, factorId, creatorId);
        assertTrue(comparisonScoreRepository.findById(id).isEmpty());

        // Assert audit log exists for SCORE_DELETED
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("SCORE_DELETED")));
    }

    @Test
    void testOptimisticLocking_ConcurrentUpdates() throws Exception {
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        // Submit score (Create)
        ComparisonScoreRequest request = new ComparisonScoreRequest(optionId, factorId, 80, "Initial");
        mockMvc.perform(post("/decisions/{decisionId}/scores", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Simulate concurrent updates by reading the score in two threads/transactions
        // Transaction 1
        org.springframework.transaction.support.TransactionTemplate txTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);

        try {
            txTemplate.execute(status -> {
                com.decisionhub.entity.ComparisonScoreId id = new com.decisionhub.entity.ComparisonScoreId(optionId, factorId, creatorId);
                com.decisionhub.entity.ComparisonScore score1 = comparisonScoreRepository.findById(id).orElseThrow();

                // Transaction 2 (simulated by PROPAGATION_REQUIRES_NEW to run in separate transaction)
                org.springframework.transaction.support.TransactionTemplate txNew = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
                txNew.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                txNew.executeWithoutResult(status2 -> {
                    com.decisionhub.entity.ComparisonScore score2 = comparisonScoreRepository.findById(id).orElseThrow();
                    score2.setScore(90);
                    comparisonScoreRepository.saveAndFlush(score2);
                });

                // Try to modify score1 (which has the old state in memory)
                score1.setScore(95);
                comparisonScoreRepository.saveAndFlush(score1);
                org.junit.jupiter.api.Assertions.fail("Expected OptimisticLockingFailureException");
                return null;
            });
        } catch (org.springframework.transaction.UnexpectedRollbackException | org.springframework.orm.ObjectOptimisticLockingFailureException | org.hibernate.StaleObjectStateException e) {
            // Succeeded! Optimistic locking worked, and the transaction failure was caught.
        }
    }
}
