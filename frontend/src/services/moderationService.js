import api from "./api";

// MY MODERATING COMMUNITIES
export const getModeratingCommunities = async () => {
  const res = await api.get("/communities/moderating");
  return res.data;
};

// JOIN REQUESTS
export const getJoinRequests = async (communityId) => {
  const res = await api.get(`/communities/${communityId}/requests`);
  return res.data;
};

export const approveRequest = async (communityId, memberId) => {
  const res = await api.put(
      `/communities/${communityId}/requests/${memberId}/approve`,
      {}
  );
  return res.data;
};

export const rejectRequest = async (communityId, memberId) => {
  const res = await api.put(
      `/communities/${communityId}/requests/${memberId}/reject`,
      {}
  );
  return res.data;
};

// COMMUNITY MEMBERS
export const getCommunityMembers = async (communityId) => {
  const res = await api.get(`/communities/${communityId}/members`);
  return res.data;
};

export const removeMember = async (communityId, memberId) => {
  const res = await api.delete(
      `/communities/${communityId}/members/${memberId}`
  );
  return res.data;
};

// COMMUNITY RULES
export const getCommunityRules = async (communityId) => {
  const res = await api.get(`/communities/${communityId}/rules`);
  return res.data;
};

export const addCommunityRule = async (communityId, ruleData) => {
  const res = await api.post(
      `/communities/${communityId}/rules`,
      ruleData
  );
  return res.data;
};

export const updateCommunityRule = async (
    communityId,
    ruleId,
    ruleData
) => {
  const res = await api.put(
      `/communities/${communityId}/rules/${ruleId}`,
      ruleData
  );
  return res.data;
};

export const deleteCommunityRule = async (communityId, ruleId) => {
  const res = await api.delete(
      `/communities/${communityId}/rules/${ruleId}`
  );
  return res.data;
};

// DECISION MODERATION
export const lockDecision = async (decisionId) => {
  const res = await api.put(
      `/moderation/decisions/${decisionId}/lock`,
      {}
  );
  return res.data;
};

export const unlockDecision = async (decisionId) => {
  const res = await api.put(
      `/moderation/decisions/${decisionId}/unlock`,
      {}
  );
  return res.data;
};

export const pinDecision = async (decisionId) => {
  const res = await api.put(
      `/moderation/decisions/${decisionId}/pin`,
      {}
  );
  return res.data;
};

export const unpinDecision = async (decisionId) => {
  const res = await api.put(
      `/moderation/decisions/${decisionId}/unpin`,
      {}
  );
  return res.data;
};

export const reportComment = async (commentId, reason) => {
  const res = await api.post(
      `/comments/${commentId}/report`,
      { reason }
  );
  return res.data;
};

export const closeDecision = async (decisionId) => {
  const res = await api.put(
      `/decisions/${decisionId}/close`,
      {}
  );
  return res.data;
};

// COMMENT MODERATION
export const pinComment = async (commentId) => {
  const res = await api.put(
      `/moderation/comments/${commentId}/pin`,
      {}
  );
  return res.data;
};

export const unpinComment = async (commentId) => {
  const res = await api.put(
      `/moderation/comments/${commentId}/unpin`,
      {}
  );
  return res.data;
};

export const modDeleteComment = async (commentId) => {
  const res = await api.delete(
      `/moderation/comments/${commentId}`
  );
  return res.data;
};

// REPORT MANAGEMENT
export const getReportedComments = async () => {
  const res = await api.get("/moderation/reports");
  return res.data;
};

export const deleteReport = async (reportId) => {
  const res = await api.delete(
      `/moderation/reports/${reportId}`
  );
  return res.data;
};