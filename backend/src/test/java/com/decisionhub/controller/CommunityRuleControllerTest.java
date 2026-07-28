package com.decisionhub.controller;

import com.decisionhub.config.JwtService;
import com.decisionhub.dto.request.community.CommunityRuleRequest;
import com.decisionhub.dto.response.community.CommunityRuleResponse;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import com.decisionhub.service.interfaces.community.CommunityRuleService;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = CommunityRuleController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class CommunityRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityRuleService communityRuleService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private CommunityRuleRequest request;
    private CommunityRuleResponse response;

    @BeforeEach
    void setUp() {
        request = new CommunityRuleRequest("Be respectful", "Always treat others with respect.");
        response = new CommunityRuleResponse(1L, 10L, "Be respectful", "Always treat others with respect.", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createRule_Success() throws Exception {
        when(communityRuleService.createRule(eq(10L), any(CommunityRuleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/moderation/communities/10/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Be respectful"));
    }

    @Test
    void updateRule_Success() throws Exception {
        when(communityRuleService.updateRule(eq(1L), any(CommunityRuleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/moderation/rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Be respectful"));
    }

    @Test
    void deleteRule_Success() throws Exception {
        doNothing().when(communityRuleService).deleteRule(eq(1L));

        mockMvc.perform(delete("/api/moderation/rules/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRulesByCommunity_Success() throws Exception {
        when(communityRuleService.getRulesByCommunity(eq(10L))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/communities/10/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Be respectful"));
    }
}
