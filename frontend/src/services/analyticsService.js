import api from "./api";

// Object Export (for components like AdminAnalyticsView, DecisionAnalyticsView, etc.)
export const analyticsService = {
  // 1. DECISION ANALYTICS (USER/MODERATOR)
  getDecisionOverview: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/overview`);
    return res.data;
  },

  getVoteStatistics: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/votes`);
    return res.data;
  },

  getVoteDistribution: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/distribution`);
    return res.data;
  },

  getParticipation: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/participation`);
    return res.data;
  },

  getDiscussion: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/discussion`);
    return res.data;
  },

  getRanking: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/ranking`);
    return res.data;
  },

  getFeedback: async (decisionId) => {
    const res = await api.get(`/api/analytics/decisions/${decisionId}/feedback`);
    return res.data;
  },

  fetchAllDecisionAnalytics: async (decisionId) => {
    const [overview, voteStats, distribution, participation, discussion, ranking, feedback] = 
      await Promise.allSettled([
        analyticsService.getDecisionOverview(decisionId),
        analyticsService.getVoteStatistics(decisionId),
        analyticsService.getVoteDistribution(decisionId),
        analyticsService.getParticipation(decisionId),
        analyticsService.getDiscussion(decisionId),
        analyticsService.getRanking(decisionId),
        analyticsService.getFeedback(decisionId),
      ]);

    return {
      overview: overview.status === "fulfilled" ? overview.value : null,
      voteStats: voteStats.status === "fulfilled" ? voteStats.value : null,
      distribution: distribution.status === "fulfilled" ? distribution.value : [],
      participation: participation.status === "fulfilled" ? participation.value : null,
      discussion: discussion.status === "fulfilled" ? discussion.value : null,
      ranking: ranking.status === "fulfilled" ? ranking.value : [],
      feedback: feedback.status === "fulfilled" ? feedback.value : null,
    };
  },

  // 2. COMMUNITY ANALYTICS (MODERATOR)
  getCommunityOverview: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/overview`);
    return res.data;
  },

  getCommunityDecisions: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/decisions`);
    return res.data;
  },

  getCommunityVoting: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/voting`);
    return res.data;
  },

  getCommunityDiscussion: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/discussion`);
    return res.data;
  },

  getCommunityActivity: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/activity`);
    return res.data;
  },

  getCommunityModeration: async (communityId) => {
    const res = await api.get(`/api/analytics/communities/${communityId}/moderation`);
    return res.data;
  },

  fetchAllCommunityAnalytics: async (communityId) => {
    const [overview, decisions, voting, discussion, activity, moderation] = 
      await Promise.allSettled([
        analyticsService.getCommunityOverview(communityId),
        analyticsService.getCommunityDecisions(communityId),
        analyticsService.getCommunityVoting(communityId),
        analyticsService.getCommunityDiscussion(communityId),
        analyticsService.getCommunityActivity(communityId),
        analyticsService.getCommunityModeration(communityId),
      ]);

    return {
      overview: overview.status === "fulfilled" ? overview.value : null,
      decisions: decisions.status === "fulfilled" ? decisions.value : null,
      voting: voting.status === "fulfilled" ? voting.value : null,
      discussion: discussion.status === "fulfilled" ? discussion.value : null,
      activity: activity.status === "fulfilled" ? activity.value : null,
      moderation: moderation.status === "fulfilled" ? moderation.value : null,
    };
  },

  // 3. ADMIN ANALYTICS (PLATFORM ADMIN)
  getAdminDashboard: async () => {
    const res = await api.get("/api/analytics/admin/dashboard");
    return res.data;
  },

  getAdminUsers: async () => {
    const res = await api.get("/api/analytics/admin/users");
    return res.data;
  },

  getAdminCommunities: async () => {
    const res = await api.get("/api/analytics/admin/communities");
    return res.data;
  },

  getAdminDecisions: async () => {
    const res = await api.get("/api/analytics/admin/decisions");
    return res.data;
  },

  getAdminDiscussion: async () => {
    const res = await api.get("/api/analytics/admin/discussion");
    return res.data;
  },

  getAdminFeedback: async () => {
    const res = await api.get("/api/analytics/admin/feedback");
    return res.data;
  },

  getAdminUsersList: async (page = 0, size = 10) => {
    const res = await api.get(`/api/analytics/admin/users-list?page=${page}&size=${size}`);
    return res.data;
  },

  fetchAllAdminAnalytics: async () => {
    const [dashboard, users, communities, decisions, discussion, feedback, usersList] = 
      await Promise.allSettled([
        analyticsService.getAdminDashboard(),
        analyticsService.getAdminUsers(),
        analyticsService.getAdminCommunities(),
        analyticsService.getAdminDecisions(),
        analyticsService.getAdminDiscussion(),
        analyticsService.getAdminFeedback(),
        analyticsService.getAdminUsersList(0, 5),
      ]);

    return {
      dashboard: dashboard.status === "fulfilled" ? dashboard.value : null,
      users: users.status === "fulfilled" ? users.value : null,
      communities: communities.status === "fulfilled" ? communities.value : null,
      decisions: decisions.status === "fulfilled" ? decisions.value : null,
      discussion: discussion.status === "fulfilled" ? discussion.value : null,
      feedback: feedback.status === "fulfilled" ? feedback.value : null,
      usersList: usersList.status === "fulfilled" ? usersList.value : { content: [] },
    };
  }
};

// Named Exports (for Sriram's pages)
export const getUserAnalyticsOverview = async () => {
  const response = await api.get("/api/analytics/overview");
  return response.data;
};

export const getUserDecisionStats = async () => {
  const response = await api.get("/api/analytics/decisions/statistics");
  return response.data;
};

export const getAdminPlatformOverview = async () => {
  const response = await api.get("/api/analytics/admin/dashboard");
  return response.data;
};

export const getAdminDecisionStats = async () => {
  const response = await api.get("/api/analytics/admin/decisions");
  return response.data;
};

export const getAdminUserStats = async () => {
  const response = await api.get("/api/analytics/admin/users");
  return response.data;
};

export const getAdminCommunityStats = async () => {
  const response = await api.get("/api/analytics/admin/communities");
  return response.data;
};

export const getAdminUsersList = async (page = 0, size = 10) => {
  const response = await api.get(`/api/analytics/admin/users-list?page=${page}&size=${size}`);
  return response.data;
};
