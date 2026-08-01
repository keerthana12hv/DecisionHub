import api from "./api";

// ─── Poll ────────────────────────────────────────────────────────────────────

/** GET /api/decisions/{decisionId}/poll */
export const getPoll = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/poll`);

/** POST /api/decisions/{decisionId}/poll/close */
export const closePoll = (decisionId) =>
  api.post(`/api/decisions/${decisionId}/poll/close`);

/** PATCH /api/decisions/{decisionId}/poll/end-time */
export const extendPollEndTime = (decisionId, endTime) =>
  api.patch(`/api/decisions/${decisionId}/poll/end-time`, { endTime });

// ─── Votes (SINGLE_CHOICE / MULTIPLE_CHOICE) ─────────────────────────────────

/**
 * PUT /api/decisions/{decisionId}/votes
 * optionIds: Long[] — the user's complete desired selection.
 * Pass [] to clear the vote.
 */
export const submitVote = (decisionId, optionIds) =>
  api.put(`/api/decisions/${decisionId}/votes`, { optionIds });

/** GET /api/decisions/{decisionId}/votes/me */
export const getMyVote = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/votes/me`);

// ─── Comparison Scores (RATING_BASED) ────────────────────────────────────────

/**
 * GET /api/decisions/{decisionId}/scores
 * Returns all scores for every participant — used to compute live tallies.
 */
export const getScores = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/scores`);

/** GET /api/decisions/{decisionId}/scores/me */
export const getMyScores = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/scores/me`);

/**
 * POST /api/decisions/{decisionId}/scores
 * { optionId, factorId, score (0-100), remarks? }
 */
export const submitScore = (decisionId, optionId, factorId, score, remarks = "") =>
  api.post(`/api/decisions/${decisionId}/scores`, {
    optionId,
    factorId,
    score,
    remarks,
  });

/**
 * DELETE /api/decisions/{decisionId}/scores/{optionId}/{factorId}
 */
export const deleteScore = (decisionId, optionId, factorId) =>
  api.delete(`/api/decisions/${decisionId}/scores/${optionId}/${factorId}`);

// ─── Ranking ──────────────────────────────────────────────────────────────────

/**
 * GET /api/decisions/{decisionId}/ranking
 * Full ranking with per-factor breakdowns.
 * Response: { decisionId, decisionTitle, status, rankingTimestamp, options: OptionRankingDto[] }
 * OptionRankingDto: { optionId, optionTitle, rank, score, factorBreakdown: FactorScoreDto[], isTied }
 */
export const getRanking = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/ranking`);

/**
 * GET /api/decisions/{decisionId}/ranking/summary
 * Condensed ranking without factor breakdowns.
 */
export const getRankingSummary = (decisionId) =>
  api.get(`/api/decisions/${decisionId}/ranking/summary`);

// ─── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Given the flat list of ComparisonScoreResponse objects returned by
 * GET /scores, compute a per-option vote-count map:
 *   { [optionId]: Set<userId> }  →  { [optionId]: number }
 *
 * We count unique users per option (a user may submit multiple
 * factor scores for the same option — we count them once).
 */
export const buildLiveCountsFromScores = (scores = []) => {
  const usersPerOption = {};
  for (const s of scores) {
    if (!usersPerOption[s.optionId]) usersPerOption[s.optionId] = new Set();
    usersPerOption[s.optionId].add(s.userId);
  }
  const counts = {};
  for (const [optionId, userSet] of Object.entries(usersPerOption)) {
    counts[Number(optionId)] = userSet.size;
  }
  return counts;
};

/**
 * Given the flat list of Vote objects (from VoteResponse.optionIds or a
 * synthesised list), count votes per option across all polled users.
 *
 * voteRecords: Array<{ userId, optionIds: Long[] }>
 */
export const buildLiveCountsFromVotes = (voteRecords = []) => {
  const counts = {};
  for (const record of voteRecords) {
    for (const id of record.optionIds ?? []) {
      counts[id] = (counts[id] ?? 0) + 1;
    }
  }
  return counts;
};
