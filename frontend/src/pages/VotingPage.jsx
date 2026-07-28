import { useEffect, useState } from "react";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { FaVoteYea, FaShareAlt, FaCheckCircle, FaTimesCircle } from "react-icons/fa";
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
  const [selections, setSelections] = useState({}); // decisionId -> optionIds[] (in-progress selection)
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
        (d) => d.status === "ACTIVE" && d.votingType !== "RATING_BASED"
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

  const toggleSelection = (decisionId, optionId, votingType) => {
    setSelections((prev) => {
      const current = prev[decisionId] || myVotes[decisionId] || [];
      let next;
      if (votingType === "SINGLE_CHOICE") {
        next = current.includes(optionId) ? [] : [optionId];
      } else {
        next = current.includes(optionId)
          ? current.filter((id) => id !== optionId)
          : [...current, optionId];
      }
      return { ...prev, [decisionId]: next };
    });
  };

  const submitVote = async (decisionId) => {
    const optionIds = selections[decisionId] ?? myVotes[decisionId] ?? [];
    setSubmitting(decisionId);
    try {
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds },
        headers()
      );
      setMyVotes((prev) => ({ ...prev, [decisionId]: optionIds }));
      addToast(
        optionIds.length === 0 ? "Vote removed" : "Vote submitted!",
        "success"
      );
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
                  const current = selections[poll.id] ?? myVotes[poll.id] ?? [];
                  const hasVoted = (myVotes[poll.id] || []).length > 0;
                  const isDirty =
                    selections[poll.id] !== undefined &&
                    JSON.stringify([...selections[poll.id]].sort()) !==
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

                      <div className="voting-options-list">
                        {poll.options.map((option) => {
                          const isSelected = current.includes(option.id);
                          return (
                            <button
                              key={option.id}
                              className={`voting-option-block ${isSelected ? "selected-choice" : ""}`}
                              onClick={() => toggleSelection(poll.id, option.id, poll.votingType)}
                            >
                              <span className="option-name-txt">{option.title}</span>
                              {isSelected && <FaCheckCircle className="option-selected-icon" />}
                            </button>
                          );
                        })}
                      </div>

                      <div className="voting-card-footer">
                        <span>
                          {poll.votingType === "SINGLE_CHOICE" ? "Select one option" : "Select one or more"}
                        </span>
                        <span>Voting ends: {new Date(poll.votingEndTime).toLocaleDateString()}</span>
                      </div>

                      <div className="vote-action-row">
                        {hasVoted && !isDirty && (
                          <span className="vote-status-confirmed">
                            <FaCheckCircle /> Your vote is recorded
                          </span>
                        )}
                        <button
                          className="btn-primary comment-submit-btn"
                          disabled={submitting === poll.id || (!isDirty && !hasVoted)}
                          onClick={() => submitVote(poll.id)}
                        >
                          {submitting === poll.id
                            ? "Saving..."
                            : hasVoted
                            ? "Update Vote"
                            : "Submit Vote"}
                        </button>
                        {hasVoted && (
                          <button
                            className="btn-secondary remove-vote-btn"
                            disabled={submitting === poll.id}
                            onClick={() => {
                              setSelections((prev) => ({ ...prev, [poll.id]: [] }));
                              submitVote(poll.id);
                            }}
                          >
                            <FaTimesCircle /> Remove Vote
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