package com.decisionhub.controller.analytics;

import com.decisionhub.dto.response.analytics.DecisionOverviewResponse;
import com.decisionhub.dto.response.analytics.DiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.ParticipationResponse;
import com.decisionhub.dto.response.analytics.PlatformDiscussionAnalyticsResponse;
import com.decisionhub.dto.response.analytics.PlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.VoteDistributionResponse;
import com.decisionhub.dto.response.analytics.VoteStatisticsResponse;
import com.decisionhub.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.decisionhub.dto.response.analytics.RankingResponse;
import com.decisionhub.dto.response.analytics.UserAnalyticsResponse;
import com.decisionhub.dto.response.analytics.CommunityOverviewResponse;
import com.decisionhub.dto.response.analytics.CommunityVotingStatisticsResponse;
import com.decisionhub.dto.response.analytics.DecisionFeedbackAnalyticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityModerationAnalyticsResponse;
import com.decisionhub.dto.response.analytics.AdminDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.AdminFeedbackAnalyticsResponse;
import com.decisionhub.dto.response.analytics.CommunityActivityResponse;
import com.decisionhub.dto.response.analytics.CommunityAnalyticsResponse;
import com.decisionhub.dto.response.analytics.UserPlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.UserDecisionStatisticsResponse;
import com.decisionhub.dto.response.authentication.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<UserPlatformOverviewResponse> getUserOverview() {
        return ResponseEntity.ok(analyticsService.getUserPlatformOverview());
    }

    @GetMapping("/decisions/statistics")
    public ResponseEntity<UserDecisionStatisticsResponse> getUserDecisionStatistics() {
        return ResponseEntity.ok(analyticsService.getUserDecisionStatistics());
    }

    @GetMapping("/admin/users-list")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(analyticsService.getAllUsers(pageable));
    }

 @GetMapping("/decisions/{decisionId}/overview")
public ResponseEntity<DecisionOverviewResponse> getDecisionOverview(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getDecisionOverview(decisionId)
    );
}
@GetMapping("/decisions/{decisionId}/votes")
public ResponseEntity<VoteStatisticsResponse> getVoteStatistics(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getVoteStatistics(decisionId)
    );
}
    @GetMapping("/decisions/{decisionId}/distribution")
public ResponseEntity<List<VoteDistributionResponse>> getDistribution(
        @PathVariable Long decisionId){

    return ResponseEntity.ok(
            analyticsService.getVoteDistribution(decisionId)
    );

}
@GetMapping("/decisions/{decisionId}/participation")
public ResponseEntity<ParticipationResponse> getParticipation(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getParticipation(decisionId)
    );
}

@GetMapping("/decisions/{decisionId}/discussion")
public ResponseEntity<DiscussionStatisticsResponse> getDiscussionStatistics(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getDiscussionStatistics(decisionId)
    );
}

@GetMapping("/decisions/{decisionId}/ranking")
public ResponseEntity<List<RankingResponse>> getFinalRanking(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getFinalRanking(decisionId)
    );
}

@GetMapping("/communities/{communityId}/overview")
public ResponseEntity<CommunityOverviewResponse> getCommunityOverview(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityOverview(communityId)
    );
}
@GetMapping("/communities/{communityId}/decision-statistics")
public ResponseEntity<CommunityDecisionStatisticsResponse> getCommunityDecisionStatistics(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityDecisionStatistics(communityId)
    );
}

@GetMapping("/communities/{communityId}/voting-statistics")
public ResponseEntity<CommunityVotingStatisticsResponse>
getCommunityVotingStatistics(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityVotingStatistics(
                    communityId
            )
    );
}

@GetMapping("/communities/{communityId}/discussion-statistics")
public ResponseEntity<CommunityDiscussionStatisticsResponse>
getCommunityDiscussionStatistics(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityDiscussionStatistics(
                    communityId
            )
    );
}


@GetMapping("/decisions/{decisionId}/feedback")
public ResponseEntity<DecisionFeedbackAnalyticsResponse> getDecisionFeedback(
        @PathVariable Long decisionId) {

    return ResponseEntity.ok(
            analyticsService.getDecisionFeedback(decisionId)
    );
}

@GetMapping("/communities/{communityId}/activity")
public ResponseEntity<CommunityActivityResponse> getCommunityActivity(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityActivity(communityId)
    );
}

@GetMapping("/communities/{communityId}/moderation")
public ResponseEntity<CommunityModerationAnalyticsResponse>
getCommunityModerationAnalytics(
        @PathVariable Long communityId) {

    return ResponseEntity.ok(
            analyticsService.getCommunityModerationAnalytics(
                    communityId
            )
    );
        }

        @GetMapping("/admin/dashboard")
public ResponseEntity<PlatformOverviewResponse> getPlatformOverview() {

    return ResponseEntity.ok(
            analyticsService.getPlatformOverview()
    );
}

@GetMapping("/admin/users")
public ResponseEntity<UserAnalyticsResponse> getUserAnalytics() {

    return ResponseEntity.ok(
            analyticsService.getUserAnalytics()
    );
}

@GetMapping("/admin/communities")
public ResponseEntity<CommunityAnalyticsResponse> getCommunityAnalytics() {

    return ResponseEntity.ok(
            analyticsService.getCommunityAnalytics()
    );
}

@GetMapping("/admin/decisions")
public ResponseEntity<AdminDecisionStatisticsResponse> getAdminDecisionStatistics() {

    return ResponseEntity.ok(
            analyticsService.getAdminDecisionStatistics()
    );
}

@GetMapping("/admin/discussions")
public ResponseEntity<PlatformDiscussionAnalyticsResponse>
getPlatformDiscussionAnalytics() {

    return ResponseEntity.ok(
            analyticsService.getPlatformDiscussionAnalytics()
    );
}

@GetMapping("/admin/feedback")
public ResponseEntity<AdminFeedbackAnalyticsResponse>
getAdminFeedbackAnalytics() {

    return ResponseEntity.ok(
            analyticsService.getAdminFeedbackAnalytics()
    );
}
}