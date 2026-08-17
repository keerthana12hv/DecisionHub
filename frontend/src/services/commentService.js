import api from "./api";

export const getComments = (decisionId) =>
  api.get(`/decisions/${decisionId}/comments`);

export const postComment = (decisionId, content) =>
  api.post(`/decisions/${decisionId}/comments`, { content });

export const postReply = (decisionId, commentId, content) =>
  api.post(`/decisions/${decisionId}/comments/${commentId}/replies`, {
    content,
  });

export const getReplies = (commentId) =>
  api.get(`/comments/${commentId}/replies`);

export const editComment = (commentId, content) =>
  api.put(`/comments/${commentId}`, { content });

export const deleteComment = (commentId) =>
  api.delete(`/comments/${commentId}`);
