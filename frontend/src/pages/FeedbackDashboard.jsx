import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StarRating from "../components/StarRating";
import EmptyState from "../components/EmptyState";
import LoadingState from "../components/LoadingState";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaExclamationTriangle, FaRedo } from "react-icons/fa";
import "../styles/Feedback.css";
import "../styles/DecisionList.css";

export default function FeedbackDashboard() {
  const { addToast } = useToast();
  const [activeTab, setActiveTab] = useState("support-tickets");
  const [supportTickets, setSupportTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Detail Modal State
  const [ticketDetailsModalOpen, setTicketDetailsModalOpen] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [updatingStatus, setUpdatingStatus] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get("/api/admin/support");
      const sortedSupport = (res.data || []).sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );

      setSupportTickets(sortedSupport);
    } catch (err) {
      console.error("Failed to load admin dashboard data:", err);
      setError("Failed to fetch support tickets from backend servers.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRetry = () => {
    loadData();
  };

  const handleViewTicket = async (ticket) => {
    try {
      const res = await api.get(`/api/admin/support/${ticket.id}`);
      setSelectedTicket(res.data);
      setSelectedStatus(res.data.status || "OPEN");
      setTicketDetailsModalOpen(true);
    } catch (err) {
      addToast("Failed to retrieve ticket details.", "error");
    }
  };

  const handleStatusChange = async (ticketId, newStatus) => {
    setUpdatingStatus(true);
    try {
      const res = await api.patch(`/api/admin/support/${ticketId}/status`, {
        status: newStatus,
      });
      addToast(`Ticket status updated to ${newStatus} successfully!`, "success");
      setSelectedTicket(res.data);
      setSelectedStatus(res.data.status || newStatus);
      setSupportTickets((prev) =>
        prev.map((t) => (t.id === ticketId ? res.data : t))
      );
    } catch (err) {
      const errMsg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Failed to update ticket status.";
      addToast(errMsg, "error");
      setSelectedStatus(selectedTicket?.status || "OPEN");
    } finally {
      setUpdatingStatus(false);
    }
  };

  const formatTicketType = (type) => {
    if (type === "BUG_REPORT") return "Bug Report";
    if (type === "SUGGESTION") return "Suggestion";
    if (type === "GENERAL_FEEDBACK") return "General Feedback";
    return type;
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="feedback-dashboard-header">
            <div className="feedback-dashboard-title">
              <h1>Feedback Dashboard</h1>
              <p>Review submitted user feedback and manage support tickets.</p>
            </div>
            <div>
              <button
                className="btn-secondary"
                onClick={loadData}
                style={{ fontSize: "0.85rem", padding: "0.5rem 1rem" }}
              >
                Refresh Data
              </button>
            </div>
          </div>

          {/* Admin Tabs */}
          <div className="feedback-tabs">
            <button
              className={`feedback-tab-btn ${activeTab === "support-tickets" ? "active" : ""}`}
              onClick={() => setActiveTab("support-tickets")}
            >
              Support Tickets
            </button>
          </div>

          {loading ? (
            <LoadingState />
          ) : error ? (
            <div className="empty-state glass-panel animate-fade-in">
              <div className="empty-state-icon" style={{ color: "var(--danger)" }}>
                <FaExclamationTriangle />
              </div>
              <h3>Loading Failed</h3>
              <p style={{ marginBottom: "1.5rem" }}>{error}</p>
              <button className="btn-primary" onClick={handleRetry}>
                <FaRedo /> Retry Loading
              </button>
            </div>
          ) : supportTickets.length === 0 ? (
            <EmptyState message="No support tickets have been submitted yet." />
          ) : (
            <div className="decision-table-wrapper glass-panel animate-fade-in">
              <table className="decision-table-element">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Type</th>
                    <th>Subject</th>
                    <th>Description</th>
                    <th>Rating</th>
                    <th>Status</th>
                    <th>Created Date</th>
                  </tr>
                </thead>
                <tbody>
                  {supportTickets.map((ticket) => (
                    <tr
                      key={ticket.id}
                      onClick={() => handleViewTicket(ticket)}
                      style={{ cursor: "pointer" }}
                      title="Click to view details"
                    >
                      <td>{ticket.userName || `User #${ticket.userId}`}</td>
                      <td style={{ fontWeight: 600 }}>{formatTicketType(ticket.type)}</td>
                      <td>{ticket.subject || "—"}</td>
                      <td
                        style={{
                          maxWidth: "200px",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                          whiteSpace: "nowrap",
                        }}
                      >
                        {ticket.description || "—"}
                      </td>
                      <td>
                        {ticket.type === "GENERAL_FEEDBACK" && ticket.rating ? (
                          <StarRating rating={ticket.rating} readOnly={true} />
                        ) : (
                          "—"
                        )}
                      </td>
                      <td>
                        {ticket.type === "BUG_REPORT" ? (
                          <span className={`status-badge ${ticket.status?.toLowerCase()}`}>
                            {ticket.status}
                          </span>
                        ) : (
                          <span className="status-badge na">N/A</span>
                        )}
                      </td>
                      <td>{new Date(ticket.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Ticket Details Overlay Modal */}
      {ticketDetailsModalOpen && selectedTicket && (
        <div className="feedback-overlay">
          <div
            className="feedback-dialog glass-panel animate-pop-in"
            style={{ textAlign: "left", alignItems: "stretch", width: "500px", gap: "0" }}
          >
            <h2 style={{ marginBottom: "1.5rem", fontSize: "1.35rem" }}>Ticket Details</h2>

            <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginBottom: "1.75rem" }}>
              <div className="ticket-detail-item">
                <span className="feedback-section-label">User</span>
                <p className="feedback-text" style={{ fontWeight: 600 }}>
                  {selectedTicket.userName || `User #${selectedTicket.userId}`}
                </p>
              </div>

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Type</span>
                <p className="feedback-text" style={{ fontWeight: 600 }}>
                  {formatTicketType(selectedTicket.type)}
                </p>
              </div>

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Subject</span>
                <p className="feedback-text">{selectedTicket.subject || "—"}</p>
              </div>

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Description</span>
                <p className="feedback-text" style={{ maxHeight: "120px", overflowY: "auto" }}>
                  {selectedTicket.description || "—"}
                </p>
              </div>

              {selectedTicket.type === "GENERAL_FEEDBACK" && selectedTicket.rating && (
                <div className="ticket-detail-item">
                  <span className="feedback-section-label">Rating</span>
                  <div style={{ marginTop: "0.25rem" }}>
                    <StarRating rating={selectedTicket.rating} readOnly={true} />
                  </div>
                </div>
              )}

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Status</span>
                <div style={{ marginTop: "0.25rem" }}>
                  {selectedTicket.type === "BUG_REPORT" ? (
                    <span className={`status-badge ${selectedTicket.status?.toLowerCase()}`}>
                      {selectedTicket.status}
                    </span>
                  ) : (
                    <span className="status-badge na">N/A</span>
                  )}
                </div>
              </div>

              {/* Bug Report status update dropdown */}
              {selectedTicket.type === "BUG_REPORT" && (
                <div className="feedback-form-group" style={{ marginTop: "0.5rem" }}>
                  <label htmlFor="status-select">Update Status</label>
                  <div style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
                    <select
                      id="status-select"
                      value={selectedStatus}
                      onChange={(e) => setSelectedStatus(e.target.value)}
                      disabled={updatingStatus}
                      style={{ flex: 1 }}
                    >
                      <option value="OPEN">OPEN</option>
                      <option value="IN_PROGRESS">IN_PROGRESS</option>
                      <option value="RESOLVED">RESOLVED</option>
                    </select>
                    <button
                      type="button"
                      className="btn-primary"
                      onClick={() => handleStatusChange(selectedTicket.id, selectedStatus)}
                      disabled={updatingStatus || selectedStatus === selectedTicket.status}
                      style={{ whiteSpace: "nowrap" }}
                    >
                      {updatingStatus ? "Saving..." : "Save"}
                    </button>
                  </div>
                </div>
              )}

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Created Date</span>
                <p className="feedback-text">{new Date(selectedTicket.createdAt).toLocaleString()}</p>
              </div>

              <div className="ticket-detail-item">
                <span className="feedback-section-label">Last Updated</span>
                <p className="feedback-text">
                  {new Date(selectedTicket.updatedAt || selectedTicket.createdAt).toLocaleString()}
                </p>
              </div>
            </div>

            <button className="btn-primary" onClick={() => setTicketDetailsModalOpen(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
