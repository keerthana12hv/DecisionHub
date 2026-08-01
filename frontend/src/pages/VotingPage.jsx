<<<<<<< Updated upstream
import { useEffect, useState } from "react";
=======
import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import {
  FaVoteYea, FaCrown, FaShareAlt, FaSync, FaCheckCircle,
  FaHourglassHalf, FaLock, FaBroadcastTower
} from "react-icons/fa";
import api from "../services/api";
import {
  getPoll,
  submitVote,
  getMyVote,
} from "../services/voteService";
>>>>>>> Stashed changes
import "../styles/VotingPage.css";
import EditDecisionModal from "../components/EditDecisionModal";

<<<<<<< Updated upstream
const STORAGE_KEY = "decisionhub-decisions";
const USER_KEY = "decisionhub-current-user";

function getCurrentUser() {
  return localStorage.getItem(USER_KEY) || "Mythili";
}

const VotingPage = () => {
  const [polls, setPolls] = useState([]);
  const [commentInputs, setCommentInputs] = useState({});
  const currentUser = getCurrentUser();
  const [showEdit, setShowEdit] = useState(false);
  const [editDecision, setEditDecision] = useState(null);

  useEffect(() => {
    const storedDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setPolls(storedDecisions);
  }, []);

  const persistPolls = (nextPolls) => {
    setPolls(nextPolls);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
  };

  const handleVote = (pollId, optionId) => {
    setPolls((prevPolls) => {
      const nextPolls = prevPolls.map((poll) => {
        const hasVoted = poll.userVoteOptionId != null;

        if (poll.id !== pollId || hasVoted) {
          return poll;
        }

        return {
          ...poll,
          userVoteOptionId: optionId,
          options: poll.options.map((option) =>
            option.id === optionId ? { ...option, votes: option.votes + 1 } : option
          ),
        };
      });

      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
      return nextPolls;
    });
  };

  const handleCommentSubmit = (pollId, event) => {
    event.preventDefault();
    const commentText = commentInputs[pollId]?.trim();

    if (!commentText) return;

    setPolls((prevPolls) => {
      const nextPolls = prevPolls.map((poll) =>
        poll.id === pollId
          ? {
              ...poll,
              comments: [...(poll.comments || []), { id: Date.now(), user: "You", text: commentText }],
            }
          : poll
      );

      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
      return nextPolls;
    });

    setCommentInputs((prev) => ({ ...prev, [pollId]: "" }));
  };

  const handleSave = (updated) => {
    setPolls((prevPolls) => {
      const next = prevPolls.map((p) => (p.id === editDecision.id ? { ...p, ...updated } : p));
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
    setShowEdit(false);
    setEditDecision(null);
    alert("Decision updated");
  };

  return (
    <div className="voting-container">
      <div className="polls-list">
        {polls.length === 0 ? (
          <div className="voting-card">
            <h1>No polls yet</h1>
            <p className="decision-description">Create a decision first and it will appear here.</p>
=======
// How often (ms) to refresh live vote counts while a poll is OPEN
const LIVE_REFRESH_INTERVAL = 8000;

// ─── helpers ────────────────────────────────────────────────────────────────

/** Count votes-per-option from OptionResponseDto.
 *  - SINGLE/MULTIPLE_CHOICE → use voteCount (from votes table, now populated by backend)
 *  - RATING_BASED           → use comparisonScores unique userId count (fallback)
 */
const countVotesFromOptions = (options = []) => {
  const map = {};
  for (const opt of options) {
    map[opt.id] = opt.voteCount ?? opt.comparisonScores?.length ?? 0;
  }
  return map;
};

const totalVotes = (countMap) =>
  Object.values(countMap).reduce((s, v) => s + v, 0);

const winnerOptionId = (countMap) => {
  let maxId = null;
  let maxCount = -1;
  for (const [id, count] of Object.entries(countMap)) {
    if (count > maxCount) { maxCount = count; maxId = Number(id); }
  }
  return maxCount > 0 ? maxId : null;
};

const formatEndTime = (iso) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleString(undefined, {
    month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
};

// ─── PollCard ────────────────────────────────────────────────────────────────

function PollCard({ decision, onVoted }) {
  const { user } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [poll, setPoll]               = useState(null);
  const [voteCounts, setVoteCounts]   = useState(() => countVotesFromOptions(decision.options));
  const [myOptionIds, setMyOptionIds] = useState([]);          // user's current selection
  const [submitting, setSubmitting]   = useState(false);
  const [pendingId, setPendingId]     = useState(null);        // optimistic highlight
  const [justVoted, setJustVoted]     = useState(false);
  const [lastRefresh, setLastRefresh] = useState(null);
  const [loadingPoll, setLoadingPoll] = useState(true);
  const intervalRef = useRef(null);

  const isSingleChoice   = decision.votingType === "SINGLE_CHOICE";
  const isMultipleChoice = decision.votingType === "MULTIPLE_CHOICE";
  const isChoiceBased    = isSingleChoice || isMultipleChoice;
  const isRatingBased    = decision.votingType === "RATING_BASED";

  // ── initial load: poll status + my existing vote ──────────────────────────
  useEffect(() => {
    let cancelled = false;

    const init = async () => {
      setLoadingPoll(true);
      try {
        const [pollRes, myVoteRes] = await Promise.all([
          getPoll(decision.id),
          getMyVote(decision.id).catch(() => null),
        ]);
        if (cancelled) return;
        setPoll(pollRes.data);
        if (myVoteRes?.data?.optionIds?.length) {
          setMyOptionIds(myVoteRes.data.optionIds);
        }
      } catch {
        // poll may not exist yet for DRAFT decisions shown in the list
      } finally {
        if (!cancelled) setLoadingPoll(false);
      }
    };

    init();
    return () => { cancelled = true; };
  }, [decision.id]);

  // ── live count refresh while poll is OPEN ─────────────────────────────────
  const refreshCounts = useCallback(async () => {
    try {
      const res = await api.get(`/api/decisions/${decision.id}`);
      const updated = res.data;
      setVoteCounts(countVotesFromOptions(updated.options));
      setLastRefresh(new Date());
    } catch {
      // silently ignore refresh errors
    }
  }, [decision.id]);

  useEffect(() => {
    if (!poll) return;
    if (poll.status !== "OPEN") return;

    intervalRef.current = setInterval(refreshCounts, LIVE_REFRESH_INTERVAL);
    return () => clearInterval(intervalRef.current);
  }, [poll, refreshCounts]);

  // ── vote handlers ─────────────────────────────────────────────────────────
  const handleOptionClick = async (optionId) => {
    if (!isChoiceBased) return;
    if (submitting) return;
    if (poll?.status !== "OPEN") return;

    let nextIds;
    if (isSingleChoice) {
      // toggle off if same option tapped again
      nextIds = myOptionIds.includes(optionId) ? [] : [optionId];
    } else {
      nextIds = myOptionIds.includes(optionId)
        ? myOptionIds.filter((id) => id !== optionId)
        : [...myOptionIds, optionId];
    }

    // Optimistic UI
    setPendingId(optionId);
    const prevIds = myOptionIds;
    const prevCounts = { ...voteCounts };

    setMyOptionIds(nextIds);

    // Optimistically update counts
    const delta = {};
    for (const id of prevIds)  delta[id] = (delta[id] ?? 0) - 1;
    for (const id of nextIds)  delta[id] = (delta[id] ?? 0) + 1;
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
      addToast(
        nextIds.length > 0 ? "Vote recorded!" : "Vote removed.",
        "success"
      );
      if (nextIds.length > 0) {
        setTimeout(() => setJustVoted(false), 3000);
      }
      onVoted?.();
    } catch (err) {
      // rollback
      setMyOptionIds(prevIds);
      setVoteCounts(prevCounts);
      addToast(
        err?.response?.data?.message ?? "Failed to record vote. Please try again.",
        "error"
      );
    } finally {
      setSubmitting(false);
      setPendingId(null);
    }
  };

  const handleShare = () => {
    navigator.clipboard.writeText(
      `${window.location.origin}/decision/${decision.id}`
    );
    addToast("Decision link copied!", "success");
  };

  // ── derived display values ─────────────────────────────────────────────────
  const total         = totalVotes(voteCounts);
  const hasVoted      = myOptionIds.length > 0;
  const pollOpen      = poll?.status === "OPEN";
  const pollClosed    = poll && !pollOpen;
  const showResults   = hasVoted || pollClosed;
  const leadingId     = winnerOptionId(voteCounts);

  if (loadingPoll) {
    return (
      <div className="voting-card glass-panel voting-card--loading">
        <div className="vp-skeleton-title" />
        <div className="vp-skeleton-bar" />
        <div className="vp-skeleton-bar vp-skeleton-bar--short" />
      </div>
    );
  }

  // ── RATING_BASED: redirect card — voting happens on the detail page ────────
  if (isRatingBased) {
    return (
      <div className="voting-card glass-panel animate-pop-in">
        <div className="voting-card-header">
          <div className="voting-card-header__left">
            <div className="voting-card-meta">
              {decision.communityName && (
                <span className="category-tag">{decision.communityName}</span>
              )}
              <span className="voting-type-tag voting-type-tag--rating_based">
                RATING BASED
              </span>
              {poll?.status === "OPEN" && (
                <span className="live-badge">
                  <FaBroadcastTower className="live-dot-icon" /> LIVE
                </span>
              )}
            </div>
            <h2>{decision.title}</h2>
          </div>
          <button className="share-vote-btn" onClick={handleShare} title="Share">
            <FaShareAlt />
          </button>
        </div>

        {decision.description && (
          <p className="poll-description">{decision.description}</p>
        )}

        <div className="rating-redirect-banner">
          <p>
            This decision uses weighted factor scoring. Rate each option by
            factor on the detail page.
          </p>
          <button
            className="btn-primary"
            onClick={() => navigate(`/decision/${decision.id}`)}
          >
            Open &amp; Rate →
          </button>
        </div>

        <div className="voting-card-footer">
          <div className="vp-footer-stat">
            <FaVoteYea />
            <span>
              <strong>
                {decision.options?.reduce(
                  (s, o) => s + new Set((o.comparisonScores ?? []).map((sc) => sc.userId)).size,
                  0
                ) ?? 0}
              </strong>{" "}
              participant{decision.options?.length !== 1 ? "s" : ""}
            </span>
          </div>
          {poll?.endTime && (
            <div className="vp-footer-stat">
              <FaHourglassHalf />
              <span>
                {poll.status === "OPEN" ? "Closes " : "Closed "}
                {formatEndTime(poll.endTime)}
              </span>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className={`voting-card glass-panel animate-pop-in${justVoted ? " just-voted" : ""}`}>

      {/* ── Header ── */}
      <div className="voting-card-header">
        <div className="voting-card-header__left">
          <div className="voting-card-meta">
            {decision.communityName && (
              <span className="category-tag">{decision.communityName}</span>
            )}
            {decision.votingType && (
              <span className={`voting-type-tag voting-type-tag--${decision.votingType.toLowerCase()}`}>
                {decision.votingType.replace("_", " ")}
              </span>
            )}
          </div>
          <h2>{decision.title}</h2>
        </div>
        <div className="voting-card-header__actions">
          {pollOpen && (
            <span className="live-badge">
              <FaBroadcastTower className="live-dot-icon" />
              LIVE
            </span>
          )}
          {pollClosed && (
            <span className="closed-badge">
              <FaLock /> CLOSED
            </span>
          )}
          <button className="share-vote-btn" onClick={handleShare} title="Share">
            <FaShareAlt />
          </button>
        </div>
      </div>

      {decision.description && (
        <p className="poll-description">{decision.description}</p>
      )}

      {/* ── Options / Results ── */}
      <div className="voting-options-list">
        {decision.options?.map((option) => {
          const count      = voteCounts[option.id] ?? 0;
          const pct        = total === 0 ? 0 : Math.round((count / total) * 100);
          const isSelected = myOptionIds.includes(option.id);
          const isLeading  = option.id === leadingId;
          const isPending  = pendingId === option.id;

          return (
            <div
              key={option.id}
              className={[
                "voting-option-block",
                showResults ? "voting-option-block--results" : "",
                isSelected  ? "selected-choice" : "",
                isLeading && showResults ? "leading-choice" : "",
                !pollOpen   ? "voting-option-block--disabled" : "",
              ].filter(Boolean).join(" ")}
              onClick={() => pollOpen && isChoiceBased && handleOptionClick(option.id)}
              role={pollOpen && isChoiceBased ? "button" : undefined}
              tabIndex={pollOpen && isChoiceBased ? 0 : undefined}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") handleOptionClick(option.id);
              }}
            >
              <div className="option-row">
                {/* Selection indicator */}
                {isChoiceBased && pollOpen && (
                  <span className={`option-selector${isSingleChoice ? " radio" : " checkbox"}${isSelected ? " checked" : ""}`}>
                    {isSelected && <FaCheckCircle />}
                  </span>
                )}

                <div className="option-row__content">
                  <div className="option-row__top">
                    <span className="option-name-txt">
                      {option.title}
                      {isSelected && <span className="your-vote-tag"> · Your vote</span>}
                      {isPending && <span className="saving-tag"> saving…</span>}
                    </span>
                    {showResults && (
                      <span className="option-count-badge">
                        <strong>{count}</strong>
                        <span className="option-count-pct"> {pct}%</span>
                      </span>
                    )}
                  </div>

                  {/* Result bar — shown immediately after voting or when poll closed */}
                  {showResults && (
                    <div className="result-bar-track" aria-label={`${pct}%`}>
                      <div
                        className={`result-bar-fill${isLeading ? " result-bar-fill--leader" : ""}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  )}

                  {/* Leader badge */}
                  {isLeading && showResults && count > 0 && (
                    <span className="winner-badge-lbl">
                      <FaCrown /> Leading
                    </span>
                  )}
                </div>
              </div>

              {/* Un-voted CTA hint */}
              {!showResults && pollOpen && isChoiceBased && (
                <span className="click-vote-hint">
                  {isSingleChoice ? "Select" : "Toggle"}
                </span>
              )}
            </div>
          );
        })}
      </div>

      {/* ── Footer stats ── */}
      <div className="voting-card-footer">
        <div className="vp-footer-stat">
          <FaVoteYea />
          <span>
            <strong>{total}</strong> vote{total !== 1 ? "s" : ""}
          </span>
        </div>
        {poll?.endTime && (
          <div className="vp-footer-stat">
            <FaHourglassHalf />
            <span>
              {pollOpen ? "Closes " : "Closed "}{formatEndTime(poll.endTime)}
            </span>
          </div>
        )}
        {pollOpen && lastRefresh && (
          <button
            className="vp-refresh-btn"
            onClick={refreshCounts}
            title="Refresh counts now"
          >
            <FaSync className={submitting ? "spin" : ""} />
            <span>
              Updated {lastRefresh.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
            </span>
          </button>
        )}
      </div>

      {/* ── Voted confirmation overlay ── */}
      {justVoted && (
        <div className="thanks-overlay-anim animate-pop-in">
          <div className="checkmark-circle">
            <div className="checkmark-draw" />
          </div>
          <h3>Vote Recorded!</h3>
          <p>Results are updating in real time below.</p>
        </div>
      )}
    </div>
  );
}

// ─── VotingPage ───────────────────────────────────────────────────────────────

export default function VotingPage() {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState(null);
  const [filter, setFilter]       = useState("ACTIVE"); // ACTIVE | ALL

  const fetchDecisions = useCallback(async () => {
    try {
      setError(null);
      const params = filter === "ACTIVE" ? { status: "ACTIVE" } : {};
      const res = await api.get("/api/decisions", { params });
      setDecisions(res.data ?? []);
    } catch (err) {
      const msg = err?.response?.data?.message ?? "Failed to load decisions.";
      setError(msg);
      addToast(msg, "error");
    } finally {
      setLoading(false);
    }
  }, [filter, addToast]);

  useEffect(() => {
    setLoading(true);
    fetchDecisions();
  }, [fetchDecisions]);

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="voting-page-container">

            {/* ── Page header ── */}
            <div className="voting-header">
              <div>
                <h1>Collaborative Voting Room</h1>
                <p>
                  Cast your vote, watch live tallies update in real time, and
                  see results revealed the moment you participate.
                </p>
              </div>
              <div className="voting-header__controls">
                <div className="vp-filter-tabs">
                  {["ACTIVE", "ALL"].map((f) => (
                    <button
                      key={f}
                      className={`vp-filter-tab${filter === f ? " active" : ""}`}
                      onClick={() => setFilter(f)}
                    >
                      {f === "ACTIVE" ? "Active polls" : "All decisions"}
                    </button>
                  ))}
                </div>
                <button
                  className="vp-refresh-btn vp-refresh-btn--header"
                  onClick={() => { setLoading(true); fetchDecisions(); }}
                  title="Refresh"
                >
                  <FaSync />
                  Refresh
                </button>
              </div>
            </div>

            {/* ── Body ── */}
            {loading ? (
              <div className="polls-feed-grid">
                {[1, 2].map((n) => (
                  <div key={n} className="voting-card glass-panel voting-card--loading">
                    <div className="vp-skeleton-title" />
                    <div className="vp-skeleton-bar" />
                    <div className="vp-skeleton-bar vp-skeleton-bar--short" />
                  </div>
                ))}
              </div>
            ) : error ? (
              <div className="glass-card empty-voting-state">
                <FaVoteYea className="empty-icon" />
                <h3>Could not load voting data</h3>
                <p>{error}</p>
                <button className="btn-primary" onClick={fetchDecisions}>
                  Try again
                </button>
              </div>
            ) : decisions.length === 0 ? (
              <div className="glass-card empty-voting-state">
                <FaVoteYea className="empty-icon" />
                <h3>No Active Polls</h3>
                <p>
                  {filter === "ACTIVE"
                    ? "There are no active choice-based polls right now. Try switching to 'All decisions'."
                    : "No decisions have been created yet."}
                </p>
              </div>
            ) : (
              <div className="polls-feed-grid">
                {decisions.map((decision) => (
                  <PollCard
                    key={decision.id}
                    decision={decision}
                    onVoted={fetchDecisions}
                  />
                ))}
              </div>
            )}
>>>>>>> Stashed changes
          </div>
        ) : (
          polls.map((poll) => {
          const totalVotes = (poll.options || []).reduce((sum, option) => sum + (option.votes || 0), 0);
          const hasVoted = poll.userVoteOptionId != null;
          const isOwner = poll.creator === currentUser;
          const isActive = poll.status === "Active";
          const isPublic = poll.visibility === "Public";
          const invited = (poll.invites || []).includes(currentUser);
          const canVote = !hasVoted && isActive && ((isPublic && !isOwner) || (!isPublic && (isOwner || invited)));

          return (
            <div key={poll.id} className="voting-card">
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <h1>{poll.title}</h1>
                {poll.creator === currentUser && (
                  <button className="edit-inline-btn" onClick={() => { setEditDecision(poll); setShowEdit(true); }}>
                    Edit
                  </button>
                )}
              </div>
              <p className="decision-description">{poll.description}</p>

              {poll.options.map((option) => {
                const percentage = totalVotes === 0 ? 0 : Math.round((option.votes / totalVotes) * 100);

                return (
                  <div key={option.id} className="option-block">
                    <div className="option-row">
                      <span>{option.name}</span>
                      {hasVoted && <span className="percentage">{percentage}%</span>}
                    </div>

                    {hasVoted && (
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${percentage}%` }}></div>
                      </div>
                    )}

                    {canVote ? (
                      <button onClick={() => handleVote(poll.id, option.id)} className="vote-btn-card">
                        Vote for {option.name}
                      </button>
                    ) : (
                      !hasVoted && <div style={{ color: "#9ca3af" }}>{isOwner ? "Can't vote your own decision" : isPublic ? "Voting unavailable" : "Private poll"}</div>
                    )}

                    {hasVoted && poll.userVoteOptionId === option.id && (
                      <span className="selected-badge">✅ You voted this</span>
                    )}
                  </div>
                );
              })}

              <p className="total-votes">Total votes: {totalVotes}</p>

              {hasVoted && <div className="thanks-box">🎉 Thank you for voting!</div>}

              <div className="comments-section">
                <h3>Comments</h3>
                <form onSubmit={(event) => handleCommentSubmit(poll.id, event)} className="comment-form">
                  <textarea
                    rows="3"
                    value={commentInputs[poll.id] || ""}
                    onChange={(event) =>
                      setCommentInputs((prev) => ({ ...prev, [poll.id]: event.target.value }))
                    }
                    placeholder="Share your thoughts..."
                  />
                  <button type="submit">Post Comment</button>
                </form>

                <div className="comment-list">
                  {(poll.comments || []).map((comment) => (
                    <div key={comment.id} className="comment-item">
                      <strong>{comment.user}</strong>
                      <p>{comment.text}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            );
          })
        )}
      </div>
      {showEdit && editDecision && (
        <EditDecisionModal
          decision={editDecision}
          onClose={() => { setShowEdit(false); setEditDecision(null); }}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
