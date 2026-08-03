import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  FaArrowLeft, FaBroadcastTower, FaLock, FaUsers,
  FaCalendarAlt, FaCrown, FaSync, FaCheckCircle,
<<<<<<< Updated upstream
=======
  FaThumbtack, FaShieldAlt,
>>>>>>> Stashed changes
} from "react-icons/fa";
import api from "../services/api";
import { getPoll, submitVote, getMyVote } from "../services/voteService";
import RatingPanel from "../components/RatingPanel";
<<<<<<< Updated upstream
import { useToast } from "../components/Toast";
import "../styles/DecisionDetail.css";
=======
import ModeratorPanel from "../components/ModeratorPanel";
import Discussion from "./Discussion";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import "../styles/DecisionDetail.css";
import "../styles/ModeratorPanel.css";
>>>>>>> Stashed changes

// ─── helpers ──────────────────────────────────────────────────────────────────

const formatDate = (iso) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleString(undefined, {
    year: "numeric", month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
};

const countVotes = (options = []) => {
  const map = {};
  for (const opt of options) {
<<<<<<< Updated upstream
    // voteCount = choice-based votes from votes table (populated by backend)
    // comparisonScores = rating-based participant count fallback
=======
>>>>>>> Stashed changes
    map[opt.id] = opt.voteCount ?? new Set((opt.comparisonScores ?? []).map((s) => s.userId)).size;
  }
  return map;
};

const total = (map) => Object.values(map).reduce((s, v) => s + v, 0);
<<<<<<< Updated upstream
=======

const leaderId = (map) => {
  let best = null, bestVal = -1;
  for (const [id, v] of Object.entries(map)) {
    if (v > bestVal) { bestVal = v; best = Number(id); }
  }
  return bestVal > 0 ? best : null;
};

// ─── ChoicePanel ──────────────────────────────────────────────────────────────

function ChoicePanel({ decision, poll }) {
  const { addToast } = useToast();
  const [myOptionIds, setMyOptionIds]   = useState([]);
  const [voteCounts, setVoteCounts]     = useState(() => countVotes(decision.options));
  const [submitting, setSubmitting]     = useState(false);
  const [justVoted, setJustVoted]       = useState(false);

  const pollOpen    = poll?.status === "OPEN";
  const isSingle    = decision.votingType === "SINGLE_CHOICE";
  const showResults = myOptionIds.length > 0 || !pollOpen;
  const totalVotes  = total(voteCounts);
  const winId       = leaderId(voteCounts);
>>>>>>> Stashed changes

const leaderId = (map) => {
  let best = null, bestVal = -1;
  for (const [id, v] of Object.entries(map)) {
    if (v > bestVal) { bestVal = v; best = Number(id); }
  }
  return bestVal > 0 ? best : null;
};

// ─── ChoicePanel ──────────────────────────────────────────────────────────────
// Handles SINGLE_CHOICE and MULTIPLE_CHOICE voting inline on the detail page.

function ChoicePanel({ decision, poll }) {
  const { addToast } = useToast();
  const [myOptionIds, setMyOptionIds]   = useState([]);
  const [voteCounts, setVoteCounts]     = useState(() => countVotes(decision.options));
  const [submitting, setSubmitting]     = useState(false);
  const [justVoted, setJustVoted]       = useState(false);

  const pollOpen      = poll?.status === "OPEN";
  const isSingle      = decision.votingType === "SINGLE_CHOICE";
  const showResults   = myOptionIds.length > 0 || !pollOpen;
  const totalVotes    = total(voteCounts);
  const winId         = leaderId(voteCounts);

  // Load my existing vote
  useEffect(() => {
    if (!decision?.id) return;
    getMyVote(decision.id)
      .then((r) => setMyOptionIds(r.data?.optionIds ?? []))
      .catch(() => {});
  }, [decision.id]);

  const handleSelect = async (optionId) => {
    if (!pollOpen || submitting) return;
<<<<<<< Updated upstream

    let nextIds;
    if (isSingle) {
      nextIds = myOptionIds.includes(optionId) ? [] : [optionId];
    } else {
      nextIds = myOptionIds.includes(optionId)
        ? myOptionIds.filter((id) => id !== optionId)
        : [...myOptionIds, optionId];
    }

    const prevIds    = myOptionIds;
    const prevCounts = { ...voteCounts };
    setMyOptionIds(nextIds);

    // Optimistic count delta
=======
    let nextIds = isSingle
      ? (myOptionIds.includes(optionId) ? [] : [optionId])
      : (myOptionIds.includes(optionId)
          ? myOptionIds.filter((id) => id !== optionId)
          : [...myOptionIds, optionId]);

    const prevIds = myOptionIds;
    const prevCounts = { ...voteCounts };
    setMyOptionIds(nextIds);

>>>>>>> Stashed changes
    const delta = {};
    for (const id of prevIds) delta[id] = (delta[id] ?? 0) - 1;
    for (const id of nextIds) delta[id] = (delta[id] ?? 0) + 1;
    setVoteCounts((prev) => {
      const next = { ...prev };
      for (const [id, d] of Object.entries(delta)) {
        next[Number(id)] = Math.max(0, (next[Number(id)] ?? 0) + d);
      }
      return next;
    });

    setSubmitting(true);
    try {
      await submitVote(decision.id, nextIds);
      setJustVoted(nextIds.length > 0);
      addToast(nextIds.length > 0 ? "Vote recorded!" : "Vote removed.", "success");
      if (nextIds.length > 0) setTimeout(() => setJustVoted(false), 3000);
    } catch (err) {
      setMyOptionIds(prevIds);
      setVoteCounts(prevCounts);
      addToast(err?.response?.data?.message ?? "Vote failed.", "error");
    } finally {
      setSubmitting(false);
    }
  };

<<<<<<< Updated upstream
  return (
    <div className="choice-panel">
      <div className="choice-panel__header">
        <h3>{pollOpen ? "Cast your vote" : "Voting closed"}</h3>
        {!pollOpen && <span className="poll-closed-tag"><FaLock /> Results are final</span>}
        {pollOpen && isSingle && (
          <span className="choice-hint">Select one option</span>
        )}
        {pollOpen && !isSingle && (
          <span className="choice-hint">Select all that apply</span>
        )}
      </div>

      <div className="choice-options">
        {decision.options?.map((opt) => {
          const count    = voteCounts[opt.id] ?? 0;
          const pct      = totalVotes === 0 ? 0 : Math.round((count / totalVotes) * 100);
          const selected = myOptionIds.includes(opt.id);
          const leading  = opt.id === winId;

          return (
            <div
              key={opt.id}
              className={[
                "choice-option",
                selected ? "choice-option--selected" : "",
                leading && showResults ? "choice-option--leading" : "",
                !pollOpen ? "choice-option--disabled" : "",
              ].filter(Boolean).join(" ")}
              onClick={() => handleSelect(opt.id)}
              role={pollOpen ? "button" : undefined}
              tabIndex={pollOpen ? 0 : undefined}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") handleSelect(opt.id);
              }}
            >
              <div className="choice-option__row">
                <span className={`choice-selector${isSingle ? " radio" : " checkbox"}${selected ? " checked" : ""}`}>
                  {selected && <FaCheckCircle />}
                </span>
                <div className="choice-option__content">
                  <div className="choice-option__top">
                    <span className="choice-option__name">
                      {opt.title}
                      {selected && <span className="your-vote-tag"> · Your vote</span>}
                    </span>
                    {showResults && (
                      <span className="choice-option__count">
                        <strong>{count}</strong>
                        <span className="option-count-pct"> {pct}%</span>
                      </span>
                    )}
                  </div>
                  {showResults && (
                    <div className="result-bar-track">
                      <div
                        className={`result-bar-fill${leading ? " result-bar-fill--leader" : ""}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  )}
                  {leading && showResults && count > 0 && (
                    <span className="winner-badge-lbl"><FaCrown /> Leading</span>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="choice-panel__footer">
        <span>Total votes: <strong>{totalVotes}</strong></span>
        {justVoted && (
          <span className="vote-confirmed">
            <FaCheckCircle /> Vote confirmed
          </span>
        )}
      </div>
    </div>
  );
}

// ─── DecisionDetail ───────────────────────────────────────────────────────────

export default function DecisionDetail() {
  const { decisionId } = useParams();
  const navigate       = useNavigate();
  const { addToast }   = useToast();

  const [decision, setDecision] = useState(null);
  const [poll, setPoll]         = useState(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState(null);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [decRes, pollRes] = await Promise.all([
        api.get(`/api/decisions/${decisionId}`),
        api.get(`/api/decisions/${decisionId}/poll`).catch(() => null),
      ]);
      setDecision(decRes.data);
      setPoll(pollRes?.data ?? null);
    } catch (err) {
      const msg = err?.response?.data?.message ?? "Could not load this decision.";
      setError(msg);
      addToast(msg, "error");
    } finally {
      setLoading(false);
    }
  }, [decisionId, addToast]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  if (loading) {
    return (
      <div className="dd-page">
        <div className="dd-loading">
          <FaSync className="spin" /> Loading decision…
        </div>
      </div>
    );
  }

  if (error || !decision) {
    return (
      <div className="dd-page">
        <div className="dd-error">
          <p>{error ?? "Decision not found."}</p>
          <button className="btn-primary" onClick={() => navigate("/decisions")}>
            Back to decisions
          </button>
        </div>
      </div>
    );
  }

  const pollOpen   = poll?.status === "OPEN";
  const pollClosed = poll && !pollOpen;
  const isRating   = decision.votingType === "RATING_BASED";

  return (
    <div className="dd-page">
      {/* ── Back nav ── */}
      <button className="dd-back-btn" onClick={() => navigate(-1)}>
        <FaArrowLeft /> Back
      </button>

      {/* ── Decision header card ── */}
      <div className="dd-header glass-panel">
        <div className="dd-header__top">
          <div className="dd-header__badges">
            {decision.communityName && (
              <span className="category-tag">{decision.communityName}</span>
            )}
            <span className={`voting-type-tag voting-type-tag--${decision.votingType?.toLowerCase()}`}>
              {decision.votingType?.replace("_", " ") ?? "—"}
            </span>
            {pollOpen && (
              <span className="live-badge">
                <FaBroadcastTower className="live-dot-icon" /> LIVE
              </span>
            )}
            {pollClosed && (
              <span className="closed-badge"><FaLock /> Closed</span>
            )}
          </div>
          <button
            className="vp-refresh-btn"
            onClick={fetchAll}
            disabled={loading}
            title="Refresh"
          >
            <FaSync className={loading ? "spin" : ""} />
          </button>
        </div>

        <h1 className="dd-title">{decision.title}</h1>
        {decision.description && (
          <p className="dd-description">{decision.description}</p>
        )}

        <div className="dd-meta-row">
          {decision.creator && (
            <span className="dd-meta-chip">
              <FaUsers /> {decision.creator.username ?? decision.creator.email}
            </span>
          )}
          {decision.deadline && (
            <span className="dd-meta-chip">
              <FaCalendarAlt /> Deadline: {formatDate(decision.deadline)}
            </span>
          )}
          {poll?.endTime && (
            <span className="dd-meta-chip">
              <FaBroadcastTower />
              {pollOpen ? "Voting closes: " : "Voting closed: "}
              {formatDate(poll.endTime)}
            </span>
          )}
        </div>
      </div>

      {/* ── Voting panel ── */}
      {!poll ? (
        <div className="dd-no-poll glass-panel">
          <p>This decision hasn't been published for voting yet.</p>
        </div>
      ) : isRating ? (
        <div className="dd-voting-section">
          <RatingPanel decision={decision} pollOpen={pollOpen} />
        </div>
      ) : (
        <div className="dd-voting-section">
          <ChoicePanel decision={decision} poll={poll} />
        </div>
      )}
    </div>
  );
}
=======
  return (
    <div className="choice-panel">
      <div className="choice-panel__header">
        <h3>{pollOpen ? "Cast your vote" : "Voting closed"}</h3>
        {!pollOpen && <span className="poll-closed-tag"><FaLock /> Results are final</span>}
        {pollOpen && <span className="choice-hint">{isSingle ? "Select one option" : "Select all that apply"}</span>}
      </div>

      <div className="choice-options">
        {decision.options?.map((opt) => {
          const count    = voteCounts[opt.id] ?? 0;
          const pct      = totalVotes === 0 ? 0 : Math.round((count / totalVotes) * 100);
          const selected = myOptionIds.includes(opt.id);
          const leading  = opt.id === winId;
          return (
            <div
              key={opt.id}
              className={[
                "choice-option",
                selected ? "choice-option--selected" : "",
                leading && showResults ? "choice-option--leading" : "",
                !pollOpen ? "choice-option--disabled" : "",
              ].filter(Boolean).join(" ")}
              onClick={() => handleSelect(opt.id)}
              role={pollOpen ? "button" : undefined}
              tabIndex={pollOpen ? 0 : undefined}
              onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") handleSelect(opt.id); }}
            >
              <div className="choice-option__row">
                <span className={`choice-selector${isSingle ? " radio" : " checkbox"}${selected ? " checked" : ""}`}>
                  {selected && <FaCheckCircle />}
                </span>
                <div className="choice-option__content">
                  <div className="choice-option__top">
                    <span className="choice-option__name">
                      {opt.title}
                      {selected && <span className="your-vote-tag"> · Your vote</span>}
                    </span>
                    {showResults && (
                      <span className="choice-option__count">
                        <strong>{count}</strong>
                        <span className="option-count-pct"> {pct}%</span>
                      </span>
                    )}
                  </div>
                  {showResults && (
                    <div className="result-bar-track">
                      <div className={`result-bar-fill${leading ? " result-bar-fill--leader" : ""}`}
                        style={{ width: `${pct}%` }} />
                    </div>
                  )}
                  {leading && showResults && count > 0 && (
                    <span className="winner-badge-lbl"><FaCrown /> Leading</span>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="choice-panel__footer">
        <span>Total votes: <strong>{totalVotes}</strong></span>
        {justVoted && (
          <span className="vote-confirmed"><FaCheckCircle /> Vote confirmed</span>
        )}
      </div>
    </div>
  );
}

// ─── DecisionDetail ───────────────────────────────────────────────────────────

export default function DecisionDetail() {
  const { decisionId } = useParams();
  const navigate       = useNavigate();
  const { user }       = useAuth();
  const { addToast }   = useToast();

  const [decision, setDecision] = useState(null);
  const [poll,     setPoll]     = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  const isMod = user?.role === "ADMIN" || user?.role === "MODERATOR";

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [decRes, pollRes] = await Promise.all([
        api.get(`/api/decisions/${decisionId}`),
        api.get(`/api/decisions/${decisionId}/poll`).catch(() => null),
      ]);
      setDecision(decRes.data);
      setPoll(pollRes?.data ?? null);
    } catch (err) {
      const msg = err?.response?.data?.message ?? "Could not load this decision.";
      setError(msg);
      addToast(msg, "error");
    } finally {
      setLoading(false);
    }
  }, [decisionId, addToast]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  // Called by ModeratorPanel when pin/lock actions succeed
  const handleDecisionUpdate = (updated) => {
    if (updated) setDecision(updated);
    else fetchAll(); // fallback: re-fetch
  };

  if (loading) {
    return (
      <div className="dd-page">
        <div className="dd-loading"><FaSync className="spin" /> Loading decision…</div>
      </div>
    );
  }

  if (error || !decision) {
    return (
      <div className="dd-page">
        <div className="dd-error">
          <p>{error ?? "Decision not found."}</p>
          <button className="btn-primary" onClick={() => navigate("/decisions")}>
            Back to decisions
          </button>
        </div>
      </div>
    );
  }

  const pollOpen   = poll?.status === "OPEN";
  const pollClosed = poll && !pollOpen;
  const isRating   = decision.votingType === "RATING_BASED";
  const isLocked   = decision.locked  ?? false;
  const isPinned   = decision.pinned  ?? false;

  return (
    <div className="dd-page">

      {/* ── Back nav ── */}
      <button className="dd-back-btn" onClick={() => navigate(-1)}>
        <FaArrowLeft /> Back
      </button>

      {/* ── Decision header card ── */}
      <div className="dd-header glass-panel">
        <div className="dd-header__top">
          <div className="dd-header__badges">
            {decision.communityName && (
              <span className="category-tag">{decision.communityName}</span>
            )}
            <span className={`voting-type-tag voting-type-tag--${decision.votingType?.toLowerCase()}`}>
              {decision.votingType?.replace("_", " ") ?? "—"}
            </span>

            {/* ── Live / Closed poll status ── */}
            {pollOpen   && <span className="live-badge"><FaBroadcastTower className="live-dot-icon" /> LIVE</span>}
            {pollClosed && <span className="closed-badge"><FaLock /> Closed</span>}

            {/* ── Pinned indicator ── */}
            {isPinned && (
              <span className="dd-pinned-badge">
                <FaThumbtack /> Pinned
              </span>
            )}

            {/* ── Locked discussion indicator ── */}
            {isLocked && (
              <span className="dd-locked-badge">
                <FaLock /> Discussion Locked
              </span>
            )}
          </div>

          <button className="vp-refresh-btn" onClick={fetchAll} disabled={loading} title="Refresh">
            <FaSync className={loading ? "spin" : ""} />
          </button>
        </div>

        <h1 className="dd-title">{decision.title}</h1>
        {decision.description && <p className="dd-description">{decision.description}</p>}

        <div className="dd-meta-row">
          {decision.creator && (
            <span className="dd-meta-chip">
              <FaUsers /> {decision.creator.username ?? decision.creator.email}
            </span>
          )}
          {decision.deadline && (
            <span className="dd-meta-chip">
              <FaCalendarAlt /> Deadline: {formatDate(decision.deadline)}
            </span>
          )}
          {poll?.endTime && (
            <span className="dd-meta-chip">
              <FaBroadcastTower />
              {pollOpen ? "Voting closes: " : "Voting closed: "}{formatDate(poll.endTime)}
            </span>
          )}
        </div>
      </div>

      {/* ── Main layout: voting + moderator panel ── */}
      <div className="dd-body-grid">

        {/* Left: voting panel */}
        <div className="dd-voting-col">
          {!poll ? (
            <div className="dd-no-poll glass-panel">
              <p>This decision hasn't been published for voting yet.</p>
            </div>
          ) : isRating ? (
            <div className="dd-voting-section">
              <RatingPanel decision={decision} pollOpen={pollOpen} />
            </div>
          ) : (
            <div className="dd-voting-section">
              <ChoicePanel decision={decision} poll={poll} />
            </div>
          )}

          {/* Discussion section — shown below voting */}
          <div className="dd-discussion-section">
            <Discussion
              decisionId={Number(decisionId)}
              isLocked={isLocked}
              isPinned={isPinned}
            />
          </div>
        </div>

        {/* Right: moderator panel (mod/admin only) */}
        {isMod && (
          <aside className="dd-mod-col">
            <ModeratorPanel
              decisionId={Number(decisionId)}
              communityId={decision.communityId ?? null}
              isPinned={isPinned}
              isLocked={isLocked}
              onDecisionUpdate={handleDecisionUpdate}
            />
          </aside>
        )}
      </div>
    </div>
  );
}
>>>>>>> Stashed changes
