import axios from "axios";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

// ─── MY MODERATING COMMUNITIES ────────────────────────────────────────────────
export const getModeratingCommunities = async () => {
  const res = await axios.get(`${API}/communities/moderating`, headers());
  return res.data;
};

// ─── JOIN REQUESTS (Private Communities) ──────────────────────────────────────
export const getJoinRequests = async (communityId) => {
  const res = await axios.get(
    `${API}/communities/${communityId}/requests`,
    headers()
  );
  return res.data;
};

export const approveRequest = async (communityId, memberId) => {
  const res = await axios.put(
    `${API}/communities/${communityId}/requests/${memberId}/approve`,
    {},
    headers()
  );
  return res.data;
};

export const rejectRequest = async (communityId, memberId) => {
  const res = await axios.put(
    `${API}/communities/${communityId}/requests/${memberId}/reject`,
    {},
    headers()
  );
  return res.data;
};

// ─── COMMUNITY MEMBERS ────────────────────────────────────────────────────────
export const getCommunityMembers = async (communityId) => {
  const res = await axios.get(
    `${API}/communities/${communityId}/members`,
    headers()
  );
  return res.data;
};

export const removeMember = async (communityId, memberId) => {
  const res = await axios.delete(
    `${API}/communities/${communityId}/members/${memberId}`,
    headers()
  );
  return res.data;
};

// ─── COMMUNITY RULES ──────────────────────────────────────────────────────────
export const getCommunityRules = async (communityId) => {
  const res = await axios.get(
    `${API}/communities/${communityId}/rules`,
    headers()
  );
  return res.data;
};

export const addCommunityRule = async (communityId, ruleData) => {
  const res = await axios.post(
    `${API}/communities/${communityId}/rules`,
    ruleData,
    headers()
  );
  return res.data;
};

export const updateCommunityRule = async (communityId, ruleId, ruleData) => {
  const res = await axios.put(
    `${API}/communities/${communityId}/rules/${ruleId}`,
    ruleData,
    headers()
  );
  return res.data;
};

export const deleteCommunityRule = async (communityId, ruleId) => {
  const res = await axios.delete(
    `${API}/communities/${communityId}/rules/${ruleId}`,
    headers()
  );
  return res.data;
};

// ─── DECISION MODERATION ─────────────────────────────────────────────────────
export const lockDecision = async (decisionId) => {
  const res = await axios.put(
    `${API}/moderation/decisions/${decisionId}/lock`,
    {},
    headers()
  );
  return res.data;
};

export const unlockDecision = async (decisionId) => {
  const res = await axios.put(
    `${API}/moderation/decisions/${decisionId}/unlock`,
    {},
    headers()
  );
  return res.data;
};

export const pinDecision = async (decisionId) => {
  const res = await axios.put(
    `${API}/moderation/decisions/${decisionId}/pin`,
    {},
    headers()
  );
  return res.data;
};

export const unpinDecision = async (decisionId) => {
  const res = await axios.put(
    `${API}/moderation/decisions/${decisionId}/unpin`,
    {},
    headers()
  );
  return res.data;
};