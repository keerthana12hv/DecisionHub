import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaChevronLeft, FaThumbtack, FaLock, FaUnlock, FaComments, FaTrash, FaExclamationTriangle, FaEye } from "react-icons/fa";

export default function AdminCommunityDecisions() {
  const { id: communityId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [community, setCommunity] = useState(null);
  const [decisions, setDecisions] = useState([]);
  const [pollStatuses, setPollStatuses] = useState({});
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [decisionToDelete, setDecisionToDelete] = useState(null);
  const [decisionToView, setDecisionToView] = useState(null);
  const [viewDetails, setViewDetails] = useState(null);
  const [viewLoading, setViewLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, [communityId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [commRes, decisionsRes] = await Promise.all([
        api.get(`/communities/${communityId}`),
        api.get(`/decisions?communityId=${communityId}`)
      ]);
      setCommunity(commRes.data);
      const decisionsData = decisionsRes.data || [];
      const sorted = [...decisionsData].sort((a, b) => (a.pinned && !b.pinned ? -1 : !a.pinned && b.pinned ? 1 : 0));
      setDecisions(sorted);

      // Fetch poll statuses in parallel
      const pollStatusesMap = {};
      await Promise.all(
        decisionsData.map(async (d) => {
          try {
            const pollRes = await api.get(`/decisions/${d.id}/poll`);
            const p = pollRes.data;
            const isExpired = p?.endTime ? (new Date() >= new Date(p.endTime)) : false;
            pollStatusesMap[d.id] = (p?.status === "OPEN" && !isExpired) ? "OPEN" : "CLOSED";
          } catch (err) {
            pollStatusesMap[d.id] = "—"; // for drafts/no active polls
          }
        })
      );
      setPollStatuses(pollStatusesMap);
    } catch (err) {
      console.error("Failed to load community decisions:", err);
      addToast("Failed to load decisions", "error");
    } finally {
      setLoading(false);
    }
  };

  const handlePinToggle = async (decision) => {
    try {
      setActionLoading(true);
      const endpoint = `/moderation/decisions/${decision.id}/${decision.pinned ? "unpin" : "pin"}`;
      await api.put(endpoint, {});
      addToast(decision.pinned ? "Decision unpinned" : "Decision pinned", "success");

      // Update decision locally from confirmed backend state and re-sort
      setDecisions((prev) => {
        const updated = prev.map((d) => (d.id === decision.id ? { ...d, pinned: !decision.pinned } : d));
        return updated.sort((a, b) => (a.pinned && !b.pinned ? -1 : !a.pinned && b.pinned ? 1 : 0));
      });
    } catch (err) {
      console.error("Failed to update pin state:", err);
      addToast("Failed to update pin state", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteDecision = async () => {
    if (!decisionToDelete) return;
    try {
      setActionLoading(true);
      await api.delete(`/decisions/${decisionToDelete.id}`);
      addToast("Decision deleted successfully", "success");
      setDecisions((prev) => prev.filter((d) => d.id !== decisionToDelete.id));
      setDecisionToDelete(null);
    } catch (err) {
      console.error("Failed to delete decision:", err);
      addToast(err.response?.data?.message || "Failed to delete decision", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleLockToggle = async (decision) => {
    try {
      setActionLoading(true);
      const endpoint = `/moderation/decisions/${decision.id}/${decision.locked ? "unlock" : "lock"}`;
      await api.put(endpoint, {});
      addToast(decision.locked ? "Discussion unlocked" : "Discussion locked", "success");

      // Update decision locally from confirmed backend state
      setDecisions((prev) =>
        prev.map((d) => (d.id === decision.id ? { ...d, locked: !decision.locked } : d))
      );
    } catch (err) {
      console.error("Failed to update lock state:", err);
      addToast("Failed to update lock state", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleViewDecision = async (decision) => {
    setDecisionToView(decision);
    setViewLoading(true);
    setViewDetails(null);
    try {
      const detailRes = await api.get(`/decisions/${decision.id}`);
      const d = detailRes.data;

      if (d && d.description) {
        const match = d.description.match(/^\[Cat:([^\]]+)\]\s*(.*)/s);
        if (match) {
          d.categoryName = match[1];
          d.description = match[2];
        }
      }

      let results = null;
      if (d.status !== "DRAFT") {
        try {
          if (d.votingType === "RATING_BASED") {
            const rankingRes = await api.get(`/decisions/${decision.id}/ranking`);
            results = rankingRes.data?.options || [];
          } else {
            const distRes = await api.get(`/analytics/decisions/${decision.id}/distribution`);
            results = distRes.data || [];
          }
        } catch (err) {
          console.error("Failed to load decision results:", err);
        }
      }

      setViewDetails({
        decision: d,
        results
      });
    } catch (err) {
      console.error("Failed to view decision details:", err);
      addToast("Failed to load decision details", "error");
      setDecisionToView(null);
    } finally {
      setViewLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Loading community decisions...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!community) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Community not found.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="decision-page" style={{ maxWidth: "1200px", margin: "0 auto" }}>

            {/* Header section with back button */}
            <div className="decision-header" style={{ marginBottom: "2rem" }}>
              <div>
                <button
                  onClick={() => navigate(`/communities/${communityId}`)}
                  style={{
                    background: "none",
                    border: "none",
                    color: "var(--accent-purple, #A78BFA)",
                    cursor: "pointer",
                    display: "flex",
                    alignItems: "center",
                    gap: "6px",
                    marginBottom: "1rem",
                    fontSize: "0.95rem",
                    fontWeight: "600"
                  }}
                >
                  <FaChevronLeft /> Back to Workspace
                </button>
                <h1>{community.name} Decisions</h1>
                <p>Manage and moderate decisions belonging to this community.</p>
              </div>
            </div>

            {/* Decisions Table */}
            <div className="decision-table-wrapper glass-panel">
              {decisions.length === 0 ? (
                <p style={{ padding: "20px" }}>No decisions found</p>
              ) : (
                <table className="decision-table-element">
                  <thead>
                    <tr>
                      <th style={{ textAlign: "left" }}>Decision</th>
                      <th style={{ textAlign: "left" }}>Creator</th>
                      <th style={{ textAlign: "left" }}>Status</th>
                      <th style={{ textAlign: "left" }}>Poll Status</th>
                      <th style={{ textAlign: "left" }}>Visibility</th>
                      <th style={{ textAlign: "center" }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {decisions.map((decision) => (
                      <tr key={decision.id}>
                        <td>
                          <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                            {decision.pinned && <FaThumbtack style={{ color: "#FBBF24" }} title="Pinned" />}
                            {decision.locked && <FaLock style={{ color: "#F87171" }} title="Locked" />}
                            <span style={{ fontWeight: "600" }}>{decision.title}</span>
                          </div>
                        </td>
                        <td>{decision.creator?.username || "—"}</td>
                        <td>
                          <span className={`status-badge ${decision.status?.toLowerCase()}`}>
                            {decision.status}
                          </span>
                        </td>
                        <td>
                          <span className={`status-badge ${pollStatuses[decision.id] === "OPEN" ? "active" : "draft"}`}>
                            {pollStatuses[decision.id] || "—"}
                          </span>
                        </td>
                        <td>
                          <span className="category-tag" style={{ textTransform: "uppercase" }}>
                            {community.visibility}
                          </span>
                        </td>
                        <td style={{ textAlign: "right", whiteSpace: "nowrap" }}>
                          <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", whiteSpace: "nowrap" }}>

                            {/* View Discussion */}
                            <Link to={`/admin/decisions/${decision.id}/discuss`} state={{ communityId }} style={{ textDecoration: "none" }}>
                              <button
                                className="btn-primary"
                                style={{
                                  padding: "6px 10px",
                                  fontSize: "0.8rem",
                                  display: "flex",
                                  alignItems: "center",
                                  justifyContent: "center",
                                  gap: "4px",
                                  whiteSpace: "nowrap"
                                }}
                              >
                                <FaComments /> View Discussion
                              </button>
                            </Link>

                            {/* View Decision */}
                            <button
                              onClick={() => handleViewDecision(decision)}
                              className="btn-primary"
                              style={{
                                padding: "6px 10px",
                                fontSize: "0.8rem",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                gap: "4px",
                                whiteSpace: "nowrap"
                              }}
                            >
                              <FaEye /> View Decision
                            </button>

                            {/* Delete Decision */}
                            <button
                              onClick={() => setDecisionToDelete(decision)}
                              disabled={actionLoading}
                              className="btn-danger"
                              style={{
                                width: "44px",
                                height: "44px",
                                padding: "0",
                                flexShrink: 0,
                                background: "rgba(239, 68, 68, 0.15)",
                                color: "#F87171",
                                border: "1px solid rgba(239, 68, 68, 0.4)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                cursor: "pointer"
                              }}
                              title="Delete Decision"
                            >
                              <FaTrash />
                            </button>

                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

          </div>
        </div>
      </div>

      {/* Delete Decision Confirmation Modal */}
      {decisionToDelete && (
        <div className="delete-overlay">
          <div className="delete-modal glass-panel animate-pop-in">
            <div className="delete-warning-icon">
              <FaExclamationTriangle />
            </div>
            <h2>Delete Decision?</h2>
            <p>
              Are you sure you want to delete this decision?
            </p>
            <div className="delete-buttons">
              <button
                className="btn-secondary"
                onClick={() => setDecisionToDelete(null)}
                disabled={actionLoading}
              >
                Cancel
              </button>
              <button
                className="btn-primary confirm-delete-btn"
                onClick={handleDeleteDecision}
                disabled={actionLoading}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* View Decision Details Modal */}
      {decisionToView && (
        <div className="delete-overlay" style={{ overflowY: "auto", padding: "2rem 0" }}>
          <div className="glass-panel animate-pop-in" style={{
            width: "600px",
            maxWidth: "95%",
            padding: "2rem",
            margin: "auto",
            position: "relative",
            display: "flex",
            flexDirection: "column",
            maxHeight: "90vh",
            overflow: "hidden"
          }}>
            <button
              onClick={() => setDecisionToView(null)}
              style={{
                position: "absolute",
                top: "1rem",
                right: "1rem",
                background: "none",
                border: "none",
                color: "var(--text-secondary)",
                fontSize: "1.5rem",
                cursor: "pointer",
                padding: "0.25rem",
                lineHeight: 1
              }}
              title="Close"
            >
              &times;
            </button>

            <h3 style={{ margin: "0 0 1.5rem 0", color: "var(--accent-purple, #A78BFA)", fontSize: "1.2rem", fontWeight: "600" }}>
              Decision Details
            </h3>

            {viewLoading ? (
              <p style={{ padding: "20px", textAlign: "center" }}>Loading details...</p>
            ) : viewDetails ? (
              <div style={{ overflowY: "auto", flex: 1, paddingRight: "0.5rem", textAlign: "left" }}>
                <h2 style={{ margin: "0 0 1rem 0", color: "var(--text-primary)", fontSize: "1.5rem" }}>
                  {viewDetails.decision.title}
                </h2>

                {/* Status elements */}
                <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", marginBottom: "1.5rem" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                    <span style={{ fontSize: "0.85rem", color: "var(--text-secondary)" }}>Decision:</span>
                    <span className={`status-badge ${viewDetails.decision.status?.toLowerCase()}`}>
                      {viewDetails.decision.status}
                    </span>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                    <span style={{ fontSize: "0.85rem", color: "var(--text-secondary)" }}>Poll:</span>
                    <span className={`status-badge ${pollStatuses[viewDetails.decision.id] === "OPEN" ? "active" : "draft"}`}>
                      {pollStatuses[viewDetails.decision.id] || "—"}
                    </span>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                    <span style={{ fontSize: "0.85rem", color: "var(--text-secondary)" }}>Type:</span>
                    <span className="category-tag" style={{ textTransform: "uppercase", display: "inline-block", padding: "2px 8px", fontSize: "0.75rem", borderRadius: "4px" }}>
                      {viewDetails.decision.votingType?.replace("_", " ")}
                    </span>
                  </div>
                </div>

                {/* Description */}
                <div style={{
                  marginBottom: "1.5rem",
                  padding: "1rem",
                  borderRadius: "8px",
                  background: "rgba(255, 255, 255, 0.02)",
                  border: "1px solid var(--border-glass)"
                }}>
                  <h4 style={{ margin: "0 0 0.5rem 0", color: "var(--text-secondary)", fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                    Description & Context
                  </h4>
                  <p style={{ margin: 0, fontSize: "0.95rem", color: "var(--text-primary)", whiteSpace: "pre-wrap", lineHeight: "1.5" }}>
                    {viewDetails.decision.description || "No description provided."}
                  </p>
                </div>

                {/* Options List */}
                <div style={{ marginBottom: "1.5rem" }}>
                  <h4 style={{ margin: "0 0 0.75rem 0", color: "var(--text-secondary)", fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                    Options
                  </h4>
                  <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                    {viewDetails.decision.options && viewDetails.decision.options.length > 0 ? (
                      viewDetails.decision.options.map((opt) => (
                        <div
                          key={opt.id}
                          style={{
                            padding: "0.75rem 1rem",
                            borderRadius: "6px",
                            background: "rgba(255, 255, 255, 0.01)",
                            border: "1px solid var(--border-glass)"
                          }}
                        >
                          <strong style={{ color: "var(--text-primary)" }}>{opt.title}</strong>
                          {opt.description && (
                            <div style={{ color: "var(--text-secondary)", fontSize: "0.85rem", marginTop: "2px" }}>
                              {opt.description}
                            </div>
                          )}
                        </div>
                      ))
                    ) : (
                      <p style={{ margin: 0, fontSize: "0.9rem", color: "var(--text-secondary)", fontStyle: "italic" }}>
                        No options defined.
                      </p>
                    )}
                  </div>
                </div>

                {/* Results/Outcome */}
                <div style={{ marginBottom: "1rem" }}>
                  <h4 style={{ margin: "0 0 0.75rem 0", color: "var(--text-secondary)", fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                    Results & Outcome
                  </h4>
                  {!viewDetails.results || viewDetails.results.length === 0 ? (
                    <p style={{ margin: 0, fontSize: "0.9rem", color: "var(--text-secondary)", fontStyle: "italic" }}>
                      No votes/ratings have been submitted yet.
                    </p>
                  ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
                      {viewDetails.decision.votingType === "RATING_BASED"
                        ? [...viewDetails.results]
                            .sort((a, b) => b.score - a.score)
                            .map((resItem, idx) => {
                              const maxScore = Math.max(...viewDetails.results.map(r => r.score)) || 1;
                              const pct = (resItem.score / maxScore) * 100;
                              return (
                                <div key={resItem.optionId} style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.9rem" }}>
                                    <span style={{ color: "var(--text-primary)" }}>
                                      <strong style={{ marginRight: "4px" }}>#{idx + 1}</strong> {resItem.optionTitle}
                                    </span>
                                    <span style={{ color: "var(--accent-purple, #A78BFA)", fontWeight: "600" }}>
                                      {resItem.score.toFixed(1)} pts
                                    </span>
                                  </div>
                                  <div style={{ height: "6px", borderRadius: "3px", background: "rgba(255, 255, 255, 0.05)", overflow: "hidden" }}>
                                    <div style={{ width: `${pct}%`, height: "100%", background: "var(--gradient-primary)" }} />
                                  </div>
                                </div>
                              );
                            })
                        : [...viewDetails.results]
                            .sort((a, b) => b.voteCount - a.voteCount)
                            .map((resItem, idx) => {
                              const totalVotes = viewDetails.results.reduce((sum, r) => sum + r.voteCount, 0) || 1;
                              const pct = (resItem.voteCount / totalVotes) * 100;
                              return (
                                <div key={resItem.optionId} style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.9rem" }}>
                                    <span style={{ color: "var(--text-primary)" }}>
                                      <strong style={{ marginRight: "4px" }}>#{idx + 1}</strong> {resItem.optionName}
                                    </span>
                                    <span style={{ color: "var(--accent-purple, #A78BFA)", fontWeight: "600" }}>
                                      {resItem.voteCount} {resItem.voteCount === 1 ? "vote" : "votes"} ({Math.round(pct)}%)
                                    </span>
                                  </div>
                                  <div style={{ height: "6px", borderRadius: "3px", background: "rgba(255, 255, 255, 0.05)", overflow: "hidden" }}>
                                    <div style={{ width: `${pct}%`, height: "100%", background: "var(--gradient-primary)" }} />
                                  </div>
                                </div>
                              );
                            })
                      }
                    </div>
                  )}
                </div>
              </div>
            ) : null}

            <div style={{ marginTop: "2rem", display: "flex", justifyContent: "flex-end" }}>
              <button
                className="btn-secondary"
                onClick={() => setDecisionToView(null)}
                style={{ minWidth: "100px" }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
