import axios from "axios";

const API_URL = "http://localhost:8080/api";

const authHeader = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
});

export const getComments = (decisionId) =>
  axios.get(`${API_URL}/decisions/${decisionId}/comments`, authHeader());

export const postComment = (decisionId, content) =>
  axios.post(`${API_URL}/decisions/${decisionId}/comments`, { content }, authHeader());

export const postReply = (decisionId, commentId, content) =>
  axios.post(
    `${API_URL}/decisions/${decisionId}/comments/${commentId}/replies`,
    { content },
    authHeader()
  );

export const getReplies = (commentId) =>
  axios.get(`${API_URL}/comments/${commentId}/replies`, authHeader());

export const editComment = (commentId, content) =>
  axios.put(`${API_URL}/comments/${commentId}`, { content }, authHeader());

export const deleteComment = (commentId) =>
  axios.delete(`${API_URL}/comments/${commentId}`, authHeader());