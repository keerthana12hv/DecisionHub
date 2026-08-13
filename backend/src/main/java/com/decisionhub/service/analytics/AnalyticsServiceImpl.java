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




    @Override
public DecisionOverviewResponse getDecisionOverview(Long decisionId) {

    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Poll not found"));

    return new DecisionOverviewResponse(
            decision.getTitle(),
            decision.getStatus().name(),
            poll.getStatus().name(),
            poll.getEndTime(),
            decision.getDeadline()
    );
}

@Override
public VoteStatisticsResponse getVoteStatistics(Long decisionId) {

    Decision decision = decisionRepository.findById(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Decision not found"));

    Poll poll = pollRepository.findByDecisionId(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Poll not found"));

    Long totalVotes = voteRepository.countByPollId(poll.getId());

    Long totalParticipants;

if (decision.getCommunity() != null) {
    totalParticipants = voteRepository.countParticipants(
            decisionId,
            decision.getCommunity().getId(),
            MembershipStatus.APPROVED
    );
} else {
    totalParticipants = voteRepository.countDistinctByPollId(poll.getId());
}
    Long numberOfOptions =
            decisionOptionRepository.countByDecisionId(decisionId);

    Long eligibleUsers = 0L;

    if (decision.getCommunity() != null) {

        eligibleUsers =
                communityMemberRepository.countByCommunityIdAndStatus(
                        decision.getCommunity().getId(),
                        MembershipStatus.APPROVED
                );
    }

    double votePercentage = 0;

    if (eligibleUsers > 0) {
        votePercentage =
                (totalParticipants.doubleValue() * 100)
                        / eligibleUsers.doubleValue();
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

    Long pollId = pollRepository.findByDecisionId(decisionId)
            .orElseThrow()
            .getId();

    List<Object[]> result = voteRepository.getVoteDistribution(decisionId);

    long totalVotes = voteRepository.countByPollId(pollId);

    List<VoteDistributionResponse> response = new ArrayList<>();

    for(Object[] row : result){

        Long optionId=(Long)row[0];

        String optionName=(String)row[1];

        Long votes=(Long)row[2];

        double percentage=0;

        if(totalVotes!=0){

            percentage=((double)votes*100)/totalVotes;

        }

        response.add(
                new VoteDistributionResponse(
                        optionId,
                        optionName,
                        votes,
                        Math.round(percentage*100.0)/100.0
                )
        );

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
            .orElseThrow(() ->
                    new ResourceNotFoundException("Decision not found"));

    if (decision.getCommunity() == null) {
        return new ParticipationResponse(
                0L,
                0L,
                0L,
                0.0
        );
    }

    Long eligibleUsers =
            communityMemberRepository.countByCommunityIdAndStatus(
                    decision.getCommunity().getId(),
                    MembershipStatus.APPROVED
            );

   Long usersVoted =
    voteRepository.countParticipants(
        decisionId,
        decision.getCommunity().getId(),
        MembershipStatus.APPROVED
    );
    Long usersNotVoted = eligibleUsers - usersVoted;

    double percentage = 0;

    if (eligibleUsers > 0) {

        percentage =
                (usersVoted.doubleValue() * 100) /
                        eligibleUsers.doubleValue();

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

    List<Object[]> result = voteRepository.getVoteDistribution(decisionId);

    Long pollId = pollRepository.findByDecisionId(decisionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Poll not found"))
            .getId();

    long totalVotes = voteRepository.countByPollId(pollId);

    List<RankingResponse> response = new ArrayList<>();

    int rank = 1;

    for (Object[] row : result) {

        Long optionId = (Long) row[0];
        String optionName = (String) row[1];
        Long voteCount = (Long) row[2];

        double percentage = 0;

        if (totalVotes > 0) {
            percentage = (voteCount.doubleValue() * 100) / totalVotes;
        }

        response.add(
                new RankingResponse(
                        rank++,
                        optionId,
                        optionName,
                        voteCount,
                        Math.round(percentage * 100.0) / 100.0
                )
        );
    }

    return response;
}


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

    DecisionFeedback feedback = decisionFeedbackRepository
            .findByDecision(decision)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Feedback not found"));

    Long feedbackCount =
            decisionFeedbackRepository.countByDecisionId(decisionId);

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
            feedback.getRating().doubleValue(),
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
public List<UserResponse> getAdminUsersList() {
    List<UserResponse> responses = new ArrayList<>();
    for (User user : userRepository.findAll()) {
        responses.add(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        ));
    }
    return responses;
}
}