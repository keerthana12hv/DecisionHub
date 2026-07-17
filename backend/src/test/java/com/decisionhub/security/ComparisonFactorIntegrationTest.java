package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.decision.ComparisonFactorRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.decision.ComparisonFactorResponse;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.entity.decision.ComparisonFactor;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.administration.AuditLog;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComparisonFactorIntegrationTest {

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

        // 1. Register and login Creator (Updated for Records)
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
                
        // Updated to use .token() record accessor
        creatorToken = objectMapper.readValue(creatorLoginResponse, LoginResponse.class).token(); 
        User creatorUser = userRepository.findByUsername("creatoruser").orElseThrow();
        creatorId = creatorUser.getId();

        // 2. Register and login Other User (Updated for Records)
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
                
        // Updated to use .token() record accessor
        otherUserToken = objectMapper.readValue(otherLoginResponse, LoginResponse.class).token();
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

        DecisionOption option2 = new DecisionOption();
        option2.setOptionName("Quarkus");
        option2.setDescription("Cloud native");
        option2.setDecision(decision);
        decisionOptionRepository.save(option2);

        // 4. Create a default Comparison Factor
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
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.CREATE_DECISION));
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
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.UPDATE_DECISION));
    }

    @Test
    void testDeleteFactor_Success() throws Exception {
        mockMvc.perform(delete("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == com.decisionhub.enums.administration.AuditActionType.DELETE_DECISION));
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
        // Transition status to ACTIVE directly in DB
        Decision d = decisionRepository.findById(decisionId).orElseThrow();
        d.setStatus(DecisionStatus.ACTIVE);
        decisionRepository.save(d);

        // Try to add factor on active board
        ComparisonFactorRequest request = new ComparisonFactorRequest("Cost", "Active check");
        mockMvc.perform(post("/decisions/{decisionId}/factors", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetFactor_Success() throws Exception {
        mockMvc.perform(get("/decisions/{decisionId}/factors/{factorId}", decisionId, factorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(factorId))
                .andExpect(jsonPath("$.name").value("Performance"))
                .andExpect(jsonPath("$.description").value("Execution speed"));
    }

    @Test
    void testGetFactor_NotFound() throws Exception {
        mockMvc.perform(get("/decisions/{decisionId}/factors/{factorId}", decisionId, 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFactor_Forbidden_NonAuthorizedView() throws Exception {
        // Create a PRIVATE decision directly in DB under creator
        Decision privateDecision = new Decision();
        privateDecision.setTitle("Private Board");
        privateDecision.setDescription("Only creator can see");
        privateDecision.setCreator(userRepository.findById(creatorId).orElseThrow());
        privateDecision.setVisibility(DecisionVisibility.PRIVATE);
        privateDecision.setStatus(DecisionStatus.DRAFT);
        privateDecision.setCreatedAt(java.time.LocalDateTime.now());
        privateDecision = decisionRepository.save(privateDecision);

        ComparisonFactor factor = new ComparisonFactor();
        factor.setName("Secret Factor");
        factor.setWeight(1);
        factor.setDecision(privateDecision);
        comparisonFactorRepository.save(factor);

        // Fetching factors of private decision with other user's token should be forbidden (403)
        mockMvc.perform(get("/decisions/{decisionId}/factors/{factorId}", privateDecision.getId(), factor.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }
}