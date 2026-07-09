package com.decisionhub.controller;

import com.decisionhub.dto.OptionRankingDto;
import com.decisionhub.dto.OptionSummaryRankingDto;
import com.decisionhub.dto.RankingResponse;
import com.decisionhub.dto.RankingSummaryResponse;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.service.RankingService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = RankingController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void getRanking_Success() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();

        OptionRankingDto optionDto = new OptionRankingDto(optionId, "Option Title", 1, 95.0, Collections.emptyList(), false);
        RankingResponse response = new RankingResponse(decisionId, "Decision Title", DecisionStatus.ACTIVE, Instant.now(), Collections.singletonList(optionDto));

        when(rankingService.getRanking(decisionId)).thenReturn(response);

        mockMvc.perform(get("/decisions/{decisionId}/ranking", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(decisionId.toString()))
                .andExpect(jsonPath("$.options[0].optionTitle").value("Option Title"))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(95.0));
    }

    @Test
    void getRankingSummary_Success() throws Exception {
        UUID decisionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();

        OptionSummaryRankingDto summaryDto = new OptionSummaryRankingDto(optionId, "Option Title", 1, 95.0);
        RankingSummaryResponse response = new RankingSummaryResponse(decisionId, "Decision Title", DecisionStatus.ACTIVE, Instant.now(), Collections.singletonList(summaryDto));

        when(rankingService.getRankingSummary(decisionId)).thenReturn(response);

        mockMvc.perform(get("/decisions/{decisionId}/ranking/summary", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decisionId").value(decisionId.toString()))
                .andExpect(jsonPath("$.options[0].optionTitle").value("Option Title"))
                .andExpect(jsonPath("$.options[0].rank").value(1))
                .andExpect(jsonPath("$.options[0].score").value(95.0));
    }
}
