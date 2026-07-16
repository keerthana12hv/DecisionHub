import api from "./api";

export const getCommunities = async () => {
  const response = await api.get("/api/communities");
  return response.data;
};

export const createCommunity = async (data) => {
  const response = await api.post("/api/communities", data);
  return response.data;
};

export const updateCommunity = async (id, data) => {
  const response = await api.put(`/api/communities/${id}`, data);
  return response.data;
};

export const deleteCommunity = async (id) => {
  const response = await api.delete(`/api/communities/${id}`);
  return response.data;
};

export const joinCommunity = async (id) => {
  const response = await api.post(`/api/communities/${id}/join`);
  return response.data;
};

export const leaveCommunity = async (id) => {
  const response = await api.post(`/api/communities/${id}/leave`);
  return response.data;
};