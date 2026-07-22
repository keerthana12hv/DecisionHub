package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.entity.administration.AuditLog;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.ComparisonScoreId;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.enums.decision.VotingType;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.AuditLogRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.voting.PollRepository;

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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private PollRepository pollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;

    private Long creatorId;

    private Long decisionId;
    private Long optionId;
    private Long factorId;

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Clean database tables in dependency-safe order.
         * Poll must be removed before Decision because Poll
         * references Decision through decision_id.
         */
        comparisonScoreRepository.deleteAll();
        pollRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // -------------------------------------------------
        // 1. Register and login Creator
        // -------------------------------------------------

        RegisterRequest creatorReg =
                new RegisterRequest(
                        "creatoruser",
                        "creator@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                creatorReg
                                        )
                                )
                )
                .andExpect(status().isOk());

        LoginRequest creatorLogin =
                new LoginRequest(
                        "creator@test.com",
                        "Password123!"
                );

        String creatorLoginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        creatorLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        creatorToken =
                objectMapper.readValue(
                        creatorLoginResponse,
                        LoginResponse.class
                ).token();

        User creatorUser =
                userRepository
                        .findByUsername("creatoruser")
                        .orElseThrow();

        creatorId = creatorUser.getId();

        // -------------------------------------------------
        // 2. Register and login Other User
        // -------------------------------------------------

        RegisterRequest otherReg =
                new RegisterRequest(
                        "otheruser",
                        "other@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                otherReg
                                        )
                                )
                )
                .andExpect(status().isOk());

        LoginRequest otherLogin =
                new LoginRequest(
                        "other@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                otherLogin
                                        )
                                )
                )
                .andExpect(status().isOk());

        // -------------------------------------------------
        // 3. Create DRAFT RATING_BASED Decision
        // -------------------------------------------------

        Decision decision = new Decision();

        decision.setTitle("Framework Comparison");
        decision.setDescription(
                "Select a backend Java framework"
        );
        decision.setCreator(creatorUser);
        decision.setVisibility(
                DecisionVisibility.PUBLIC
        );
        decision.setStatus(
                DecisionStatus.DRAFT
        );
        decision.setVotingType(
                VotingType.RATING_BASED
        );
        decision.setVotingEndTime(
                LocalDateTime.now().plusHours(2)
        );
        decision.setCreatedAt(
                LocalDateTime.now()
        );

        decision =
                decisionRepository.save(decision);

        decisionId = decision.getId();

        // -------------------------------------------------
        // 4. Create Options
        // -------------------------------------------------

        DecisionOption option1 =
                new DecisionOption();

        option1.setOptionName("Spring Boot");
        option1.setDescription(
                "Enterprise stack"
        );
        option1.setDecision(decision);

        option1 =
                decisionOptionRepository.save(option1);

        optionId = option1.getId();

        DecisionOption option2 =
                new DecisionOption();

        option2.setOptionName("Quarkus");
        option2.setDescription(
                "Cloud native"
        );
        option2.setDecision(decision);

        decisionOptionRepository.save(option2);

        // -------------------------------------------------
        // 5. Create Comparison Factor
        // -------------------------------------------------

        ComparisonFactorRequest factorRequest =
                new ComparisonFactorRequest(
                        "Performance",
                        "Execution speed"
                );

        String factorResponseJson =
                mockMvc.perform(
                                post(
                                        "/api/decisions/{decisionId}/factors",
                                        decisionId
                                )
                                        .header(
                                                HttpHeaders.AUTHORIZATION,
                                                "Bearer " + creatorToken
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        factorRequest
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        factorId =
                objectMapper.readValue(
                        factorResponseJson,
                        ComparisonFactorResponse.class
                ).id();
    }

    /**
     * Activates the Decision and creates the Poll fixture required
     * by rating-based participation.
     *
     * These integration tests modify the Decision directly through
     * the repository, so DecisionPublishedEvent is not triggered.
     */
    private void activateDecisionAndCreatePoll() {

        Decision decision =
                decisionRepository
                        .findById(decisionId)
                        .orElseThrow();

        decision.setStatus(
                DecisionStatus.ACTIVE
        );

        decisionRepository.save(decision);

        Poll poll = new Poll();

        poll.setDecision(decision);
        poll.setStatus(PollStatus.OPEN);
        poll.setEndTime(
                LocalDateTime.now().plusHours(1)
        );
        poll.setCreatedAt(
                LocalDateTime.now()
        );
        poll.setUpdatedAt(
                LocalDateTime.now()
        );

        pollRepository.save(poll);
    }

    @Test
    void testSubmitScore_BoardNotActive_ReturnsBadRequest()
            throws Exception {

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        80,
                        "Validation check"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSubmitScore_Success_CreateAndUpdate()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        85,
                        "Excellent startup"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.score")
                                .value(85)
                )
                .andExpect(
                        jsonPath("$.remarks")
                                .value("Excellent startup")
                );

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertTrue(
                auditLogs.stream()
                        .anyMatch(
                                l -> l.getAction()
                                        == com.decisionhub.enums.administration.AuditActionType.CREATE_DECISION
                        )
        );

        ComparisonScoreRequest updateRequest =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        95,
                        "Updated higher score"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.score")
                                .value(95)
                )
                .andExpect(
                        jsonPath("$.remarks")
                                .value("Updated higher score")
                );

        List<AuditLog> updatedLogs =
                auditLogRepository.findAll();

        assertTrue(
                updatedLogs.stream()
                        .anyMatch(
                                l -> l.getAction()
                                        == com.decisionhub.enums.administration.AuditActionType.UPDATE_DECISION
                        )
        );
    }

    @Test
    void testSubmitScore_ValidationFails_InvalidRange()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        120,
                        "Out of bounds"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetScores_Success()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        85,
                        "Great"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].score")
                                .value(85)
                );
    }

    @Test
    void testDeleteScore_Success()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        85,
                        "Great"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete(
                                "/api/decisions/{decisionId}/scores/{optionId}/{factorId}",
                                decisionId,
                                optionId,
                                factorId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(status().isNoContent());

        ComparisonScoreId id =
                new ComparisonScoreId(
                        optionId,
                        factorId,
                        creatorId
                );

        assertTrue(
                comparisonScoreRepository
                        .findById(id)
                        .isEmpty()
        );

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertTrue(
                auditLogs.stream()
                        .anyMatch(
                                l -> l.getAction()
                                        == com.decisionhub.enums.administration.AuditActionType.DELETE_DECISION
                        )
        );
    }

    @Test
    void testOptimisticLocking_ConcurrentUpdates()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        80,
                        "Initial"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());

        org.springframework.transaction.support.TransactionTemplate txTemplate =
                new org.springframework.transaction.support.TransactionTemplate(
                        transactionManager
                );

        try {

            txTemplate.execute(status -> {

                ComparisonScoreId id =
                        new ComparisonScoreId(
                                optionId,
                                factorId,
                                creatorId
                        );

                ComparisonScore score1 =
                        comparisonScoreRepository
                                .findById(id)
                                .orElseThrow();

                org.springframework.transaction.support.TransactionTemplate txNew =
                        new org.springframework.transaction.support.TransactionTemplate(
                                transactionManager
                        );

                txNew.setPropagationBehavior(
                        org.springframework.transaction.TransactionDefinition
                                .PROPAGATION_REQUIRES_NEW
                );

                txNew.executeWithoutResult(status2 -> {

                    ComparisonScore score2 =
                            comparisonScoreRepository
                                    .findById(id)
                                    .orElseThrow();

                    score2.setScore(90);

                    comparisonScoreRepository
                            .saveAndFlush(score2);
                });

                score1.setScore(95);

                comparisonScoreRepository
                        .saveAndFlush(score1);

                org.junit.jupiter.api.Assertions.fail(
                        "Expected OptimisticLockingFailureException"
                );

                return null;
            });

        } catch (
                org.springframework.transaction.UnexpectedRollbackException
                | org.springframework.orm.ObjectOptimisticLockingFailureException
                | org.hibernate.StaleObjectStateException e
        ) {
            // Optimistic locking worked as expected.
        }
    }

    @Test
    void testGetScore_Success()
            throws Exception {

        activateDecisionAndCreatePoll();

        ComparisonScoreRequest request =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        85,
                        "Great"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/scores/{optionId}/{factorId}",
                                decisionId,
                                optionId,
                                factorId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.score")
                                .value(85)
                )
                .andExpect(
                        jsonPath("$.remarks")
                                .value("Great")
                );
    }

    @Test
    void testGetScore_NotFound()
            throws Exception {

        /*
         * Read operations don't require an OPEN Poll, but the
         * Decision must be ACTIVE according to the score service.
         */
        Decision decision =
                decisionRepository
                        .findById(decisionId)
                        .orElseThrow();

        decision.setStatus(
                DecisionStatus.ACTIVE
        );

        decisionRepository.save(decision);

        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/scores/{optionId}/{factorId}",
                                decisionId,
                                optionId,
                                999L
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(status().isNotFound());
    }
}