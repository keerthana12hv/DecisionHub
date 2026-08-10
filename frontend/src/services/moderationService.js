import api from "./api";

// ─── Decision moderation ──────────────────────────────────────────────────────
// CommunityModerationController → @RequestMapping("/api/moderation")

export const pinDecision     = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/pin`);
export const unpinDecision   = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/unpin`);
export const lockDiscussion  = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/lock`);
export const unlockDiscussion= (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/unlock`);

// ─── Comment moderation ───────────────────────────────────────────────────────

/** PUT /api/moderation/comments/{commentId}/pin */
export const pinComment   = (commentId) => api.put(`/api/moderation/comments/${commentId}/pin`);

/** PUT /api/moderation/comments/{commentId}/unpin */
export const unpinComment = (commentId) => api.put(`/api/moderation/comments/${commentId}/unpin`);

/** DELETE /api/moderation/comments/{commentId}  — moderator force-delete */
export const modDeleteComment = (commentId) => api.delete(`/api/moderation/comments/${commentId}`);

/** GET /api/moderation/decisions/{decisionId}/comments/pinned */
export const getPinnedComment = (decisionId) =>
  api.get(`/api/moderation/decisions/${decisionId}/comments/pinned`);

// ─── Report (stub — queues locally until backend endpoint is built) ───────────
export const reportContent = async (type, targetId, reason) => {
  try {
    await api.post(`/api/report`, { type, targetId, reason });
  } catch {
    const key = "dh-pending-reports";
    const list = JSON.parse(localStorage.getItem(key) ?? "[]");
    list.push({ type, targetId, reason, reportedAt: new Date().toISOString() });
    localStorage.setItem(key, JSON.stringify(list));
  }
};
