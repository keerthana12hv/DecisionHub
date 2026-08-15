import { useEffect, useState } from "react";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { FaVoteYea, FaShareAlt, FaCheckCircle } from "react-icons/fa";
import "../styles/VotingPage.css";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

const VotingPage = () => {
  const { addToast } = useToast();
  const [polls, setPolls] = useState([]);
  const [myVotes, setMyVotes] = useState({}); // decisionId -> optionIds[]
  // MULTIPLE_CHOICE only: staged checkbox selections per decision, kept
  // separate from myVotes so nothing submits until Submit Vote is clicked.
  const [pendingSelections, setPendingSelections] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(null);

  useEffect(() => {
    loadActivePolls();
  }, []);

  const loadActivePolls = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`${API}/decisions`, headers());
      const votable = res.data.filter(
        (d) => d.status === "ACTIVE" && 
               d.votingType !== "RATING_BASED" && 
               (!d.votingEndTime || new Date() < new Date(d.votingEndTime))
      );
      setPolls(votable);

      // Fetch each decision's current user vote in parallel
      const voteEntries = await Promise.all(
        votable.map(async (d) => {
          try {
            const voteRes = await axios.get(`${API}/decisions/${d.id}/votes/me`, headers());
            return [d.id, voteRes.data.optionIds || []];
          } catch {
            return [d.id, []];
          }
        })
      );
      setMyVotes(Object.fromEntries(voteEntries));
    } catch (err) {
      console.error("Failed to load polls:", err);
      addToast("Failed to load voting room", "error");
    } finally {
      setLoading(false);
    }
  };

  // SINGLE_CHOICE: selecting a radio button submits immediately, no button needed.
  const handleSingleChoiceVote = async (decisionId, optionId) => {
    setSubmitting(decisionId);
    try {
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds: [optionId] },
        headers()
      );
      setMyVotes((prev) => ({ ...prev, [decisionId]: [optionId] }));
      addToast("Vote submitted!", "success");
    } catch (err) {
      console.error("Failed to submit vote:", err);
      addToast(err.response?.data?.message || "Failed to submit vote", "error");
    } finally {
      setSubmitting(null);
    }
  };

  // MULTIPLE_CHOICE: checkboxes only update the staged local selection.
  const toggleMultipleChoiceOption = (decisionId, optionId) => {
    setPendingSelections((prev) => {
      const current = prev[decisionId] ?? myVotes[decisionId] ?? [];
      const next = current.includes(optionId)
        ? current.filter((id) => id !== optionId)
        : [...current, optionId];
      return { ...prev, [decisionId]: next };
    });
  };

  // MULTIPLE_CHOICE: explicit Submit Vote button sends the staged selection.
  const submitMultipleChoiceVote = async (decisionId) => {
    const optionIds = pendingSelections[decisionId] ?? myVotes[decisionId] ?? [];
    setSubmitting(decisionId);
    try {
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds },
        headers()
      );
      setMyVotes((prev) => ({ ...prev, [decisionId]: optionIds }));
      setPendingSelections((prev) => {
        const next = { ...prev };
        delete next[decisionId];
        return next;
      });
      addToast(optionIds.length === 0 ? "Vote removed" : "Vote submitted!", "success");
    } catch (err) {
      console.error("Failed to submit vote:", err);
      addToast(err.response?.data?.message || "Failed to submit vote", "error");
    } finally {
      setSubmitting(null);
    }
  };

  const handleShare = (decisionId) => {
    const shareUrl = `${window.location.origin}/decision/${decisionId}`;
    navigator.clipboard.writeText(shareUrl);
    addToast("Decision link copied to clipboard!", "success");
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="voting-page-container">
            <div className="voting-header">
              <h1>Collaborative Voting Room</h1>
              <p>Cast your vote on active single and multiple-choice decisions.</p>
            </div>

            <div className="polls-feed-grid">
              {loading ? (
                <div className="glass-card empty-voting-state">
                  <p>Loading active polls...</p>
                </div>
              ) : polls.length === 0 ? (
                <div className="glass-card empty-voting-state">
                  <FaVoteYea className="empty-icon" />
                  <h3>No Active Decisions</h3>
                  <p>There are no single or multiple-choice decisions open for voting right now.</p>
                </div>
              ) : (
                polls.map((poll) => {
                  const isSingle = poll.votingType === "SINGLE_CHOICE";
                  const current = pendingSelections[poll.id] ?? myVotes[poll.id] ?? [];
                  const hasVoted = (myVotes[poll.id] || []).length > 0;
                  const isDirty =
                    isSingle
                      ? false
                      : pendingSelections[poll.id] !== undefined &&
                        JSON.stringify([...pendingSelections[poll.id]].sort()) !==
                          JSON.stringify([...(myVotes[poll.id] || [])].sort());

                  return (
                    <div key={poll.id} className="voting-card glass-panel animate-pop-in">
                      <div className="voting-card-header">
                        <div>
                          <span className="category-tag">{poll.categoryName}</span>
                          <h2>{poll.title}</h2>
                        </div>
                        <button
                          className="share-vote-btn"
                          onClick={() => handleShare(poll.id)}
                          title="Share Decision"
                        >
                          <FaShareAlt />
                        </button>
                      </div>

                      <p className="poll-description">{poll.description}</p>

                      {/* Radio buttons for Single Choice, checkboxes for Multiple
                          Choice — question with options directly underneath,
                          no separate per-option vote button/card.
                          NOTE: this is a <label>, not a <div onClick>. A label
                          wrapping its input forwards a click to the input exactly
                          once via native browser semantics. Attaching onClick to
                          the wrapper AND onChange to the input double-fires on a
                          direct click (toggle, then toggle back) — that's what
                          made the checkboxes/radios look unresponsive before. */}
                      <div className="vote-question-list" style={{ display: "flex", flexDirection: "column", gap: "0.6rem", margin: "0.75rem 0", width: "100%" }}>
                        {poll.options.map((option) => {
                          const isSelected = current.includes(option.id);
                          return (
                            <label
                              key={option.id}
                              style={{
                                display: "flex",
                                flexDirection: "row",
                                alignItems: "center",
                                gap: "0.6rem",
                                cursor: submitting === poll.id ? "not-allowed" : "pointer",
                                width: "100%",
                                float: "none",
                                position: "static",
                                textAlign: "left"
                              }}
                            >
                              <input
                                type={isSingle ? "radio" : "checkbox"}
                                name={isSingle ? `poll-${poll.id}` : undefined}
                                checked={isSelected}
                                disabled={submitting === poll.id}
                                onChange={() =>
                                  isSingle
                                    ? handleSingleChoiceVote(poll.id, option.id)
                                    : toggleMultipleChoiceOption(poll.id, option.id)
                                }
                                style={{ flexShrink: 0, margin: 0, position: "static", float: "none", width: "18px", height: "18px" }}
                              />
                              <span style={{ textAlign: "left" }}>{option.title}</span>
                            </label>
                          );
                        })}
                      </div>

                      <div className="voting-card-footer">
                        <span>{isSingle ? "Select one option" : "Select one or more"}</span>
                        <span>Voting ends: {new Date(poll.votingEndTime).toLocaleDateString()}</span>
                      </div>

                      <div className="vote-action-row">
                        {hasVoted && !isDirty && (
                          <span className="vote-status-confirmed">
                            <FaCheckCircle /> Your vote is recorded
                          </span>
                        )}
                        {/* Only MULTIPLE_CHOICE needs an explicit submit button —
                            SINGLE_CHOICE submits instantly on selection. */}
                        {!isSingle && (
                          <button
                            className="btn-primary comment-submit-btn"
                            disabled={submitting === poll.id || (!isDirty && !hasVoted)}
                            onClick={() => submitMultipleChoiceVote(poll.id)}
                          >
                            {submitting === poll.id ? "Saving..." : hasVoted ? "Update Vote" : "Submit Vote"}
                          </button>
                        )}
                      </div>

                      <p className="results-note">
                        Vote counts and results will be available once the analytics module ships.
                      </p>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VotingPage;