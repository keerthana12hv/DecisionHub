import api from "./api";

// ─── Decision moderation (CommunityModerationController) ─────────────────────
// PUT /api/moderation/decisions/{id}/pin|unpin|lock|unlock

export const pinDecision   = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/pin`);
export const unpinDecision = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/unpin`);
export const lockDiscussion   = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/lock`);
export const unlockDiscussion = (decisionId) => api.put(`/api/moderation/decisions/${decisionId}/unlock`);

// ─── Comment moderation ───────────────────────────────────────────────────────
// CommentController is a stub — these will be wired once backend endpoints exist.
// Until then they resolve locally so UI doesn't break.

export const pinComment = (commentId) =>
  api.put(`/api/comments/${commentId}/pin`).catch(() => ({ data: null, _local: true }));

export const unpinComment = (commentId) =>
  api.put(`/api/comments/${commentId}/unpin`).catch(() => ({ data: null, _local: true }));

export const hideComment = (commentId) =>
  api.put(`/api/comments/${commentId}/hide`).catch(() => ({ data: null, _local: true }));

export const unhideComment = (commentId) =>
  api.put(`/api/comments/${commentId}/unhide`).catch(() => ({ data: null, _local: true }));

// ─── Report ───────────────────────────────────────────────────────────────────
// ReportController is a stub — stores report locally until backend is ready.

export const reportContent = (type, targetId, reason) =>
  api.post(`/api/report`, { type, targetId, reason }).catch(() => {
    // Persist locally so report isn't lost
    const key = "decisionhub-pending-reports";
    const existing = JSON.parse(localStorage.getItem(key) ?? "[]");
    existing.push({ type, targetId, reason, reportedAt: new Date().toISOString() });
    localStorage.setItem(key, JSON.stringify(existing));
    return { data: null, _local: true };
  });
