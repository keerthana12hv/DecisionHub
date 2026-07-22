import axios from "axios";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

// Core community actions
export const getCommunities = async () => {
  const res = await axios.get(`${API}/communities`, headers());
  return res.data;
};

export const createCommunity = async (data) => {
  const res = await axios.post(`${API}/communities`, data, headers());
  return res.data;
};

export const joinCommunity = async (communityId) => {
  const res = await axios.post(`${API}/communities/${communityId}/join`, {}, headers());
  return res.data;
};

export const leaveCommunity = async (communityId) => {
  const res = await axios.post(`${API}/communities/${communityId}/leave`, {}, headers());
  return res.data;
};

// Moderator actions
export const getModeratingCommunities = () =>
  axios.get(`${API}/communities/moderating`, headers());

export const getJoinRequests = (communityId) =>
  axios.get(`${API}/communities/${communityId}/requests`, headers());

export const approveRequest = (communityId, memberId) =>
  axios.put(`${API}/communities/${communityId}/requests/${memberId}/approve`, {}, headers());

export const rejectRequest = (communityId, memberId) =>
  axios.put(`${API}/communities/${communityId}/requests/${memberId}/reject`, {}, headers());

export const getMembers = (communityId) =>
  axios.get(`${API}/communities/${communityId}/members`, headers());

export const removeMember = (communityId, memberId) =>
  axios.delete(`${API}/communities/${communityId}/members/${memberId}`, headers());