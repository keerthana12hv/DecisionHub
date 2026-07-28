import axios from "axios";

const API_URL = "http://localhost:8080/api/comments";

const authHeader = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
});

export const getComments = (decisionId) =>
  axios.get(`${API_URL}/decision/${decisionId}`, authHeader());

export const postComment = (decisionId, content) =>
  axios.post(API_URL, { decisionId, content }, authHeader());

export const postReply = (commentId, content) =>
  axios.post(`${API_URL}/${commentId}/reply`, { content }, authHeader());

export const deleteComment = (commentId) =>
  axios.delete(`${API_URL}/${commentId}`, authHeader());