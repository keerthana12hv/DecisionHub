import axios from "axios";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: {
    Authorization: `Bearer ${token()}`,
    "Content-Type": "application/json"
  }
});

export const getPoll = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/poll`, headers());

export const getMyVote = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/votes/me`, headers());

export const submitVote = (decisionId, optionIds) =>
  axios.put(
    `${API}/decisions/${decisionId}/votes`,
    { optionIds },
    headers()
  );

export const getScores = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/scores`, headers());

export const getMyScores = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/scores/me`, headers());

export const submitScore = (decisionId, optionId, factorId, score, remarks = "") =>
  axios.post(
    `${API}/decisions/${decisionId}/scores`,
    { optionId, factorId, score, remarks },
    headers()
  );

export const deleteScore = (decisionId, optionId, factorId) =>
  axios.delete(`${API}/decisions/${decisionId}/scores/${optionId}/${factorId}`, headers());

export const getRanking = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/ranking`, headers());

export const getRankingSummary = (decisionId) =>
  axios.get(`${API}/decisions/${decisionId}/ranking/summary`, headers());