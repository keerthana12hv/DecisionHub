package com.decisionhub.service.analytics;

import com.decisionhub.dto.response.analytics.CommunityOverviewResponse;
import com.decisionhub.dto.response.analytics.DecisionOverviewResponse;
import com.decisionhub.dto.response.analytics.DiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.ParticipationResponse;
import com.decisionhub.dto.response.analytics.PlatformDiscussionAnalyticsResponse;
import com.decisionhub.dto.response.analytics.PlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.RankingResponse;
import com.decisionhub.dto.response.analytics.UserAnalyticsResponse;
import com.decisionhub.dto.response.analytics.VoteDistributionResponse;
import com.decisionhub.dto.response.analytics.VoteStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityDiscussionStatisticsResponse;
import com.decisionhub.dto.response.analytics.CommunityModerationAnalyticsResponse;
import com.decisionhub.entity.community.Community;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.entity.voting.Poll;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.dto.response.authentication.UserResponse;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.enums.community.CommunityVisibility;
import com.decisionhub.enums.community.MembershipStatus;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.enums.voting.PollStatus;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.repository.community.CommunityMemberRepository;
import com.decisionhub.repository.community.CommunityRepository;
import com.decisionhub.repository.decision.DecisionOptionRepository;
import com.decisionhub.repository.decision.DecisionRepository;
import com.decisionhub.repository.discussion.CommentRepository;
import com.decisionhub.repository.voting.PollRepository;
import com.decisionhub.repository.voting.VoteRepository;
import com.decisionhub.repository.decision.ComparisonScoreRepository;
import com.decisionhub.entity.decision.ComparisonScore;
import com.decisionhub.entity.decision.DecisionOption;
import com.decisionhub.enums.decision.VotingType;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.decisionhub.dto.response.analytics.CommunityVotingStatisticsResponse;
import com.decisionhub.dto.response.analytics.DecisionFeedbackAnalyticsResponse;
import com.decisionhub.repository.decision.DecisionFeedbackRepository;
import com.decisionhub.entity.decision.DecisionFeedback;
import com.decisionhub.dto.response.analytics.MemberActivityResponse;
import com.decisionhub.dto.response.analytics.AdminDecisionStatisticsResponse;
import com.decisionhub.dto.response.analytics.AdminFeedbackAnalyticsResponse;
import com.decisionhub.dto.response.analytics.CommunityActivityResponse;
import com.decisionhub.dto.response.analytics.CommunityAnalyticsResponse;
import com.decisionhub.dto.response.analytics.UserPlatformOverviewResponse;
import com.decisionhub.dto.response.analytics.UserDecisionStatisticsResponse;
import com.decisionhub.dto.response.authentication.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final DecisionRepository decisionRepository;

    private final DecisionOptionRepository decisionOptionRepository;

    private final PollRepository pollRepository;

    private final VoteRepository voteRepository;

    private final CommunityMemberRepository communityMemberRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
        private final DecisionFeedbackRepository decisionFeedbackRepository;
private final UserRepository userRepository;
private final ComparisonScoreRepository comparisonScoreRepository;




    @Override
public DecisionOverviewResponse getDecisionOverview(Long decisionId) {

    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElse(null);

    return new DecisionOverviewResponse(
            decision.getTitle(),
            decision.getStatus().name(),
            poll != null ? poll.getStatus().name() : "DRAFT",
            poll != null ? poll.getEndTime() : null,
            decision.getDeadline()
    );
}

@Override
public VoteStatisticsResponse getVoteStatistics(Long decisionId) {
    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElse(null);

    Long numberOfOptions = decisionOptionRepository.countByDecisionId(decisionId);

    if (poll == null) {
        return new VoteStatisticsResponse(0L, 0L, 0.0, numberOfOptions);
    }

    Long totalVotes;
    Long totalParticipants;
    double votePercentage = 0.0;
    Long eligibleUsers = 0L;

    if (decision.getCommunity() != null) {
        eligibleUsers = communityMemberRepository.countByCommunityIdAndStatus(
                decision.getCommunity().getId(),
                MembershipStatus.APPROVED
        );
    }

    if (decision.getVotingType() == VotingType.RATING_BASED) {
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);
        long uniqueVoters = scores.stream()
                .map(s -> s.getUser().getId())
                .distinct()
                .count();
        totalVotes = uniqueVoters;
        totalParticipants = uniqueVoters;
    } else {
        totalVotes = voteRepository.countByPollId(poll.getId());
        if (decision.getCommunity() != null) {
            totalParticipants = voteRepository.countParticipants(
                    decisionId,
                    decision.getCommunity().getId(),
                    MembershipStatus.APPROVED
            );
        } else {
            totalParticipants = voteRepository.countDistinctByPollId(poll.getId());
        }
    }

    if (eligibleUsers > 0) {
        votePercentage = (totalParticipants.doubleValue() * 100) / eligibleUsers.doubleValue();
    }

    return new VoteStatisticsResponse(
            totalVotes,
            totalParticipants,
            Math.round(votePercentage * 100.0) / 100.0,
            numberOfOptions
    );
}
@Override
public List<VoteDistributionResponse> getVoteDistribution(Long decisionId) {
    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElse(null);

    if (poll == null) {
        return new ArrayList<>();
    }

    if (decision.getVotingType() == VotingType.RATING_BASED) {
        List<DecisionOption> options = decisionOptionRepository.findByDecisionId(decisionId);
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);
        Map<Long, List<ComparisonScore>> scoresMap = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getOption().getId()));

        List<VoteDistributionResponse> response = new ArrayList<>();
        for (DecisionOption option : options) {
            List<ComparisonScore> optionScores = scoresMap.getOrDefault(option.getId(), new ArrayList<>());
            double averageScore = 0.0;
            if (!optionScores.isEmpty()) {
                double sum = 0.0;
                for (ComparisonScore score : optionScores) {
                    sum += score.getScore();
                }
                averageScore = sum / optionScores.size();
            }
            long voterCount = optionScores.stream().map(s -> s.getUser().getId()).distinct().count();
            response.add(new VoteDistributionResponse(
                    option.getId(),
                    option.getOptionName(),
                    voterCount,
                    Math.round(averageScore * 100.0) / 100.0
            ));
        }
        return response;
    }

    Long pollId = poll.getId();
    List<Object[]> result = voteRepository.getVoteDistribution(decisionId);
    long totalVotes = voteRepository.countByPollId(pollId);
    List<VoteDistributionResponse> response = new ArrayList<>();

    for (Object[] row : result) {
        Long optionId = (Long) row[0];
        String optionName = (String) row[1];
        Long votes = (Long) row[2];
        double percentage = 0.0;

        if (totalVotes != 0) {
            percentage = ((double) votes * 100) / totalVotes;
        }

        response.add(new VoteDistributionResponse(
                optionId,
                optionName,
                votes,
                Math.round(percentage * 100.0) / 100.0
        ));
    }
    return response;
}

@Override
public CommunityOverviewResponse getCommunityOverview(Long communityId) {

    Community community =
            communityRepository.findById(communityId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Community not found"));

    Long totalMembers =
            communityMemberRepository.countByCommunityIdAndStatus(
                    communityId,
                    MembershipStatus.APPROVED
            );

    Long totalDecisions =
            decisionRepository.countByCommunityId(
                    communityId
            );

    Long activeDecisions =
            decisionRepository.countByCommunityIdAndStatus(
                    communityId,
                    DecisionStatus.ACTIVE
            );

    Long closedDecisions =
            decisionRepository.countByCommunityIdAndStatus(
                    communityId,
                    DecisionStatus.CLOSED
            );

    return new CommunityOverviewResponse(

            community.getId(),

            community.getName(),

            totalMembers,

            totalDecisions,

            activeDecisions,

            closedDecisions

    );
}
@Override
public ParticipationResponse getParticipation(Long decisionId) {
    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

    if (decision.getCommunity() == null) {
        return new ParticipationResponse(0L, 0L, 0L, 0.0);
    }

    Long eligibleUsers = communityMemberRepository.countByCommunityIdAndStatus(
            decision.getCommunity().getId(),
            MembershipStatus.APPROVED
    );

    Long usersVoted;
    if (decision.getVotingType() == VotingType.RATING_BASED) {
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);
        usersVoted = scores.stream()
                .map(s -> s.getUser().getId())
                .distinct()
                .count();
    } else {
        usersVoted = voteRepository.countParticipants(
                decisionId,
                decision.getCommunity().getId(),
                MembershipStatus.APPROVED
        );
    }

    Long usersNotVoted = eligibleUsers - usersVoted;
    double percentage = 0.0;

    if (eligibleUsers > 0) {
        percentage = (usersVoted.doubleValue() * 100) / eligibleUsers.doubleValue();
    }

    return new ParticipationResponse(
            eligibleUsers,
            usersVoted,
            usersNotVoted,
            Math.round(percentage * 100.0) / 100.0
    );
}

@Override
public DiscussionStatisticsResponse getDiscussionStatistics(Long decisionId) {

    long totalComments =
            commentRepository.countByDecisionIdAndParentCommentIsNull(decisionId);

    long totalReplies =
            commentRepository.countByDecisionIdAndParentCommentIsNotNull(decisionId);

    return new DiscussionStatisticsResponse(
            totalComments,
            totalReplies,
            totalComments + totalReplies
    );
}
@Override
public List<RankingResponse> getFinalRanking(Long decisionId) {
    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElse(null);

    if (poll == null) {
        return new ArrayList<>();
    }

    if (decision.getVotingType() == VotingType.RATING_BASED) {
        List<DecisionOption> options = decisionOptionRepository.findByDecisionId(decisionId);
        List<ComparisonScore> scores = comparisonScoreRepository.findByOptionDecisionId(decisionId);

        Map<Long, List<ComparisonScore>> scoresMap = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getOption().getId()));

        List<RankingResponse> response = new ArrayList<>();
        List<IntermediateRanking> intermediateList = new ArrayList<>();

        for (DecisionOption option : options) {
            List<ComparisonScore> optionScores = scoresMap.getOrDefault(option.getId(), new ArrayList<>());
            double averageScore = 0.0;
            if (!optionScores.isEmpty()) {
                double sum = 0.0;
                for (ComparisonScore score : optionScores) {
                    sum += score.getScore();
                }
                averageScore = sum / optionScores.size();
            }
            long voterCount = optionScores.stream().map(s -> s.getUser().getId()).distinct().count();
            intermediateList.add(new IntermediateRanking(option.getId(), option.getOptionName(), voterCount, averageScore));
        }

        intermediateList.sort((a, b) -> Double.compare(b.score, a.score));

        int rank = 1;
        int count = 1;
        double prevScore = -1.0;
        for (int i = 0; i < intermediateList.size(); i++) {
            IntermediateRanking item = intermediateList.get(i);
            if (i > 0 && item.score < prevScore) {
                rank = count;
            }
            response.add(new RankingResponse(
                    rank,
                    item.optionId,
                    item.optionName,
                    item.voterCount,
                    Math.round(item.score * 100.0) / 100.0
            ));
            prevScore = item.score;
            count++;
        }
        return response;
    }

    Long pollId = poll.getId();
    List<Object[]> result = voteRepository.getVoteDistribution(decisionId);
    long totalVotes = voteRepository.countByPollId(pollId);
    List<RankingResponse> response = new ArrayList<>();

    int rank = 1;
    for (Object[] row : result) {
        Long optionId = (Long) row[0];
        String optionName = (String) row[1];
        Long voteCount = (Long) row[2];

        double percentage = 0.0;
        if (totalVotes > 0) {
            percentage = (voteCount.doubleValue() * 100) / totalVotes;
        }

        response.add(new RankingResponse(
                rank++,
                optionId,
                optionName,
                voteCount,
                Math.round(percentage * 100.0) / 100.0
        ));
    }
    return response;
}

@Override


@Override
public CommunityDecisionStatisticsResponse getCommunityDecisionStatistics(Long communityId) {

    Community community = communityRepository.findById(communityId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Community not found"));

    Long totalDecisions =
            decisionRepository.countByCommunityId(communityId);

    Long activeDecisions =
            decisionRepository.countByCommunityIdAndStatus(
                    communityId,
                    DecisionStatus.ACTIVE
            );

    Long closedDecisions =
            decisionRepository.countByCommunityIdAndStatus(
                    communityId,
                    DecisionStatus.CLOSED
            );

    Long otherDecisions =
            totalDecisions - activeDecisions - closedDecisions;

    return new CommunityDecisionStatisticsResponse(

            community.getId(),

            community.getName(),

            totalDecisions,

            activeDecisions,

            closedDecisions,

            otherDecisions

    );
}



@Override
public CommunityVotingStatisticsResponse
getCommunityVotingStatistics(Long communityId) {

    Community community =
            communityRepository.findById(communityId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Community not found"));

    Long totalPolls =
            pollRepository.countByDecisionCommunityId(communityId);

    Long openPolls =
            pollRepository.countByDecisionCommunityIdAndStatus(
                    communityId,
                    PollStatus.OPEN
            );

    Long closedPolls =
            pollRepository.countByDecisionCommunityIdAndStatus(
                    communityId,
                    PollStatus.CLOSED
            );

    Long totalVotes =
            voteRepository.countVotesByCommunity(communityId);

    double averageVotes = 0;

    if (totalPolls > 0) {
        averageVotes =
                totalVotes.doubleValue() /
                        totalPolls.doubleValue();
    }

    return new CommunityVotingStatisticsResponse(

            community.getId(),

            community.getName(),

            totalPolls,

            openPolls,

            closedPolls,

            totalVotes,

            Math.round(averageVotes * 100.0) / 100.0

    );

}


@Override
public CommunityDiscussionStatisticsResponse
getCommunityDiscussionStatistics(Long communityId) {

    Community community =
            communityRepository.findById(communityId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Community not found"));

    Long totalComments =
            commentRepository.countByDecisionCommunityIdAndParentCommentIsNull(
                    communityId
            );

    Long totalReplies =
            commentRepository.countByDecisionCommunityIdAndParentCommentIsNotNull(
                    communityId
            );

    return new CommunityDiscussionStatisticsResponse(

            community.getId(),

            community.getName(),

            totalComments,

            totalReplies,

            totalComments + totalReplies

    );
}


@Override
public DecisionFeedbackAnalyticsResponse getDecisionFeedback(Long decisionId) {

    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Decision not found"));

    Long feedbackCount =
            decisionFeedbackRepository.countByDecisionId(decisionId);

    if (feedbackCount == null || feedbackCount == 0L) {
        return new DecisionFeedbackAnalyticsResponse(
                0.0,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L
        );
    }

    Double averageRating = decisionFeedbackRepository.getAverageRatingByDecisionId(decisionId);
    if (averageRating == null) {
        averageRating = 0.0;
    }

    Long fiveStar =
            decisionFeedbackRepository.countByDecisionIdAndRating(decisionId, 5);

    Long fourStar =
            decisionFeedbackRepository.countByDecisionIdAndRating(decisionId, 4);

    Long threeStar =
            decisionFeedbackRepository.countByDecisionIdAndRating(decisionId, 3);

    Long twoStar =
            decisionFeedbackRepository.countByDecisionIdAndRating(decisionId, 2);

    Long oneStar =
            decisionFeedbackRepository.countByDecisionIdAndRating(decisionId, 1);

    return new DecisionFeedbackAnalyticsResponse(
            averageRating,
            feedbackCount,
            fiveStar,
            fourStar,
            threeStar,
            twoStar,
            oneStar
    );
}

@Override
public CommunityActivityResponse getCommunityActivity(Long communityId) {

    communityRepository.findById(communityId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Community not found"));

    List<MemberActivityResponse> mostActiveMembers = new ArrayList<>();

    List<MemberActivityResponse> highestParticipationMembers = new ArrayList<>();

    List<MemberActivityResponse> topDecisionCreators = new ArrayList<>();

    // Most Active Members (Comments)

    for (Object[] row : commentRepository.getMostActiveMembers(communityId)) {

        mostActiveMembers.add(
                new MemberActivityResponse(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]
                )
        );
    }

    // Highest Participation Members (Votes)

    for (Object[] row : voteRepository.getHighestParticipationMembers(communityId)) {

        highestParticipationMembers.add(
                new MemberActivityResponse(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]
                )
        );
    }

    // Top Decision Creators

    for (Object[] row : decisionRepository.getTopDecisionCreators(communityId)) {

        topDecisionCreators.add(
                new MemberActivityResponse(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]
                )
        );
    }

    return new CommunityActivityResponse(
            mostActiveMembers,
            highestParticipationMembers,
            topDecisionCreators
    );
}

@Override
public CommunityModerationAnalyticsResponse
getCommunityModerationAnalytics(Long communityId) {

    long locked =
            decisionRepository.countByCommunityIdAndLockedTrue(communityId);

    long unlocked =
            decisionRepository.countByCommunityIdAndLockedFalse(communityId);

    long pinned =
            decisionRepository.countByCommunityIdAndPinnedTrue(communityId);

    long removed =
            commentRepository.countByDecisionCommunityIdAndDeletedAtIsNotNull(
                    communityId
            );

    long reported = 0;

    return new CommunityModerationAnalyticsResponse(
            locked,
            unlocked,
            pinned,
            reported,
            removed
    );
}

@Override
public PlatformOverviewResponse getPlatformOverview() {

    long totalUsers = userRepository.count();

    long totalCommunities = communityRepository.countByDeletedAtIsNull();

    long totalDecisions = decisionRepository.count();

    long totalVotes = voteRepository.count();

    long totalComments =
            commentRepository.countByParentCommentIsNull();

    long totalReplies =
            commentRepository.countByParentCommentIsNotNull();

    return new PlatformOverviewResponse(
            totalUsers,
            totalCommunities,
            totalDecisions,
            totalVotes,
            totalComments,
            totalReplies
    );
}


@Override
public UserAnalyticsResponse getUserAnalytics() {

    long totalUsers = userRepository.count();

    long activeUsers =
            userRepository.countByStatus(UserStatus.ACTIVE);

    long inactiveUsers =
            userRepository.countByStatus(UserStatus.INACTIVE);

    long suspendedUsers =
            userRepository.countByStatus(UserStatus.SUSPENDED);

    return new UserAnalyticsResponse(
            totalUsers,
            activeUsers,
            inactiveUsers,
            suspendedUsers
    );
}


@Override
public CommunityAnalyticsResponse getCommunityAnalytics() {

    long totalCommunities = communityRepository.countByDeletedAtIsNull();

    long publicCommunities =
            communityRepository.countByVisibilityAndDeletedAtIsNull(
                    CommunityVisibility.PUBLIC
            );

    long privateCommunities =
            communityRepository.countByVisibilityAndDeletedAtIsNull(
                    CommunityVisibility.PRIVATE
            );

    Community community =
            communityRepository.findTopByOrderByMemberCountDesc();

    String mostActiveCommunity =
            community != null ? community.getName() : "N/A";

    return new CommunityAnalyticsResponse(
            totalCommunities,
            publicCommunities,
            privateCommunities,
            mostActiveCommunity
    );
}

@Override
public AdminDecisionStatisticsResponse getAdminDecisionStatistics() {

    return new AdminDecisionStatisticsResponse(

            decisionRepository.count(),

            decisionRepository.countByStatus(DecisionStatus.DRAFT),

            decisionRepository.countByStatus(DecisionStatus.ACTIVE),

            decisionRepository.countByStatus(DecisionStatus.CLOSED),

            decisionRepository.countByVisibility(DecisionVisibility.PUBLIC),

            decisionRepository.countByVisibility(DecisionVisibility.COMMUNITY)
    );
}



@Override
public PlatformDiscussionAnalyticsResponse getPlatformDiscussionAnalytics() {

    long comments = commentRepository.countTopLevelComments();

    long replies = commentRepository.countReplies();

    long totalDecisions = decisionRepository.count();

    double average = totalDecisions == 0
            ? 0.0
            : (double) comments / totalDecisions;

    String mostActiveDecision =
            decisionRepository.findMostActiveDecision();

    return new PlatformDiscussionAnalyticsResponse(
            comments,
            replies,
            average,
            mostActiveDecision
    );
}

@Override
public AdminFeedbackAnalyticsResponse getAdminFeedbackAnalytics() {

    long total = decisionFeedbackRepository.count();

    Double average = decisionFeedbackRepository.getAverageRating();

    if (average == null) {
        average = 0.0;
    }

    average = Math.round(average * 100.0) / 100.0;

    return new AdminFeedbackAnalyticsResponse(
            total,
            average,
            decisionFeedbackRepository.countByRating(5),
            decisionFeedbackRepository.countByRating(4),
            decisionFeedbackRepository.countByRating(3),
            decisionFeedbackRepository.countByRating(2),
            decisionFeedbackRepository.countByRating(1)
    );
}

@Override
public UserPlatformOverviewResponse getUserPlatformOverview() {
    long totalVotes = voteRepository.count();
    long activeDecisions = decisionRepository.countByStatus(DecisionStatus.ACTIVE);
    long totalUsers = userRepository.count();

    double participationRate = 0.0;
    if (totalUsers > 0) {
        participationRate = (double) totalVotes / totalUsers;
    }

    String mostPopularDecision = decisionRepository.findMostActiveDecision();
    if (mostPopularDecision == null) {
        mostPopularDecision = "N/A";
    }

    return new UserPlatformOverviewResponse(
            totalVotes,
            activeDecisions,
            Math.round(participationRate * 100.0) / 100.0,
            mostPopularDecision
    );
}

@Override
public UserDecisionStatisticsResponse getUserDecisionStatistics() {
    long activeDecisions = decisionRepository.countByStatus(DecisionStatus.ACTIVE);
    long closedDecisions = decisionRepository.countByStatus(DecisionStatus.CLOSED);
    long totalDecisions = activeDecisions + closedDecisions;

    return new UserDecisionStatisticsResponse(
            totalDecisions,
            activeDecisions,
            closedDecisions
    );
}

@Override
public Page<UserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable)
            .map(u -> new UserResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole(),
                    u.getStatus()
            ));
}

private static class IntermediateRanking {
    Long optionId;
    String optionName;
    Long voterCount;
    double score;
    
    IntermediateRanking(Long optionId, String optionName, Long voterCount, double score) {
        this.optionId = optionId;
        this.optionName = optionName;
        this.voterCount = voterCount;
        this.score = score;
    }
}
}