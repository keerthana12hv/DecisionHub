import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaThumbtack, FaLock, FaUnlock, FaComments, FaTrash, FaExclamationTriangle } from "react-icons/fa";

export default function AdminDecisions() {
  const { addToast } = useToast();

  const [decisions, setDecisions] = useState([]);
  const [pollStatuses, setPollStatuses] = useState({});
  const [loading, setLoading] = useState(true);
  const [decisionToDelete, setDecisionToDelete] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const decisionsRes = await api.get("/api/decisions");
        const allDecisions = decisionsRes.data || [];
        // Filter only PUBLIC decisions (where communityName is null or empty)
        const publicDecisions = allDecisions.filter((d) => !d.communityName);

        const sorted = [...publicDecisions].sort((a, b) => (a.pinned && !b.pinned ? -1 : !a.pinned && b.pinned ? 1 : 0));
        setDecisions(sorted);

        // Fetch poll statuses in parallel
        const pollStatusesMap = {};
        await Promise.all(
          publicDecisions.map(async (d) => {
            try {
              const pollRes = await api.get(`/api/decisions/${d.id}/poll`);
              pollStatusesMap[d.id] = pollRes.data?.status || "CLOSED";
            } catch (err) {
              console.warn(`Failed to fetch poll status for decision ${d.id}:`, err);
              pollStatusesMap[d.id] = "—";
            }
          })
        );
        setPollStatuses(pollStatusesMap);
      } catch (err) {
        console.error("Failed to fetch decisions data:", err);
        addToast("Failed to load decisions", "error");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [addToast]);

  const handlePinToggle = async (decision) => {
    try {
      setActionLoading(true);
      const endpoint = `/api/moderation/decisions/${decision.id}/${decision.pinned ? "unpin" : "pin"}`;
      await api.put(endpoint, {});
      addToast(decision.pinned ? "Decision unpinned" : "Decision pinned", "success");

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

  const handleLockToggle = async (decision) => {
    try {
      setActionLoading(true);
      const endpoint = `/api/moderation/decisions/${decision.id}/${decision.locked ? "unlock" : "lock"}`;
      await api.put(endpoint, {});
      addToast(decision.locked ? "Discussion unlocked" : "Discussion locked", "success");

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

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="decision-page" style={{ maxWidth: "1200px", margin: "0 auto" }}>

            {/* Header */}
            <div className="decision-header" style={{ marginBottom: "2rem" }}>
              <div>
                <h1>Public Decisions</h1>
                <p>Manage and moderate platform-level public decisions.</p>
              </div>
            </div>

            {/* Decisions Table */}
            <div className="decision-table-wrapper glass-panel" style={{ overflowX: "auto" }}>
              {loading ? (
                <p style={{ padding: "20px" }}>Loading decisions...</p>
              ) : decisions.length === 0 ? (
                <p style={{ padding: "20px" }}>No decisions found</p>
              ) : (
                <table className="decision-table-element" style={{ minWidth: "900px" }}>
                  <thead>
                    <tr>
                      <th style={{ padding: "12px 16px", textAlign: "left" }}>Decision</th>
                      <th style={{ padding: "12px 16px", textAlign: "left" }}>Creator</th>
                      <th style={{ padding: "12px 16px", textAlign: "left" }}>Status</th>
                      <th style={{ padding: "12px 16px", textAlign: "left" }}>Poll Status</th>
                      <th style={{ padding: "12px 16px", textAlign: "left" }}>Created Date</th>
                      <th style={{ padding: "12px 16px", textAlign: "center" }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {decisions.map((decision) => {
                      return (
                        <tr key={decision.id}>
                          <td style={{ padding: "12px 16px" }}>
                            <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                                {decision.pinned && <FaThumbtack style={{ color: "#FBBF24" }} title="Pinned" />}
                                {decision.locked && <FaLock style={{ color: "#F87171" }} title="Locked" />}
                                <span style={{ fontWeight: "600" }}>{decision.title}</span>
                              </div>
                              {decision.description && (
                                <span style={{ fontSize: "0.85rem", color: "rgba(255,255,255,0.6)", display: "block" }}>
                                  {decision.description}
                                </span>
                              )}
                            </div>
                          </td>
                          <td style={{ padding: "12px 16px" }}>{decision.creator?.username || "—"}</td>
                          <td style={{ padding: "12px 16px" }}>
                            <span className={`status-badge ${decision.status?.toLowerCase()}`}>
                              {decision.status}
                            </span>
                          </td>
                          <td style={{ padding: "12px 16px" }}>
                            <span className={`status-badge ${pollStatuses[decision.id] === "OPEN" ? "active" : "draft"}`}>
                              {pollStatuses[decision.id] || "—"}
                            </span>
                          </td>
                          <td style={{ padding: "12px 16px" }}>
                            {decision.createdAt ? new Date(decision.createdAt).toLocaleDateString() : "—"}
                          </td>
                          <td style={{ padding: "12px 16px", textAlign: "right", whiteSpace: "nowrap" }}>
                            <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", alignItems: "center", whiteSpace: "nowrap" }}>

                              {/* Pin / Unpin */}
                              <button
                                onClick={() => handlePinToggle(decision)}
                                disabled={actionLoading}
                                className="btn-secondary"
                                style={{
                                  padding: "6px 10px",
                                  fontSize: "0.8rem",
                                  display: "flex",
                                  alignItems: "center",
                                  gap: "4px",
                                  whiteSpace: "nowrap"
                                }}
                              >
                                <FaThumbtack /> {decision.pinned ? "Unpin Decision" : "Pin Decision"}
                              </button>

                              {/* Lock / Unlock */}
                              <button
                                onClick={() => handleLockToggle(decision)}
                                disabled={actionLoading}
                                className="btn-secondary"
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
                                {decision.locked ? <FaUnlock /> : <FaLock />} {decision.locked ? "Unlock Discussion" : "Lock Discussion"}
                              </button>

                              {/* View Discussion */}
                              <Link to={`/admin/decisions/${decision.id}/discuss`} state={{ fromAdminDecisions: true }} style={{ textDecoration: "none" }}>
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
                      );
                    })}
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
