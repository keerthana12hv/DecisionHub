package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.ComparisonFactor;
import com.decisionhub.entity.VotingType;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.ComparisonFactorRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class ComparisonFactorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionBoardRepository decisionBoardRepository;

    @Autowired
    private ComparisonFactorRepository comparisonFactorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String otherUserToken;
    private UUID creatorId;
    private UUID otherUserId;

    private UUID decisionId;
    private UUID factorId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database tables in reverse order
        comparisonFactorRepository.deleteAll();
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
    void testAddFactor_Success() throws Exception {
        ComparisonFactorRequest request = new ComparisonFactorRequest("Cost", "License and hosting cost");

        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Cost"));

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("FACTOR_CREATED") && l.getTargetId() != null));
    }

    @Test
    void testAddFactor_DuplicateName_ReturnsBadRequest() throws Exception {
        ComparisonFactorRequest request = new ComparisonFactorRequest("Performance", "Duplicate name check");

        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddFactor_Forbidden_NonOwner() throws Exception {
        ComparisonFactorRequest request = new ComparisonFactorRequest("Scalability", "Forbidden check");

        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateFactor_Success() throws Exception {
        ComparisonFactorRequest request = new ComparisonFactorRequest("Throughput", "Updated performance description");

        mockMvc.perform(put("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Throughput"))
                .andExpect(jsonPath("$.description").value("Updated performance description"));

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("FACTOR_UPDATED")));
    }

    @Test
    void testDeleteFactor_Success() throws Exception {
        mockMvc.perform(delete("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("FACTOR_DELETED")));
    }

    @Test
    void testGetFactors_Success() throws Exception {
        mockMvc.perform(get("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Performance"));
    }

    @Test
    void testFactorModificationOnActiveBoard_ReturnsBadRequest() throws Exception {
        // Transition status to ACTIVE
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        // Try to add factor on active board
        ComparisonFactorRequest request = new ComparisonFactorRequest("Cost", "Active check");
        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
