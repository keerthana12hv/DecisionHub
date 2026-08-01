import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/VotingInsights.css";
import {
  FaBroadcastTower, FaSync, FaVoteYea, FaClock,
  FaTrophy, FaUsers, FaChevronRight,
} from "react-icons/fa";
import api from "../services/api";
import { getScores, buildLiveCountsFromScores } from "../services/voteService";

const REFRESH_MS = 12000;

// ─── helpers ──────────────────────────────────────────────────────────────────

/** From a DecisionResponse, count unique participants (users who scored any option).
 *  For choice-based decisions, uses voteCount. For rating-based, uses comparisonScores. */
const participantCount = (options = []) => {
  // Choice-based: sum of voteCount fields
  const choiceTotal = (options ?? []).reduce((s, o) => s + (o.voteCount ?? 0), 0);
  if (choiceTotal > 0) return choiceTotal;
  // Rating-based: unique userIds across all comparisonScores
  const users = new Set();
  for (const opt of options) {
    for (const s of opt.comparisonScores ?? []) users.add(s.userId);
  }
  return users.size;
};

/** Total votes: choice voteCount + rating score entries */
const totalScoreEntries = (options = []) => {
  const choiceTotal = (options ?? []).reduce((s, o) => s + (o.voteCount ?? 0), 0);
  const ratingTotal = (options ?? []).reduce((s, o) => s + (o.comparisonScores?.length ?? 0), 0);
  return choiceTotal + ratingTotal;
};

/** Derive the leading option title + its participant count */
const leadingOption = (options = []) => {
  let best = null;
  let bestCount = -1;
  for (const opt of options) {
    const c = new Set((opt.comparisonScores ?? []).map((s) => s.userId)).size;
    if (c > bestCount) { bestCount = c; best = opt; }
  }
  return best ? { title: best.title, count: bestCount } : null;
};

// ─── MiniResultBar ────────────────────────────────────────────────────────────

function MiniResultBar({ option, maxCount, totalCount }) {
  // Choice-based: use voteCount. Rating-based: use unique userId count from comparisonScores.
  const count = option.voteCount > 0
    ? option.voteCount
    : new Set((option.comparisonScores ?? []).map((s) => s.userId)).size;
  const pct   = maxCount === 0 ? 0 : Math.round((count / maxCount) * 100);
  const share = totalCount === 0 ? 0 : Math.round((count / totalCount) * 100);

  return (
    <div className="vi-result-bar-item">
      <div className="vi-result-bar-labels">
        <span className="vi-result-option-name">{option.title}</span>
        <span className="vi-result-option-count">
          {count} <span className="vi-result-pct">({share}%)</span>
        </span>
      </div>
      <div className="vi-result-bar-track">
        <div
          className="vi-result-bar-fill"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

// ─── DecisionResultRow ────────────────────────────────────────────────────────

function DecisionResultRow({ decision, navigate }) {
  const options = decision.options ?? [];

  // Per-option counts: choice → voteCount, rating → unique raters
  const counts = options.map((o) =>
    o.voteCount > 0
      ? o.voteCount
      : new Set((o.comparisonScores ?? []).map((s) => s.userId)).size
  );
  const totalParticipants = counts.reduce((s, c) => s + c, 0);
  const maxCount = Math.max(...counts, 0);
  const isActive = decision.status === "ACTIVE";

  return (
    <div className="vi-decision-row">
      <div className="vi-decision-row__header">
        <div className="vi-decision-row__title-group">
          <span className={`vi-status-dot vi-status-dot--${isActive ? "active" : "closed"}`} />
          <span className="vi-decision-title">{decision.title}</span>
          <span className="vi-voting-type-tag">
            {decision.votingType?.replace("_", " ") ?? "—"}
          </span>
        </div>
        <div className="vi-decision-row__meta">
          <span className="vi-participant-chip">
            <FaUsers /> {totalParticipants}
          </span>
          <button
            className="vi-view-btn"
            onClick={() => navigate(`/decision/${decision.id}`)}
            title="View decision"
          >
            <FaChevronRight />
          </button>
        </div>
      </div>

      {options.length > 0 ? (
        <div className="vi-result-bars">
          {options.map((opt) => (
            <MiniResultBar
              key={opt.id}
              option={opt}
              maxCount={maxCount}
              totalCount={totalParticipants}
            />
          ))}
        </div>
      ) : (
        <p className="vi-no-options">No options configured.</p>
      )}
    </div>
  );
}

// ─── VotingInsights (main export) ─────────────────────────────────────────────

export default function VotingInsights() {
  const navigate = useNavigate();

  const [decisions, setDecisions]     = useState([]);
  const [loading, setLoading]         = useState(true);
  const [refreshing, setRefreshing]   = useState(false);
  const [lastRefresh, setLastRefresh] = useState(null);
  const [error, setError]             = useState(null);
  const intervalRef                   = useRef(null);

  const fetchData = useCallback(async (isBackground = false) => {
    if (isBackground) setRefreshing(true);
    else setLoading(true);
    setError(null);

    try {
      // Fetch all ACTIVE decisions — these are the live ones
      const res = await api.get("/api/decisions", { params: { status: "ACTIVE" } });
      setDecisions(res.data ?? []);
      setLastRefresh(new Date());
    } catch (err) {
      setError(err?.response?.data?.message ?? "Failed to load voting data.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  // Initial load + periodic refresh
  useEffect(() => {
    fetchData(false);
    intervalRef.current = setInterval(() => fetchData(true), REFRESH_MS);
    return () => clearInterval(intervalRef.current);
  }, [fetchData]);

  // ── derived summary stats ──────────────────────────────────────────────────
  const activeCount    = decisions.filter((d) => d.status === "ACTIVE").length;
  const totalParticips = decisions.reduce(
    (sum, d) => sum + participantCount(d.options), 0
  );
  const allOptions     = decisions.flatMap((d) => d.options ?? []);

  // Top option across all active decisions
  let topOpt = null;
  let topCount = -1;
  for (const opt of allOptions) {
    const c = new Set((opt.comparisonScores ?? []).map((s) => s.userId)).size;
    if (c > topCount) { topCount = c; topOpt = opt; }
  }

  const summaryCards = [
    {
      icon: <FaVoteYea />,
      label: "Live Scores",
      value: loading ? "…" : totalScoreEntries(allOptions),
      cls: "purple",
    },
    {
      icon: <FaClock />,
      label: "Active Polls",
      value: loading ? "…" : activeCount,
      cls: "blue",
    },
    {
      icon: <FaUsers />,
      label: "Participants",
      value: loading ? "…" : totalParticips,
      cls: "green",
    },
    {
      icon: <FaTrophy />,
      label: "Leading Option",
      value: loading ? "…" : (topOpt?.title ?? "—"),
      sub: topOpt && topCount > 0 ? `${topCount} voter${topCount !== 1 ? "s" : ""}` : null,
      cls: "yellow",
      long: true,
    },
  ];

  return (
    <div className="voting-insights glass-card">
      {/* ── Header ── */}
      <div className="comparison-header">
        <div>
          <h3>Voting UI &amp; Live Results</h3>
          <p>
            Live vote activity, participant counts, and leading options
            across all active decisions.
          </p>
        </div>
        <div className="vi-header-right">
          {!loading && (
            <span className="live-badge">
              <FaBroadcastTower className="live-dot-icon" /> LIVE
            </span>
          )}
          <button
            className="vp-refresh-btn"
            onClick={() => fetchData(true)}
            disabled={refreshing}
            title="Refresh now"
          >
            <FaSync className={refreshing ? "spin" : ""} />
            {lastRefresh && (
              <span>
                {lastRefresh.toLocaleTimeString([], {
                  hour: "2-digit", minute: "2-digit",
                })}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* ── Summary pills ── */}
      <div className="voting-summary-grid">
        {summaryCards.map((card) => (
          <div key={card.label} className={`summary-pill summary-pill--${card.cls}`}>
            <span className="summary-pill__icon">{card.icon}</span>
            <div className="summary-pill__body">
              <p>{card.label}</p>
              <h4 className={card.long ? "summary-pill__value--long" : ""}>
                {card.value}
              </h4>
              {card.sub && <span className="summary-pill__sub">{card.sub}</span>}
            </div>
          </div>
        ))}
      </div>

      {/* ── Per-decision result rows ── */}
      <div className="result-visualization">
        {loading ? (
          <div className="vi-skeleton-list">
            {[1, 2].map((n) => (
              <div key={n} className="vi-skeleton-row">
                <div className="vp-skeleton-title" />
                <div className="vp-skeleton-bar" />
                <div className="vp-skeleton-bar vp-skeleton-bar--short" />
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="comparison-empty">
            <span>{error}</span>
            <button className="btn-link" onClick={() => fetchData(false)}>
              Retry
            </button>
          </div>
        ) : decisions.length === 0 ? (
          <div className="comparison-empty">
            No active decisions to display.
          </div>
        ) : (
          decisions.map((d) => (
            <DecisionResultRow key={d.id} decision={d} navigate={navigate} />
          ))
        )}
      </div>
    </div>
  );
}
