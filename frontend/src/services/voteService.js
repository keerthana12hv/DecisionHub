import api from "./api";

export const getScores = (decisionId) =>
    api.get(`/decisions/${decisionId}/scores`);

export const getMyScores = (decisionId) =>
    api.get(`/decisions/${decisionId}/scores/me`);

export const submitScore = (
    decisionId,
    optionId,
    factorId,
    score,
    remarks = ""
) =>
    api.post(
        `/decisions/${decisionId}/scores`,
        { optionId, factorId, score, remarks }
    );

export const deleteScore = (decisionId, optionId, factorId) =>
    api.delete(
        `/decisions/${decisionId}/scores/${optionId}/${factorId}`
    );

export const getRanking = (decisionId) =>
    api.get(`/decisions/${decisionId}/ranking`);

export const getRankingSummary = (decisionId) =>
    api.get(`/decisions/${decisionId}/ranking/summary`);