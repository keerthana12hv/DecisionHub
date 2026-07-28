import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import RatingPanel from "../components/RatingPanel";
import PollResultsPanel from "../components/PollResultsPanel";
import EditBoardPanel from "../components/EditBoardPanel";
import DecisionModerationControls from "../components/moderator/DecisionModerationControls";
import { getCommunities, getMembers } from "../services/communityService";
import "../styles/DecisionDetail.css";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}` }
});

// Options store values in comparisonScores keyed by factorId (confirmed from the real
// API response) — match the factor's id, not a criterionName field that doesn't exist.
const findCriterionValue = (option, factor) => {
  const match = (option.comparisonScores || []).find((s) => s.factorId === factor.id);
  if (!match) return "—";
  return match.remarks || match.score || "—";
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

  // Membership gate for private (community-scoped) decisions.
  // WORKAROUND: the decision API only returns communityName, not communityId,
  // so we match by name against the user's community list to find the real id.
  const [canParticipate, setCanParticipate] = useState(true);
  const [membershipChecked, setMembershipChecked] = useState(false);

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
    if (voting || !canParticipate) return;
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
      await axios.put(
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

  useEffect(() => {
    if (!decision || !currentUserId) return;
    checkMembership();
  }, [decision, currentUserId]);

  const checkMembership = async () => {
    // Public decisions (no community) — everyone can participate.
    if (!decision.communityName) {
      setCanParticipate(true);
      setMembershipChecked(true);
      return;
    }
    try {
      const communities = await getCommunities();
      const match = communities.find((c) => c.name === decision.communityName);
      if (!match) {
        // Can't verify — block by default to be safe.
        setCanParticipate(false);
        setMembershipChecked(true);
        return;
      }
      const membersRes = await getMembers(match.id);
      const isApprovedMember = (membersRes.data || []).some(
        (m) => String(m.userId) === String(currentUserId)
      );
      setCanParticipate(isApprovedMember || isModerator);
    } catch (err) {
      console.error("Failed to check community membership:", err);
      setCanParticipate(false);
    } finally {
      setMembershipChecked(true);
    }
  };

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
                                  {findCriterionValue(opt, factor)}
                                </td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}

                  {membershipChecked && !canParticipate && (
                    <div className="membership-blocked-notice">
                      <p>
                        🔒 This is a private community decision. You must be an approved
                        member of <strong>{decision.communityName}</strong> to vote, rate,
                        or comment.
                      </p>
                    </div>
                  )}

                  {decision.votingType === "RATING_BASED" ? (
                    <RatingPanel
                      decision={decision}
                      pollOpen={
                        canParticipate &&
                        (decision.poll?.status === "OPEN" || decision.status === "ACTIVE")
                      }
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
                                disabled={voting || !canParticipate}
                                onClick={() => handleVote(opt.id)}
                                title={
                                  !canParticipate
                                    ? "You must be an approved community member to vote"
                                    : undefined
                                }
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
                  <PollResultsPanel decision={decision} />
                </div>
              )}

              {activeTab === "edit-board" && (
                <div className="detail-tab-content">
                  <EditBoardPanel
                    decision={decision}
                    onSaved={(updated) => {
                      setDecision(updated);
                      setActiveTab("overview");
                    }}
                    onCancel={() => setActiveTab("overview")}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
