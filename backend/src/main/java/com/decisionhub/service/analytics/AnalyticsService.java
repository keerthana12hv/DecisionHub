package com.decisionhub.service.analytics;

import com.decisionhub.dto.response.analytics.CommunityOverviewResponse;
import com.decisionhub.dto.response.analytics.CommunityVotingStatisticsResponse;
import com.decisionhub.dto.response.analytics.DecisionFeedbackAnalyticsResponse;
import com.decisionhub.dto.response.analytics.DecisionOverviewResponse;
import com.decisionhub.dto.response.analytics.DiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.ParticipationResponse;
import com.decisionhub.dto.response.analytics.PlatformDiscussionAnalyticsResponse;
import com.decisionhub.dto.response.analytics.PlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.VoteDistributionResponse;
import com.decisionhub.dto.response.analytics.VoteStatisticsResponse;
import java.util.List;
import com.decisionhub.dto.response.analytics.RankingResponse;
import com.decisionhub.dto.response.analytics.UserAnalyticsResponse;
import com.decisionhub.dto.response.analytics.AdminDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityActivityResponse;
import com.decisionhub.dto.response.analytics.CommunityAnalyticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityModerationAnalyticsResponse;
import com.decisionhub.dto.response.analytics.AdminFeedbackAnalyticsResponse;
import com.decisionhub.dto.response.analytics.UserPlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.UserDecisionStatisticsResponse;
import com.decisionhub.dto.response.authentication.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnalyticsService {

    UserPlatformOverviewResponse getUserPlatformOverview();
    UserDecisionStatisticsResponse getUserDecisionStatistics();
    Page<UserResponse> getAllUsers(Pageable pageable);

DecisionOverviewResponse getDecisionOverview(Long decisionId);

VoteStatisticsResponse getVoteStatistics(Long decisionId);

    List<VoteDistributionResponse> getVoteDistribution(Long decisionId);

    ParticipationResponse getParticipation(Long decisionId);

    DiscussionStatisticsResponse getDiscussionStatistics(Long decisionId);

    List<RankingResponse> getFinalRanking(Long decisionId);

    CommunityOverviewResponse getCommunityOverview(Long communityId);

    CommunityDecisionStatisticsResponse getCommunityDecisionStatistics(Long communityId);

    CommunityVotingStatisticsResponse
getCommunityVotingStatistics(Long communityId);
    
    CommunityDiscussionStatisticsResponse
getCommunityDiscussionStatistics(Long communityId);



DecisionFeedbackAnalyticsResponse getDecisionFeedback(Long decisionId);
CommunityActivityResponse getCommunityActivity(Long communityId);

CommunityModerationAnalyticsResponse
getCommunityModerationAnalytics(Long communityId);

PlatformOverviewResponse getPlatformOverview();
UserAnalyticsResponse getUserAnalytics();

CommunityAnalyticsResponse getCommunityAnalytics();

AdminDecisionStatisticsResponse getAdminDecisionStatistics();
PlatformDiscussionAnalyticsResponse
getPlatformDiscussionAnalytics();

AdminFeedbackAnalyticsResponse getAdminFeedbackAnalytics();
}