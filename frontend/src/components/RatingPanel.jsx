import { useState, useEffect, useRef, useCallback } from "react";
import "../styles/RatingPanel.css";
import {
  FaBroadcastTower, FaSync, FaTrophy, FaMedal, FaChartBar,
  FaUsers, FaCheckCircle, FaExclamationCircle, FaInfoCircle,
} from "react-icons/fa";
import {
  getMyScores,
  getScores,
  submitScore,
  deleteScore,
  getRanking,
  buildLiveCountsFromScores,
} from "../services/voteService";
import { useToast } from "./Toast";

// How often (ms) to refresh live participant counts while poll is OPEN
const LIVE_REFRESH_MS = 10000;

// How long (ms) to wait after slider stops moving before auto-saving
const DEBOUNCE_SAVE_MS = 600;

// ─── helpers ─────────────────────────────────────────────────────────────────

const rankMedal = (rank) => {
  if (rank === 1) return { icon: <FaTrophy />, cls: "rank-gold" };
  if (rank === 2) return { icon: <FaMedal />,  cls: "rank-silver" };
  if (rank === 3) return { icon: <FaMedal />,  cls: "rank-bronze" };
  return { icon: <span className="rank-number">#{rank}</span>, cls: "rank-default" };
};

const pct = (value, max) => (max === 0 ? 0 : Math.round((value / max) * 100));

// ─── ScoreCell ────────────────────────────────────────────────────────────────
// One slider per (option × factor) cell.  Debounces saves so the backend
// isn't hammered while the user drags.

function ScoreCell({ decisionId, optionId, factorId, initialValue, pollOpen, onSaved }) {
  const { addToast } = useToast();
  const [value, setValue]       = useState(initialValue ?? 50);
  const [status, setStatus]     = useState("idle"); // idle | saving | saved | error
  const debounceRef             = useRef(null);
  const latestValue             = useRef(value);

  // Sync if parent refreshes initial values (e.g. after full reload)
  useEffect(() => {
    if (initialValue != null) setValue(initialValue);
  }, [initialValue]);

  const persist = useCallback(
    async (v) => {
      setStatus("saving");
      try {
        await submitScore(decisionId, optionId, factorId, v);
        setStatus("saved");
        onSaved?.();
        setTimeout(() => setStatus("idle"), 1800);
      } catch (err) {
        setStatus("error");
        addToast(
          err?.response?.data?.message ?? "Failed to save score.",
          "error"
        );
        setTimeout(() => setStatus("idle"), 3000);
      }
    },
    [decisionId, optionId, factorId, onSaved, addToast]
  );

  const handleChange = (e) => {
    const v = Number(e.target.value);
    setValue(v);
    latestValue.current = v;

    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      persist(latestValue.current);
    }, DEBOUNCE_SAVE_MS);
  };

  // Cleanup on unmount
  useEffect(() => () => clearTimeout(debounceRef.current), []);

  const statusIcon = {
    saving: <span className="cell-status saving" title="Saving…">●</span>,
    saved:  <span className="cell-status saved"  title="Saved"><FaCheckCircle /></span>,
    error:  <span className="cell-status error"  title="Save failed"><FaExclamationCircle /></span>,
  }[status];

  return (
    <div className="score-cell">
      <div className="score-cell__value">
        <span className="score-number">{value}</span>
        {statusIcon}
      </div>
      <input
        type="range"
        min={0}
        max={100}
        step={1}
        value={value}
        disabled={!pollOpen}
        onChange={handleChange}
        className="score-slider"
        aria-label={`Score ${value}`}
      />
      <div className="score-cell__track-labels">
        <span>0</span>
        <span>100</span>
      </div>
    </div>
  );
}

// ─── RankingChart ─────────────────────────────────────────────────────────────
// Horizontal bar chart rendered from a RankingResponse.

function RankingChart({ ranking, factors }) {
  if (!ranking?.options?.length) return null;

  const maxScore = Math.max(...ranking.options.map((o) => o.score), 0.001);

  return (
    <div className="ranking-chart">
      <div className="ranking-chart__header">
        <FaChartBar />
        <h4>Live Ranking</h4>
        <span className="ranking-chart__ts">
          {new Date(ranking.rankingTimestamp).toLocaleTimeString([], {
            hour: "2-digit", minute: "2-digit",
          })}
        </span>
      </div>

      <div className="ranking-chart__bars">
        {ranking.options.map((opt) => {
          const { icon, cls } = rankMedal(opt.rank);
          const barWidth = pct(opt.score, maxScore);

          return (
            <div key={opt.optionId} className={`ranking-bar-row ${opt.isTied ? "is-tied" : ""}`}>
              {/* Rank medal */}
              <div className={`ranking-bar-rank ${cls}`}>{icon}</div>

              {/* Label + bar */}
              <div className="ranking-bar-body">
                <div className="ranking-bar-labels">
                  <span className="ranking-bar-name">
                    {opt.optionTitle}
                    {opt.isTied && <span className="tied-tag"> (tied)</span>}
                  </span>
                  <span className="ranking-bar-score">{opt.score.toFixed(1)}</span>
                </div>
                <div className="ranking-bar-track">
                  <div
                    className={`ranking-bar-fill ${cls}`}
                    style={{ width: `${barWidth}%` }}
                  />
                </div>

                {/* Factor breakdown */}
                {opt.factorBreakdown?.length > 0 && (
                  <div className="ranking-factor-pills">
                    {opt.factorBreakdown.map((f) => (
                      <span key={f.factorId} className="factor-pill" title={f.factorName}>
                        {f.factorName}: <strong>{f.averageScore.toFixed(1)}</strong>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ─── ParticipantCounter ───────────────────────────────────────────────────────

function ParticipantCounter({ decisionId, pollOpen, onCountsReady }) {
  const [participantCount, setParticipantCount] = useState(0);
  const [liveCounts, setLiveCounts]             = useState({});
  const [refreshing, setRefreshing]             = useState(false);
  const [lastRefresh, setLastRefresh]           = useState(null);
  const intervalRef                             = useRef(null);

  const refresh = useCallback(async () => {
    setRefreshing(true);
    try {
      const res  = await getScores(decisionId);
      const scores = res.data ?? [];
      const counts = buildLiveCountsFromScores(scores);
      setLiveCounts(counts);
      onCountsReady?.(counts);

      // unique participant count = unique userIds in the scores list
      const uniqueUsers = new Set(scores.map((s) => s.userId));
      setParticipantCount(uniqueUsers.size);
      setLastRefresh(new Date());
    } catch {
      // silent
    } finally {
      setRefreshing(false);
    }
  }, [decisionId, onCountsReady]);

  useEffect(() => {
    refresh();
    if (pollOpen) {
      intervalRef.current = setInterval(refresh, LIVE_REFRESH_MS);
    }
    return () => clearInterval(intervalRef.current);
  }, [pollOpen, refresh]);

  return (
    <div className="participant-counter">
      <div className="participant-counter__stat">
        <FaUsers />
        <span>
          <strong>{participantCount}</strong> participant{participantCount !== 1 ? "s" : ""}
        </span>
      </div>
      {pollOpen && (
        <>
          <span className="live-badge live-badge--sm">
            <FaBroadcastTower className="live-dot-icon" /> LIVE
          </span>
          <button
            className="vp-refresh-btn"
            onClick={refresh}
            title="Refresh now"
            disabled={refreshing}
          >
            <FaSync className={refreshing ? "spin" : ""} />
            {lastRefresh && (
              <span>
                {lastRefresh.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
              </span>
            )}
          </button>
        </>
      )}
    </div>
  );
}

// ─── RatingPanel (main export) ────────────────────────────────────────────────

export default function RatingPanel({ decision, pollOpen }) {
  const { addToast } = useToast();

  // my saved scores: key = "optionId-factorId" → value (0-100)
  const [myScores, setMyScores]   = useState({});
  const [ranking, setRanking]     = useState(null);
  const [rankLoading, setRankLoading] = useState(false);
  const [rankError, setRankError] = useState(null);
  const [liveCounts, setLiveCounts]   = useState({});

  const options = decision.options       ?? [];
  const factors = decision.comparisonFactors ?? [];  // ComparisonFactorResponse[]

  // ── Load my existing scores on mount ────────────────────────────────────
  useEffect(() => {
    if (!decision?.id) return;

    getMyScores(decision.id)
      .then((res) => {
        const map = {};
        for (const s of res.data ?? []) {
          map[`${s.optionId}-${s.factorId}`] = s.score;
        }
        setMyScores(map);
      })
      .catch(() => {
        // no scores yet — fine, sliders will default to 50
      });
  }, [decision.id]);

  // ── Fetch / refresh ranking ──────────────────────────────────────────────
  const fetchRanking = useCallback(async () => {
    if (!decision?.id) return;
    setRankLoading(true);
    setRankError(null);
    try {
      const res = await getRanking(decision.id);
      setRanking(res.data);
    } catch (err) {
      const msg = err?.response?.data?.message ?? "Ranking not available yet.";
      setRankError(msg);
    } finally {
      setRankLoading(false);
    }
  }, [decision.id]);

  // Fetch ranking on mount and whenever poll is closed (results become final)
  useEffect(() => {
    fetchRanking();
  }, [fetchRanking]);

  // Auto-refresh ranking periodically while poll is open
  useEffect(() => {
    if (!pollOpen) return;
    const id = setInterval(fetchRanking, LIVE_REFRESH_MS);
    return () => clearInterval(id);
  }, [pollOpen, fetchRanking]);

  // ── Delete a score ───────────────────────────────────────────────────────
  const handleDelete = async (optionId, factorId) => {
    const key = `${optionId}-${factorId}`;
    if (myScores[key] == null) return;
    try {
      await deleteScore(decision.id, optionId, factorId);
      setMyScores((prev) => {
        const next = { ...prev };
        delete next[key];
        return next;
      });
      addToast("Score removed.", "success");
      fetchRanking();
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to remove score.", "error");
    }
  };

  if (!options.length || !factors.length) {
    return (
      <div className="rating-panel">
        <div className="rating-empty">
          <FaInfoCircle />
          <p>This decision has no options or factors configured yet.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="rating-panel">

      {/* ── Live participant strip ── */}
      <ParticipantCounter
        decisionId={decision.id}
        pollOpen={pollOpen}
        onCountsReady={setLiveCounts}
      />

      {/* ── Scoring matrix ── */}
      <div className="rating-section">
        <div className="rating-section__header">
          <h3>Score each option</h3>
          {!pollOpen && (
            <span className="poll-closed-tag">
              <FaCheckCircle /> Voting closed — results below
            </span>
          )}
        </div>

        <div className="rating-matrix-wrapper">
          <table className="rating-matrix">
            <thead>
              <tr>
                <th className="matrix-option-col">Option</th>
                {factors.map((f) => (
                  <th key={f.id} className="matrix-factor-col" title={f.description}>
                    <span>{f.name}</span>
                    {f.weight && f.weight !== 1 && (
                      <span className="factor-weight-tag">×{f.weight}</span>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {options.map((opt) => {
                const participantCount = liveCounts[opt.id] ?? 0;
                return (
                  <tr key={opt.id}>
                    <td className="matrix-option-label">
                      <span className="option-title">{opt.title}</span>
                      {pollOpen && (
                        <span className="matrix-participant-pill">
                          <FaUsers /> {participantCount} rated
                        </span>
                      )}
                    </td>
                    {factors.map((factor) => {
                      const key = `${opt.id}-${factor.id}`;
                      return (
                        <td key={factor.id} className="matrix-score-cell">
                          <ScoreCell
                            decisionId={decision.id}
                            optionId={opt.id}
                            factorId={factor.id}
                            initialValue={myScores[key]}
                            pollOpen={pollOpen}
                            onSaved={() => {
                              setMyScores((prev) => ({ ...prev })); // trigger re-render
                              fetchRanking();
                            }}
                          />
                          {myScores[key] != null && pollOpen && (
                            <button
                              className="score-delete-btn"
                              onClick={() => handleDelete(opt.id, factor.id)}
                              title="Remove my score"
                            >
                              ×
                            </button>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* ── Ranking / Results visualization ── */}
      <div className="ranking-section">
        <div className="ranking-section__header">
          <h3>{pollOpen ? "Live Ranking" : "Final Results"}</h3>
          {pollOpen && (
            <button
              className="vp-refresh-btn"
              onClick={fetchRanking}
              disabled={rankLoading}
            >
              <FaSync className={rankLoading ? "spin" : ""} />
              Refresh ranking
            </button>
          )}
        </div>

        {rankLoading && !ranking ? (
          <div className="ranking-loading">
            <FaSync className="spin" /> Computing ranking…
          </div>
        ) : rankError ? (
          <div className="ranking-error">
            <FaInfoCircle />
            <span>{rankError}</span>
            <button className="btn-link" onClick={fetchRanking}>Retry</button>
          </div>
        ) : ranking ? (
          <RankingChart ranking={ranking} factors={factors} />
        ) : null}
      </div>
    </div>
  );
}
