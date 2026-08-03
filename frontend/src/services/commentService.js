import api from "./api";

const BASE = "/api/comments";

export const getComments  = (decisionId) => api.get(`${BASE}/decision/${decisionId}`);
export const postComment  = (decisionId, content) => api.post(BASE, { decisionId, content });
export const postReply    = (commentId, content)  => api.post(`${BASE}/${commentId}/reply`, { content });
export const deleteComment = (commentId)           => api.delete(`${BASE}/${commentId}`);

// ── Stubs — wired to future backend endpoints ────────────────────────────────
export const pinComment   = (commentId) => api.put(`${BASE}/${commentId}/pin`).catch(() => ({ _local: true }));
export const unpinComment = (commentId) => api.put(`${BASE}/${commentId}/unpin`).catch(() => ({ _local: true }));
export const hideComment  = (commentId) => api.put(`${BASE}/${commentId}/hide`).catch(() => ({ _local: true }));
