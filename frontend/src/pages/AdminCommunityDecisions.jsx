import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaChevronLeft, FaThumbtack, FaLock, FaUnlock, FaComments, FaTrash, FaExclamationTriangle } from "react-icons/fa";

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

  useEffect(() => {
    fetchData();
  }, [communityId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [commRes, decisionsRes] = await Promise.all([
        api.get(`/api/communities/${communityId}`),
        api.get(`/api/decisions?communityId=${communityId}`)
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
            const pollRes = await api.get(`/api/decisions/${d.id}/poll`);
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
      const endpoint = `/api/moderation/decisions/${decision.id}/${decision.pinned ? "unpin" : "pin"}`;
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
      await api.delete(`/api/decisions/${decisionToDelete.id}`);
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
      const endpoint = `/api/moderation/decisions/${decision.id}/${decision.locked ? "unlock" : "lock"}`;
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
    </div>
  );
}
