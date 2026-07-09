package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.administration.AuditLog;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.ComparisonScoreRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DecisionOptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private DecisionOptionRepository decisionOptionRepository;

    @Autowired
    private ComparisonScoreRepository comparisonScoreRepository;

    @Autowired
    private ComparisonFactorRepository comparisonFactorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String otherUserToken;
    private Long creatorId;
    private Long otherUserId;

    private Long decisionId;
    private Long option1Id;
    private Long option2Id;

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

        // 3. Create a default Decision directly in DB
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
        option1Id = option1.getId();

        DecisionOption option2 = new DecisionOption();
        option2.setOptionName("Quarkus");
        option2.setDescription("Cloud native");
        option2.setDecision(decision);
        decisionOptionRepository.save(option2);
        option2Id = option2.getId();
    }

    @Test
    void testAddOption_Success() throws Exception {
        OptionCreateDto request = new OptionCreateDto("Micronaut", "A modern framework", Collections.emptyList());

        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Micronaut"));

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.CREATE_DECISION));
    }

    @Test
    void testAddOption_DuplicateTitle_ReturnsBadRequest() throws Exception {
        OptionCreateDto request = new OptionCreateDto("Spring Boot", "Duplicate", Collections.emptyList());

        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddOption_Forbidden_NonOwner() throws Exception {
        OptionCreateDto request = new OptionCreateDto("Micronaut", "Forbidden check", Collections.emptyList());

        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateOption_Success() throws Exception {
        OptionCreateDto request = new OptionCreateDto("Quarkus 3", "Updated native description", Collections.emptyList());

        mockMvc.perform(put("/decisions/{decisionId}/options/{optionId}", decisionId, option2Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Quarkus 3"))
                .andExpect(jsonPath("$.description").value("Updated native description"));

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.UPDATE_DECISION));
    }

    @Test
    void testDeleteOption_MinOptionsRuleViolation_ReturnsBadRequest() throws Exception {
        // Deleting when board has exactly 2 options should fail
        mockMvc.perform(delete("/decisions/{decisionId}/options/{optionId}", decisionId, option2Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteOption_Success_AfterAddingThirdOption() throws Exception {
        // 1. Add third option
        OptionCreateDto addRequest = new OptionCreateDto("Micronaut", "Third option", Collections.emptyList());
        String responseJson = mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long thirdOptionId = objectMapper.readValue(responseJson, OptionResponseDto.class).id();

        // 2. Delete one of the options
        mockMvc.perform(delete("/decisions/{decisionId}/options/{optionId}", decisionId, option2Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.DELETE_DECISION));
    }

    @Test
    void testOptionModificationOnActiveBoard_ReturnsBadRequest() throws Exception {
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Try to add option on active board
        OptionCreateDto request = new OptionCreateDto("Micronaut", "Active check", Collections.emptyList());
        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
