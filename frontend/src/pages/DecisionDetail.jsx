import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import RatingPanel from "../components/RatingPanel";
import PollResultsPanel from "../components/PollResultsPanel";
import EditBoardPanel from "../components/EditBoardPanel";
import DecisionModerationControls from "../components/moderator/DecisionModerationControls";
import { getModeratingCommunities } from "../services/moderationService";
import { getCommunities, getMembers } from "../services/communityService";
import { useToast } from "../components/Toast";
import "../styles/DecisionDetail.css";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}` }
});

// Options store values in comparisonScores keyed by factorId, one entry per
// voter (each entry also carries a userId). Showing a single raw entry would
// leak one specific person's individual rating to everyone viewing this page —
// instead we average all submitted scores for that factor/option, which is
// the correct aggregate view for the Overview/analytics screen. Individual
// scores stay private to the voter who submitted them (see RatingPanel).
const findCriterionValue = (option, factor) => {
  const matches = (option.comparisonScores || []).filter((s) => s.factorId === factor.id);
  if (matches.length === 0) return "—";
  const avg = matches.reduce((sum, s) => sum + (s.score || 0), 0) / matches.length;
  return Math.round(avg * 10) / 10;
};

export default function DecisionDetail() {
  const { id: decisionId } = useParams();
  const { addToast } = useToast();
  const [decision, setDecision] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [activeTab, setActiveTab] = useState("overview");

  // Voting state for SINGLE_CHOICE / MULTIPLE_CHOICE
  const [myVoteOptionIds, setMyVoteOptionIds] = useState([]);
  const [voting, setVoting] = useState(false);
  // MULTIPLE_CHOICE only: checkbox selections staged here until the user
  // clicks Submit — unlike SINGLE_CHOICE, which submits instantly on click.
  const [pendingSelection, setPendingSelection] = useState(null);

  // Communities the logged-in user moderates — used to check whether they can
  // pin/lock THIS decision. The DecisionResponse only returns communityName
  // (no communityId), so we match by name against this list.
  const [moderatingCommunities, setModeratingCommunities] = useState([]);

  // Community membership gate: decisions with a communityName require the
  // viewer to be an APPROVED member of that community to vote/rate.
  // Decisions with no community (communityName null) are open to everyone.
  const [canParticipate, setCanParticipate] = useState(true);
  const [membershipChecked, setMembershipChecked] = useState(false);

  useEffect(() => {
    fetchDecision();
    fetchMyVote();
    fetchModeratingCommunities();
    try {
      const t = token();
      const payload = JSON.parse(atob(t.split(".")[1]));
      setCurrentUserId(payload.id);
    } catch (err) {
      console.error("Failed to decode token:", err);
    }
  }, [decisionId]);

  const fetchModeratingCommunities = async () => {
    try {
      const data = await getModeratingCommunities();
      setModeratingCommunities(data || []);
    } catch (err) {
      // Not being a moderator of anything is a normal state, not an error.
      setModeratingCommunities([]);
    }
  };

  // Live vote counts: poll the decision every 5s while the poll is still open,
  // so other users' votes/ratings show up without a manual reload.
  useEffect(() => {
    if (!decision) return;
    const pollOpen = decision.poll?.status === "OPEN" || decision.status === "ACTIVE";
    if (!pollOpen) return;

    const intervalId = setInterval(() => {
      fetchDecision(true);
    }, 5000);

    return () => clearInterval(intervalId);
  }, [decisionId, decision?.status, decision?.poll?.status]);

  // Check community membership once we have both the decision and the
  // current user's ID. Decisions with no community are open to everyone;
  // decisions with a community require an APPROVED membership record.
  useEffect(() => {
    if (!decision || !currentUserId) return;

    if (!decision.communityName) {
      setCanParticipate(true);
      setMembershipChecked(true);
      return;
    }

    checkMembership();
  }, [decision?.communityName, currentUserId]);

  const checkMembership = async () => {
    try {
      setMembershipChecked(false);
      // DecisionResponse only gives us communityName (no communityId), so
      // find the matching community by name first to get its real ID.
      const allCommunities = await getCommunities();
      const match = (allCommunities || []).find((c) => c.name === decision.communityName);
      if (!match) {
        // Community not found (edge case) — fail closed, block participation.
        setCanParticipate(false);
        return;
      }
      const membersRes = await getMembers(match.id);
      const members = membersRes.data || [];
      const myMembership = members.find((m) => String(m.userId) === String(currentUserId));
      setCanParticipate(!!myMembership && myMembership.status === "APPROVED");
    } catch (err) {
      console.error("Failed to check community membership:", err);
      // Fail closed on error — don't accidentally allow voting when we
      // couldn't actually confirm membership.
      setCanParticipate(false);
    } finally {
      setMembershipChecked(true);
    }
  };

  // silent=true is used for background polling refreshes so they update the
  // data without toggling `loading` and re-flashing the whole page.
  const fetchDecision = async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      const res = await axios.get(`${API}/decisions/${decisionId}`, headers());
      const d = res.data;
      if (d && d.description) {
        const match = d.description.match(/^\[Cat:([^\]]+)\]\s*(.*)/s);
        if (match) {
          d.categoryName = match[1];
          d.description = match[2];
        }
      }
      setDecision(d);
    } catch (err) {
      console.error("Failed to fetch decision:", err);
      if (!silent) {
        if (err.response?.status === 403) {
          setError(
            "This is a private decision — you must be an approved member of its community to view it."
          );
        } else {
          setError("Could not load this decision.");
        }
      }
    } finally {
      if (!silent) setLoading(false);
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

  // SINGLE_CHOICE: called the instant a radio button is selected — submits
  // immediately, no separate button needed.
  const handleSingleChoiceVote = async (optionId) => {
    if (voting || !canParticipate) return;
    const isFirstVote = myVoteOptionIds.length === 0;
    setVoting(true);
    try {
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds: [optionId] },
        headers()
      );
      setMyVoteOptionIds([optionId]);
      addToast(isFirstVote ? "Vote submitted successfully." : "Vote updated successfully.", "success");
      await fetchDecision();
    } catch (err) {
      console.error("Failed to submit vote:", err);
      addToast("Failed to submit vote.", "error");
    } finally {
      setVoting(false);
    }
  };

  // MULTIPLE_CHOICE: checkbox toggles only update the staged local selection.
  const toggleMultipleChoiceOption = (optionId) => {
    if (!canParticipate) return;
    const current = pendingSelection ?? myVoteOptionIds;
    const next = current.includes(optionId)
      ? current.filter((id) => id !== optionId)
      : [...current, optionId];
    setPendingSelection(next);
  };

  // MULTIPLE_CHOICE: explicit Submit button sends the staged selection.
  const submitMultipleChoiceVote = async () => {
    if (voting || !canParticipate) return;
    const isFirstVote = myVoteOptionIds.length === 0;
    const optionIds = pendingSelection ?? myVoteOptionIds;
    setVoting(true);
    try {
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        { optionIds },
        headers()
      );
      setMyVoteOptionIds(optionIds);
      setPendingSelection(null);
      addToast(isFirstVote ? "Vote submitted successfully." : "Vote updated successfully.", "success");
      await fetchDecision();
    } catch (err) {
      console.error("Failed to submit vote:", err);
      addToast("Failed to submit vote.", "error");
    } finally {
      setVoting(false);
    }
  };

  // Pin/lock controls belong to the COMMUNITY MODERATOR, not the decision's
  // creator. A decision only has a communityName (string) from the backend,
  // so we match it against the names of communities this user moderates.
  // Decisions with no community (Personal/Public) have no community moderator,
  // so no one gets pin/lock controls for them via this check.
  const isModerator =
    !!decision?.communityName &&
    moderatingCommunities.some((c) => c.name === decision.communityName);

  const hasCriteria = decision?.factors && decision.factors.length > 0;

  // Edit Board access: the decision's CREATOR, or the COMMUNITY MODERATOR of
  // the community this decision belongs to (if any). Regular members/voters
  // never get edit access, even if they're viewing their own community's decision.
  const isCreator =
    decision?.creator && String(decision.creator.id) === String(currentUserId);
  const canEdit = isCreator || isModerator;

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

              {/* Tabs — "Edit Board" shown to the decision's creator OR the
                  community moderator of the community it belongs to. */}
              <div className="detail-tabs">
                {["overview", "discussion", "poll results"]
                  .concat(canEdit ? ["edit board"] : [])
                  .map((tab) => {
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
                    <div className="membership-required-notice">
                      <p>
                        You must be an approved member of this community to vote, rate, or
                        comment on this decision.
                      </p>
                    </div>
                  )}

                  {decision.votingType === "RATING_BASED" ? (
                    <RatingPanel
                      decision={decision}
                      currentUserId={currentUserId}
                      pollOpen={
                        (decision.poll?.status === "OPEN" || decision.status === "ACTIVE") &&
                        canParticipate
                      }
                      onScoreSubmitted={fetchDecision}
                    />
                  ) : (
                    <div className="available-options">
                      <h3>{decision.title}</h3>
                      <div className="vote-question-list" style={{ display: "flex", flexDirection: "column", gap: "0.75rem", marginTop: "1rem", width: "100%" }}>
                        {decision.votingType === "SINGLE_CHOICE"
                          ? decision.options.map((opt) => {
                              const isSelected = myVoteOptionIds.includes(opt.id);
                              return (
                                <div
                                  key={opt.id}
                                  onClick={() => !voting && canParticipate && handleSingleChoiceVote(opt.id)}
                                  style={{
                                    display: "flex",
                                    flexDirection: "row",
                                    alignItems: "center",
                                    gap: "0.6rem",
                                    cursor: canParticipate ? "pointer" : "default",
                                    width: "100%",
                                    float: "none",
                                    position: "static",
                                    textAlign: "left"
                                  }}
                                >
                                  <input
                                    type="radio"
                                    name={`decision-${decision.id}-choice`}
                                    checked={isSelected}
                                    disabled={voting || !canParticipate}
                                    onChange={() => {}} // No-op: click is handled by parent div's onClick
                                    style={{ flexShrink: 0, margin: 0, position: "static", float: "none", width: "18px", height: "18px" }}
                                  />
                                  <span style={{ flex: "initial", textAlign: "left" }}>
                                    <strong>{opt.title}</strong>
                                    {opt.description && <span> — {opt.description}</span>}
                                  </span>
                                </div>
                              );
                            })
                          : decision.options.map((opt) => {
                              const current = pendingSelection ?? myVoteOptionIds;
                              const isSelected = current.includes(opt.id);
                              return (
                                <div
                                  key={opt.id}
                                  onClick={() => canParticipate && toggleMultipleChoiceOption(opt.id)}
                                  style={{
                                    display: "flex",
                                    flexDirection: "row",
                                    alignItems: "center",
                                    gap: "0.6rem",
                                    cursor: canParticipate ? "pointer" : "default",
                                    width: "100%",
                                    float: "none",
                                    position: "static",
                                    textAlign: "left"
                                  }}
                                >
                                  <input
                                    type="checkbox"
                                    checked={isSelected}
                                    disabled={voting || !canParticipate}
                                    onChange={() => {}} // No-op: click is handled by parent div's onClick
                                    style={{ flexShrink: 0, margin: 0, position: "static", float: "none", width: "18px", height: "18px" }}
                                  />
                                  <span style={{ flex: "initial", textAlign: "left" }}>
                                    <strong>{opt.title}</strong>
                                    {opt.description && <span> — {opt.description}</span>}
                                  </span>
                                </div>
                              );
                            })}
                      </div>

                      {/* Only MULTIPLE_CHOICE needs an explicit submit — SINGLE_CHOICE
                          submits instantly the moment a radio button is selected. */}
                      {decision.votingType === "MULTIPLE_CHOICE" && (
                        <button
                          className="btn-primary"
                          disabled={voting || !canParticipate || pendingSelection === null}
                          onClick={submitMultipleChoiceVote}
                        >
                          {voting ? "Submitting..." : "Submit Vote"}
                        </button>
                      )}
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

              {activeTab === "edit-board" && canEdit && (
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