package com.decisionhub.controller;

import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.OptionResponseDto;
import com.decisionhub.service.DecisionOptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.decisionhub.config.JwtService;
import com.decisionhub.service.impl.authentication.CustomUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@WebMvcTest(
    controllers = DecisionOptionController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class DecisionOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DecisionOptionService decisionOptionService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void createOption_withValidPayload_returnsCreated() throws Exception {
        Long decisionId = 1L;
        Long optionId = 2L;
        OptionCreateDto request = new OptionCreateDto("Quarkus", "Kubernetes native Java", Collections.emptyList());
        OptionResponseDto response = new OptionResponseDto(optionId, "Quarkus", "Kubernetes native Java", Collections.emptyList());

        when(decisionOptionService.createOption(eq(decisionId), any(OptionCreateDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(optionId))
                .andExpect(jsonPath("$.title").value("Quarkus"));
    }

    @Test
    void createOption_withBlankTitle_returnsBadRequest() throws Exception {
        Long decisionId = 1L;
        OptionCreateDto request = new OptionCreateDto("", "Description", Collections.emptyList());

        mockMvc.perform(post("/decisions/{decisionId}/options", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOption_withValidPayload_returnsOk() throws Exception {
        Long decisionId = 1L;
        Long optionId = 2L;
        OptionCreateDto request = new OptionCreateDto("Updated title", "Updated desc", Collections.emptyList());
        OptionResponseDto response = new OptionResponseDto(optionId, "Updated title", "Updated desc", Collections.emptyList());

        when(decisionOptionService.updateOption(eq(decisionId), eq(optionId), any(OptionCreateDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/decisions/{decisionId}/options/{optionId}", decisionId, optionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }

    @Test
    void deleteOption_returnsNoContent() throws Exception {
        Long decisionId = 1L;
        Long optionId = 2L;
        
        doNothing().when(decisionOptionService).deleteOption(eq(decisionId), eq(optionId), any(), any());

        mockMvc.perform(delete("/decisions/{decisionId}/options/{optionId}", decisionId, optionId))
                .andExpect(status().isNoContent());
    }
}
