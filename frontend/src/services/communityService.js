import api from "./api";

// Core community actions
export const getCommunities = async () => {
  const res = await api.get("/communities");
  return res.data;
};

export const getMyCommunities = async () => {
  const res = await api.get("/communities/my");
  return res.data;
};

export const getCategories = async () => {
  const res = await api.get("/categories");
  return res.data;
};

export const createCommunity = async (data) => {
  const res = await api.post("/communities", data);
  return res.data;
};

export const joinCommunity = async (communityId) => {
  const res = await api.post(`/communities/${communityId}/join`, {});
  return res.data;
};

export const leaveCommunity = async (communityId) => {
  const res = await api.post(`/communities/${communityId}/leave`, {});
  return res.data;
};

// Moderator actions
export const getModeratingCommunities = () =>
  api.get("/communities/moderating");

export const getJoinRequests = (communityId) =>
  api.get(`/communities/${communityId}/requests`);

export const approveRequest = (communityId, memberId) =>
  api.put(`/communities/${communityId}/requests/${memberId}/approve`, {});

export const rejectRequest = (communityId, memberId) =>
  api.put(`/communities/${communityId}/requests/${memberId}/reject`, {});

export const getMembers = (communityId) =>
  api.get(`/communities/${communityId}/members`);

export const removeMember = (communityId, memberId) =>
  api.delete(`/communities/${communityId}/members/${memberId}`);

// Community Rules
export const getRules = (communityId) =>
  api.get(`/communities/${communityId}/rules`);

export const createRule = (communityId, data) =>
  api.post(`/moderation/communities/${communityId}/rules`, data);

export const updateRule = (ruleId, data) =>
  api.put(`/moderation/rules/${ruleId}`, data);

export const deleteRule = (ruleId) =>
  api.delete(`/moderation/rules/${ruleId}`);

// Decisions
export const getDecisions = (params) =>
  api.get("/decisions", { params });

// Decision Moderation
export const lockDecision = (decisionId) =>
  api.put(`/moderation/decisions/${decisionId}/lock`, {});

export const unlockDecision = (decisionId) =>
  api.put(`/moderation/decisions/${decisionId}/unlock`, {});

export const pinDecision = (decisionId) =>
  api.put(`/moderation/decisions/${decisionId}/pin`, {});

export const unpinDecision = (decisionId) =>
  api.put(`/moderation/decisions/${decisionId}/unpin`, {});
