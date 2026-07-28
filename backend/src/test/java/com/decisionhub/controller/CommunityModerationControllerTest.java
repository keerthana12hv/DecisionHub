package com.decisionhub.controller;

import com.decisionhub.config.JwtService;
import com.decisionhub.dto.response.decision.DecisionResponse;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import com.decisionhub.service.interfaces.community.CommunityModerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = CommunityModerationController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class CommunityModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommunityModerationService communityModerationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private DecisionResponse response;

    @BeforeEach
    void setUp() {
        response = new DecisionResponse(
                1L, "Decision Title", "Description", null, null, "Community Name",
                DecisionStatus.DRAFT, null, Collections.emptyList(), Collections.emptyList(),
                LocalDateTime.now(), true, true
        );
    }

    @Test
    void pinDecision_Success() throws Exception {
        when(communityModerationService.pinDecision(eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/moderation/decisions/1/pin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void unpinDecision_Success() throws Exception {
        DecisionResponse unpinnedResponse = new DecisionResponse(
                1L, "Decision Title", "Description", null, null, "Community Name",
                DecisionStatus.DRAFT, null, Collections.emptyList(), Collections.emptyList(),
                LocalDateTime.now(), false, true
        );
        when(communityModerationService.unpinDecision(eq(1L))).thenReturn(unpinnedResponse);

        mockMvc.perform(put("/api/moderation/decisions/1/unpin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void lockDiscussion_Success() throws Exception {
        when(communityModerationService.lockDiscussion(eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/moderation/decisions/1/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    void unlockDiscussion_Success() throws Exception {
        DecisionResponse unlockedResponse = new DecisionResponse(
                1L, "Decision Title", "Description", null, null, "Community Name",
                DecisionStatus.DRAFT, null, Collections.emptyList(), Collections.emptyList(),
                LocalDateTime.now(), true, false
        );
        when(communityModerationService.unlockDiscussion(eq(1L))).thenReturn(unlockedResponse);

        mockMvc.perform(put("/api/moderation/decisions/1/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.locked").value(false));
    }
}
