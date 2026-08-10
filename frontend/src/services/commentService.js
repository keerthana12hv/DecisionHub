import api from "./api";

// ─── Comments ─────────────────────────────────────────────────────────────────
// CommentController → @RequestMapping("/api")

/** GET /api/decisions/{decisionId}/comments */
export const getComments = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/comments`);

/** POST /api/decisions/{decisionId}/comments  body: { content } */
export const postComment = (decisionId, content) =>
  api.post(`/api/decisions/${decisionId}/comments`, { content });

/** POST /api/decisions/{decisionId}/comments/{parentCommentId}/replies  body: { content } */
export const postReply = (decisionId, parentCommentId, content) =>
  api.post(`/api/decisions/${decisionId}/comments/${parentCommentId}/replies`, { content });

/** PUT /api/comments/{commentId}  body: { content } */
export const updateComment = (commentId, content) =>
  api.put(`/api/comments/${commentId}`, { content });

/** DELETE /api/comments/{commentId} */
export const deleteComment = (commentId) =>
  api.delete(`/api/comments/${commentId}`);

/** GET /api/comments/{commentId}/replies */
export const getReplies = (commentId) =>
  api.get(`/api/comments/${commentId}/replies`);
