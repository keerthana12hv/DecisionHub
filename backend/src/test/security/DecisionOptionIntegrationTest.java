package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.User;
import com.decisionhub.entity.VotingType;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class DecisionOptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionBoardRepository decisionBoardRepository;

    @Autowired
    private DecisionOptionRepository decisionOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String creatorToken;
    private String otherUserToken;
    private UUID creatorId;
    private UUID otherUserId;

    private UUID decisionId;
    private UUID option1Id;
    private UUID option2Id;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database tables in reverse order
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
        option1Id = createdDecision.options().get(0).id();
        option2Id = createdDecision.options().get(1).id();
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
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("OPTION_CREATED") && l.getTargetId() != null));
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
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("OPTION_UPDATED")));
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
        UUID thirdOptionId = UUID.fromString(objectMapper.readTree(responseJson).get("id").asText());

        // 2. Delete one of the options
        mockMvc.perform(delete("/decisions/{decisionId}/options/{optionId}", decisionId, option2Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("OPTION_DELETED")));
    }

    @Test
    void testOptionModificationOnActiveBoard_ReturnsBadRequest() throws Exception {
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        // Try to add option on active board
        OptionCreateDto request = new OptionCreateDto("Micronaut", "Active check", Collections.emptyList());
        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
