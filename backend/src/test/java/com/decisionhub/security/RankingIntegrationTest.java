package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.request.decision.ComparisonScoreRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.entity.authentication.User;
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
    private PollRepository pollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String otherUserToken;

    private Long decisionId;
    private Long optionId;
    private Long factorId;

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Clean database tables in dependency-safe order.
         */
        comparisonScoreRepository.deleteAll();
        pollRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // -------------------------------------------------
        // 1. Register and Login Creator
        // -------------------------------------------------

        RegisterRequest creatorReg =
                new RegisterRequest(
                        "creatoruser",
                        "creator@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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

        // -------------------------------------------------
        // 2. Register and Login Other User
        // -------------------------------------------------

        RegisterRequest otherReg =
                new RegisterRequest(
                        "otheruser",
                        "other@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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

        String otherLoginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        otherLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        otherUserToken =
                objectMapper.readValue(
                        otherLoginResponse,
                        LoginResponse.class
                ).token();

        // -------------------------------------------------
        // 3. Create DRAFT RATING_BASED Decision
        // -------------------------------------------------

        Decision decision = new Decision();

        decision.setTitle(
                "Framework Comparison"
        );

        decision.setDescription(
                "Select a backend Java framework"
        );

        decision.setCreator(
                creatorUser
        );

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

        decisionId =
                decision.getId();

        // -------------------------------------------------
        // 4. Create Option
        // -------------------------------------------------

        DecisionOption option =
                new DecisionOption();

        option.setOptionName(
                "Spring Boot"
        );

        option.setDescription(
                "Enterprise stack"
        );

        option.setDecision(
                decision
        );

        option =
                decisionOptionRepository.save(option);

        optionId =
                option.getId();

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
     * Activates the Decision and creates the Poll required for
     * rating-based score participation.
     *
     * The test changes status directly in the repository,
     * therefore the normal DecisionPublishedEvent does not run.
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

        poll.setDecision(
                decision
        );

        poll.setStatus(
                PollStatus.OPEN
        );

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
    void testGetRanking_BoardNotActive_ReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/ranking",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void testGetRanking_Success()
            throws Exception {

        // Rating participation requires an ACTIVE Decision
        // with an associated OPEN Poll.
        activateDecisionAndCreatePoll();

        // Submit score as Creator.
        ComparisonScoreRequest requestCreator =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        90,
                        "Excellent performance"
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
                                                requestCreator
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        // Submit score as Other User.
        ComparisonScoreRequest requestOther =
                new ComparisonScoreRequest(
                        optionId,
                        factorId,
                        80,
                        "Good performance"
                );

        mockMvc.perform(
                        post(
                                "/api/decisions/{decisionId}/scores",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + otherUserToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestOther
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        // Average = (90 + 80) / 2 = 85.0
        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/ranking",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + creatorToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.decisionId")
                                .value(decisionId)
                )
                .andExpect(
                        jsonPath("$.options[0].rank")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.options[0].score")
                                .value(85.0)
                )
                .andExpect(
                        jsonPath(
                                "$.options[0].factorBreakdown[0].averageScore"
                        ).value(85.0)
                )
                .andExpect(
                        jsonPath(
                                "$.options[0].factorBreakdown[0].weight"
                        ).value(1.0)
                )
                .andExpect(
                        jsonPath(
                                "$.options[0].factorBreakdown[0].weightedScore"
                        ).value(85.0)
                );

        // Ranking summary.
        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/ranking/summary",
                                decisionId
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + otherUserToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.decisionId")
                                .value(decisionId)
                )
                .andExpect(
                        jsonPath("$.options[0].rank")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.options[0].score")
                                .value(85.0)
                );
    }

    @Test
    void testGetRanking_Unauthenticated_ReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/decisions/{decisionId}/ranking",
                                decisionId
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}