package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.decision.AuditLogRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.voting.PollRepository;
import com.decisionhub.repository.voting.VoteRepository;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private ComparisonScoreRepository comparisonScoreRepository;

    @Autowired
    private ComparisonFactorRepository comparisonFactorRepository;

    @Autowired
    private DecisionOptionRepository decisionOptionRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {

        // Clean voting data first because Vote -> Poll -> Decision
        voteRepository.deleteAll();
        pollRepository.deleteAll();

        // Clean Decision child entities
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();

        // Clean Decisions after all dependent entities
        decisionRepository.deleteAll();

        // Clean remaining data
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCorsPreflight_Success() throws Exception {

        mockMvc.perform(
                        options("/api/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5173"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "content-type"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5173"
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                                "GET,POST,PUT,PATCH,DELETE,OPTIONS"
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                                "true"
                        )
                );

        mockMvc.perform(
                        options("/api/auth/login")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5174"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "POST"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "content-type"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5174"
                        )
                );
    }

    @Test
    void testCorsActualRequest_Success() throws Exception {

        RegisterRequest registerReq =
                new RegisterRequest(
                        "corsuser",
                        "cors@test.com",
                        "Password123!"
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5173"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                registerReq
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5173"
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                                "true"
                        )
                );
    }
}