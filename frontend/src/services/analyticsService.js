import api from "./api";

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
