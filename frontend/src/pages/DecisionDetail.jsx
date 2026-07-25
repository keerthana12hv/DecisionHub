import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import RatingPanel from "../components/RatingPanel";
import DecisionModerationControls from "../components/moderator/DecisionModerationControls";
import "../styles/DecisionDetail.css";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}` }
});

// Turns "Food Variety" into the same key format CreateDecision.jsx uses — kept in
// sync so the matrix can look up each option's stored criterion value by name.
const findCriterionValue = (option, criterionName) => {
  const match = option.criteria?.find(
    (c) => c.criterionName?.toLowerCase() === criterionName.toLowerCase()
  );
  return match?.remarks || match?.score || "—";
};

export default function DecisionDetail() {
  const { id: decisionId } = useParams();
  const [decision, setDecision] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [activeTab, setActiveTab] = useState("overview");

  // Voting state for SINGLE_CHOICE / MULTIPLE_CHOICE
  const [myVoteOptionIds, setMyVoteOptionIds] = useState([]);
  const [voting, setVoting] = useState(false);

  useEffect(() => {
    fetchDecision();
    fetchMyVote();
    try {
      const t = token();
      const payload = JSON.parse(atob(t.split(".")[1]));
      setCurrentUserId(payload.id);
    } catch (err) {
      console.error("Failed to decode token:", err);
    }
  }, [decisionId]);

  const fetchDecision = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`${API}/decisions/${decisionId}`, headers());
      setDecision(res.data);
    } catch (err) {
      console.error("Failed to fetch decision:", err);
      setError("Could not load this decision.");
    } finally {
      setLoading(false);
    }
  };

  // Reuses the same endpoint already confirmed working in VotingPage.jsx
  const fetchMyVote = async () => {
    try {
      const res = await axios.get(`${API}/decisions/${decisionId}/votes/me`, headers());
      setMyVoteOptionIds(res.data?.optionIds || []);
    } catch (err) {
      // No vote cast yet is a normal state, not necessarily an error
      setMyVoteOptionIds([]);
    }
  };

  const handleVote = async (optionId) => {
    if (voting) return;
    setVoting(true);
    try {
      const isMultiple = decision.votingType === "MULTIPLE_CHOICE";
      let nextIds;
      if (isMultiple) {
        nextIds = myVoteOptionIds.includes(optionId)
          ? myVoteOptionIds.filter((id) => id !== optionId)
          : [...myVoteOptionIds, optionId];
      } else {
        nextIds = [optionId];
      }
      await axios.post(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds: nextIds },
        headers()
      );
      setMyVoteOptionIds(nextIds);
      await fetchDecision(); // refresh vote counts
    } catch (err) {
      console.error("Failed to submit vote:", err);
    } finally {
      setVoting(false);
    }
  };

  const isModerator =
    decision?.creator && String(decision.creator.id) === String(currentUserId);

  const hasCriteria = decision?.factors && decision.factors.length > 0;

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          {loading && <p>Loading decision...</p>}
          {error && <p>{error}</p>}
          {!loading && !error && decision && (
            <div className="decision-detail-page">
              <div className="detail-page-tags">
                {decision.categoryName && <span className="tag-pill">#{decision.categoryName}</span>}
                <span className="tag-pill status-pill">{decision.status}</span>
              </div>
              <h2>{decision.title}</h2>

              {isModerator && (
                <DecisionModerationControls
                  decision={decision}
                  onUpdate={(updated) => setDecision(updated)}
                />
              )}

              {/* Tabs */}
              <div className="detail-tabs">
                {["overview", "discussion", "poll results", "edit board"].map((tab) => {
                  const key = tab.replace(" ", "-");
                  return (
                    <button
                      key={key}
                      className={`detail-tab-btn ${activeTab === key ? "active" : ""}`}
                      onClick={() => setActiveTab(key)}
                    >
                      {tab.charAt(0).toUpperCase() + tab.slice(1)}
                    </button>
                  );
                })}
              </div>

              {/* Overview Tab */}
              {activeTab === "overview" && (
                <div className="detail-tab-content">
                  <div className="description-context-card">
                    <h3>Description &amp; Context</h3>
                    <p>{decision.description}</p>
                  </div>

                  {hasCriteria && (
                    <div className="comparison-matrix">
                      <h3>Comparison Matrix</h3>
                      <table>
                        <thead>
                          <tr>
                            <th>Parameter</th>
                            {decision.options.map((opt) => (
                              <th key={opt.id}>{opt.title}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {decision.factors.map((factor) => (
                            <tr key={factor.id || factor.name}>
                              <td>{factor.name}</td>
                              {decision.options.map((opt) => (
                                <td key={opt.id}>
                                  {findCriterionValue(opt, factor.name)}
                                </td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}

                  {decision.votingType === "RATING_BASED" ? (
                    <RatingPanel
                      decision={decision}
                      pollOpen={decision.poll?.status === "OPEN" || decision.status === "ACTIVE"}
                    />
                  ) : (
                    <div className="available-options">
                      <h3>Available Options</h3>
                      <div className="options-grid">
                        {decision.options.map((opt) => {
                          const hasVoted = myVoteOptionIds.includes(opt.id);
                          return (
                            <div
                              key={opt.id}
                              className={`option-card ${hasVoted ? "voted" : ""}`}
                            >
                              <div className="option-card-header">
                                <h4>{opt.title}</h4>
                                <span className="vote-count-badge">
                                  Votes: {opt.voteCount ?? 0}
                                </span>
                              </div>
                              <p>{opt.description}</p>
                              <button
                                className={hasVoted ? "btn-voted" : "btn-vote"}
                                disabled={voting}
                                onClick={() => handleVote(opt.id)}
                              >
                                {hasVoted ? "Voted" : "Vote for this option"}
                              </button>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {activeTab === "discussion" && (
                <div className="detail-tab-content">
                  <p className="tab-placeholder">Discussion — coming soon.</p>
                </div>
              )}

              {activeTab === "poll-results" && (
                <div className="detail-tab-content">
                  <p className="tab-placeholder">Poll Results — coming soon.</p>
                </div>
              )}

              {activeTab === "edit-board" && (
                <div className="detail-tab-content">
                  <p className="tab-placeholder">Edit Board — coming soon.</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
