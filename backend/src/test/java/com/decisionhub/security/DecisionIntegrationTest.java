package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.decision.DecisionRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.entity.administration.AuditLog;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.entity.community.Category;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.community.CommunityMember;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.administration.AuditActionType;
import com.decisionhub.enums.community.CommunityMemberRole;
import com.decisionhub.enums.community.CommunityVisibility;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CategoryRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.AuditLogRepository;
import com.decisionhub.repository.decision.ComparisonFactorRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DecisionIntegrationTest {

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
    private CommunityMemberRepository communityMemberRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String otherUserToken;
    private Long creatorId;
    private Long otherUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database tables in reverse order of dependencies
        comparisonScoreRepository.deleteAll();
        comparisonFactorRepository.deleteAll();
        decisionOptionRepository.deleteAll();
        decisionRepository.deleteAll();
        communityMemberRepository.deleteAll();
        communityRepository.deleteAll();
        categoryRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register and login Creator
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
        creatorToken = objectMapper.readValue(creatorLoginResponse, LoginResponse.class).token();
        User creatorUser = userRepository.findByUsername("creatoruser").orElseThrow();
        creatorId = creatorUser.getId();

        // 2. Register and login Other User
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
        otherUserToken = objectMapper.readValue(otherLoginResponse, LoginResponse.class).token();
        otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();
    }

    @Test
    void testCreateDecision_Success() throws Exception {
        DecisionRequest request = new DecisionRequest(
            "Evaluation of Tech Stacks",
            "This decision compares backend technologies.",
            null,
            null,
            true,
            null,
            null,
            LocalDateTime.now().plusDays(5),
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        String responseJson = mockMvc.perform(post("/api/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Evaluation of Tech Stacks"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        DecisionResponse response = objectMapper.readValue(responseJson, DecisionResponse.class);
        assertNotNull(response.id());

        // Verify DB record
        assertTrue(decisionRepository.findById(response.id()).isPresent());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == AuditActionType.CREATE_DECISION));
    }

    @Test
    void testCreateDecision_CommunityDecision_Success() throws Exception {
        // Create Category
        Category category = new Category();
        category.setName("General");
        category.setSlug("general");
        category.setIsActive(true);
        category = categoryRepository.save(category);

        // Create Community
        User owner = userRepository.findById(creatorId).orElseThrow();
        Community community = new Community();
        community.setName("Java Devs");
        community.setSlug("java-devs");
        community.setDescription("Java developer community");
        community.setCategory(category);
        community.setOwner(owner);
        community.setVisibility(CommunityVisibility.PUBLIC);
        community = communityRepository.save(community);

        // Join Community
        CommunityMember member = new CommunityMember();
        member.setCommunity(community);
        member.setUser(owner);
        member.setRole(CommunityMemberRole.MEMBER);
        member.setStatus(MembershipStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        communityMemberRepository.save(member);

        DecisionRequest request = new DecisionRequest(
            "Which framework to use?",
            "Comparing Spring Boot, Quarkus, Micronaut",
            null,
            community.getId(),
            false,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        mockMvc.perform(post("/api/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.communityName").value("Java Devs"));
    }

    @Test
    void testUpdateDecision_Success() throws Exception {
        // Create decision directly in DB
        User owner = userRepository.findById(creatorId).orElseThrow();
        Decision decision = new Decision();
        decision.setTitle("Initial Title");
        decision.setDescription("Initial Description");
        decision.setCreator(owner);
        decision.setVisibility(com.decisionhub.enums.decision.DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);

        DecisionRequest updateRequest = new DecisionRequest(
            "Updated Title",
            "Updated Description",
            null,
            null,
            true,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        mockMvc.perform(put("/api/decisions/{id}", decision.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Verify DB update
        Decision updated = decisionRepository.findById(decision.getId()).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == AuditActionType.UPDATE_DECISION));
    }

    @Test
    void testUpdateDecision_Forbidden_NonOwner() throws Exception {
        User owner = userRepository.findById(creatorId).orElseThrow();
        Decision decision = new Decision();
        decision.setTitle("Initial Title");
        decision.setCreator(owner);
        decision.setVisibility(com.decisionhub.enums.decision.DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);

        DecisionRequest updateRequest = new DecisionRequest(
            "Malicious Title Update",
            null,
            null,
            null,
            true,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );

        mockMvc.perform(put("/api/decisions/{id}", decision.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteDecision_Success() throws Exception {
        User owner = userRepository.findById(creatorId).orElseThrow();
        Decision decision = new Decision();
        decision.setTitle("To Be Deleted");
        decision.setCreator(owner);
        decision.setVisibility(com.decisionhub.enums.decision.DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);

        mockMvc.perform(delete("/api/decisions/{id}", decision.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Verify DB record is gone
        assertTrue(decisionRepository.findById(decision.getId()).isEmpty());

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction() == AuditActionType.DELETE_DECISION));
    }

    @Test
    void testDeleteDecision_Forbidden_NonOwner() throws Exception {
        User owner = userRepository.findById(creatorId).orElseThrow();
        Decision decision = new Decision();
        decision.setTitle("Owner's Decision");
        decision.setCreator(owner);
        decision.setVisibility(com.decisionhub.enums.decision.DecisionVisibility.PUBLIC);
        decision.setStatus(DecisionStatus.DRAFT);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);

        mockMvc.perform(delete("/api/decisions/{id}", decision.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());

        // Verify DB record is still present
        assertTrue(decisionRepository.findById(decision.getId()).isPresent());
    }
}
