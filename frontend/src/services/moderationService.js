import api from "./api";

export const pinDecision = (decisionId) =>
  api.put(`/api/moderation/decisions/${decisionId}/pin`);

export const unpinDecision = (decisionId) =>
  api.put(`/api/moderation/decisions/${decisionId}/unpin`);

export const lockDecision = (decisionId) =>
  api.put(`/api/moderation/decisions/${decisionId}/lock`);

export const unlockDecision = (decisionId) =>
  api.put(`/api/moderation/decisions/${decisionId}/unlock`);