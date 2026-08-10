import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  FaArrowLeft, FaBroadcastTower, FaLock, FaLockOpen,
  FaUsers, FaCalendarAlt, FaCrown, FaSync,
  FaCheckCircle, FaThumbtack,
} from "react-icons/fa";
import api from "../services/api";
import { getPoll, submitVote, getMyVote } from "../services/voteService";
import RatingPanel from "../components/RatingPanel";
import ModeratorPanel from "../components/ModeratorPanel";
import Discussion from "./Discussion";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import "../styles/DecisionDetail.css";

// ─── helpers ──────────────────────────────────────────────────────────────────

const fmt = (iso) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleString(undefined, {
    year: "numeric", month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
};

const countVotes = (options = []) => {
  const m = {};
  for (const o of options) {
    m[o.id] = o.voteCount ?? new Set((o.comparisonScores ?? []).map((s) => s.userId)).size;
  }
  return m;
};
const sumVotes  = (m) => Object.values(m).reduce((s, v) => s + v, 0);
const leadId    = (m) => {
  let best = null, bv = -1;
  for (const [id, v] of Object.entries(m)) if (v > bv) { bv = v; best = Number(id); }
  return bv > 0 ? best : null;
};

// ─── ChoicePanel ──────────────────────────────────────────────────────────────

function ChoicePanel({ decision, poll }) {
  const { addToast }                          = useToast();
  const [myIds,   setMyIds]                   = useState([]);
  const [counts,  setCounts]                  = useState(() => countVotes(decision.options));
  const [busy,    setBusy]                    = useState(false);
  const [voted,   setVoted]                   = useState(false);

  const open    = poll?.status === "OPEN";
  const single  = decision.votingType === "SINGLE_CHOICE";
  const show    = myIds.length > 0 || !open;
  const total   = sumVotes(counts);
  const winner  = leadId(counts);

  useEffect(() => {
    if (!decision?.id) return;
    getMyVote(decision.id).then((r) => setMyIds(r.data?.optionIds ?? [])).catch(() => {});
  }, [decision.id]);

  const handleSelect = async (optId) => {
    if (!open || busy) return;
    const next = single
      ? (myIds.includes(optId) ? [] : [optId])
      : (myIds.includes(optId) ? myIds.filter((i) => i !== optId) : [...myIds, optId]);

    const prev = myIds, prevC = { ...counts };
    setMyIds(next);
    const delta = {};
    for (const i of prev) delta[i] = (delta[i] ?? 0) - 1;
    for (const i of next) delta[i] = (delta[i] ?? 0) + 1;
    setCounts((c) => {
      const n = { ...c };
      for (const [i, d] of Object.entries(delta)) n[Number(i)] = Math.max(0, (n[Number(i)] ?? 0) + d);
      return n;
    });

    setBusy(true);
    try {
      await submitVote(decision.id, next);
      setVoted(next.length > 0);
      addToast(next.length > 0 ? "Vote recorded!" : "Vote removed.", "success");
      if (next.length > 0) setTimeout(() => setVoted(false), 3000);
    } catch (err) {
      setMyIds(prev); setCounts(prevC);
      addToast(err?.response?.data?.message ?? "Vote failed.", "error");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="choice-panel">
      <div className="choice-panel-header">
        <h3>{open ? "Cast your vote" : "Voting closed"}</h3>
        {!open && <span className="poll-closed-tag"><FaLock /> Results are final</span>}
        {open && <span className="choice-hint">{single ? "Select one" : "Select all that apply"}</span>}
      </div>

      <div className="choice-options">
        {decision.options?.map((opt) => {
          const count = counts[opt.id] ?? 0;
          const pct   = total === 0 ? 0 : Math.round((count / total) * 100);
          const sel   = myIds.includes(opt.id);
          const lead  = opt.id === winner;
          return (
            <div key={opt.id}
              className={["choice-opt", sel ? "choice-opt--sel" : "", lead && show ? "choice-opt--lead" : "", !open ? "choice-opt--disabled" : ""].filter(Boolean).join(" ")}
              onClick={() => handleSelect(opt.id)}
              role={open ? "button" : undefined} tabIndex={open ? 0 : undefined}
              onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") handleSelect(opt.id); }}
            >
              <div className="choice-opt-row">
                <span className={`choice-dot${single ? " radio" : ""}${sel ? " sel" : ""}`}>
                  {sel && <FaCheckCircle />}
                </span>
                <div className="choice-opt-body">
                  <div className="choice-opt-top">
                    <span className="choice-opt-name">
                      {opt.title}
                      {sel && <span className="your-vote-tag"> · Your vote</span>}
                    </span>
                    {show && <span className="choice-opt-count"><strong>{count}</strong> <span className="opt-pct">{pct}%</span></span>}
                  </div>
                  {show && (
                    <div className="result-bar-track">
                      <div className={`result-bar-fill${lead ? " result-bar-fill--lead" : ""}`} style={{ width: `${pct}%` }} />
                    </div>
                  )}
                  {lead && show && count > 0 && <span className="winner-lbl"><FaCrown /> Leading</span>}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="choice-footer">
        <span>Total votes: <strong>{total}</strong></span>
        {voted && <span className="vote-ok"><FaCheckCircle /> Vote confirmed</span>}
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
    setLoading(true); setError(null);
    try {
      const [dRes, pRes] = await Promise.all([
        api.get(`/api/decisions/${decisionId}`),
        api.get(`/api/decisions/${decisionId}/poll`).catch(() => null),
      ]);
      setDecision(dRes.data);
      setPoll(pRes?.data ?? null);
    } catch (err) {
      const msg = err?.response?.data?.message ?? "Could not load this decision.";
      setError(msg); addToast(msg, "error");
    } finally {
      setLoading(false);
    }
  }, [decisionId, addToast]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  // ModeratorPanel callback — sync decision state after lock/pin
  const handleDecisionUpdate = (updated) => {
    if (updated) setDecision(updated);
    else fetchAll();
  };

  if (loading) return (
    <div className="dashboard">
      <Sidebar /><div className="dashboard-main"><Navbar />
        <div className="dashboard-content">
          <div className="dd-state"><FaSync className="spin" /> Loading decision…</div>
        </div>
      </div>
    </div>
  );

  if (error || !decision) return (
    <div className="dashboard">
      <Sidebar /><div className="dashboard-main"><Navbar />
        <div className="dashboard-content">
          <div className="dd-state dd-state--error">
            <p>{error ?? "Decision not found."}</p>
            <button className="btn-primary" onClick={() => navigate("/decisions")}>Back to decisions</button>
          </div>
        </div>
      </div>
    </div>
  );

  const pollOpen   = poll?.status === "OPEN";
  const pollClosed = poll && !pollOpen;
  const isRating   = decision.votingType === "RATING_BASED";
  const isLocked   = decision.locked  ?? false;
  const isPinned   = decision.pinned  ?? false;

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="dd-page">

            {/* Back */}
            <button className="dd-back-btn" onClick={() => navigate(-1)}>
              <FaArrowLeft /> Back
            </button>

            {/* Header card */}
            <div className="dd-header glass-panel">
              <div className="dd-header-top">
                <div className="dd-badges">
                  {decision.communityName && <span className="category-tag">{decision.communityName}</span>}
                  <span className={`voting-type-tag voting-type-tag--${decision.votingType?.toLowerCase()}`}>
                    {decision.votingType?.replace("_", " ")}
                  </span>
                  {pollOpen   && <span className="live-badge"><FaBroadcastTower className="live-dot-icon" /> LIVE</span>}
                  {pollClosed && <span className="closed-badge"><FaLock /> Closed</span>}
                  {isPinned && (
                    <span className="dd-pinned-badge"><FaThumbtack /> Pinned</span>
                  )}
                  {isLocked && (
                    <span className="dd-locked-badge"><FaLock /> Discussion Locked</span>
                  )}
                </div>
                <button className="vp-refresh-btn" onClick={fetchAll} disabled={loading} title="Refresh">
                  <FaSync className={loading ? "spin" : ""} />
                </button>
              </div>

              <h1 className="dd-title">{decision.title}</h1>
              {decision.description && <p className="dd-desc">{decision.description}</p>}

              <div className="dd-meta">
                {decision.creator && (
                  <span className="dd-meta-chip"><FaUsers /> {decision.creator.username ?? decision.creator.email}</span>
                )}
                {decision.deadline && (
                  <span className="dd-meta-chip"><FaCalendarAlt /> Deadline: {fmt(decision.deadline)}</span>
                )}
                {poll?.endTime && (
                  <span className="dd-meta-chip">
                    <FaBroadcastTower /> {pollOpen ? "Closes: " : "Closed: "}{fmt(poll.endTime)}
                  </span>
                )}
              </div>
            </div>

            {/* Body — two column: content + mod panel */}
            <div className={`dd-body${isMod ? " dd-body--with-mod" : ""}`}>

              {/* Left column */}
              <div className="dd-content-col">

                {/* Voting panel */}
                {!poll ? (
                  <div className="dd-card glass-panel">
                    <p className="dd-muted">This decision hasn't been published for voting yet.</p>
                  </div>
                ) : isRating ? (
                  <div className="dd-card">
                    <RatingPanel decision={decision} pollOpen={pollOpen} />
                  </div>
                ) : (
                  <div className="dd-card glass-panel">
                    <ChoicePanel decision={decision} poll={poll} />
                  </div>
                )}

                {/* Discussion */}
                <div className="dd-card glass-panel">
                  <Discussion
                    decisionId={Number(decisionId)}
                    isLocked={isLocked}
                    isPinned={isPinned}
                  />
                </div>
              </div>

              {/* Right column — mod panel */}
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
        </div>
      </div>
    </div>
  );
}
