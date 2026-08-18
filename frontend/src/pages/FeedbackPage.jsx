import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StarRating from "../components/StarRating";
import EmptyState from "../components/EmptyState";
import LoadingState from "../components/LoadingState";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import api from "../services/api";
import "../styles/Feedback.css";
import "../styles/DecisionList.css";
import { FaExclamationTriangle } from "react-icons/fa";

export default function FeedbackPage() {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState("bug-report");

  // Support Tickets State
  const [tickets, setTickets] = useState([]);
  const [loadingTickets, setLoadingTickets] = useState(false);
  const [ticketsError, setTicketsError] = useState("");
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [showDetails, setShowDetails] = useState(false);

  // Bug Report Form State
  const [bugSubject, setBugSubject] = useState("");
  const [bugDescription, setBugDescription] = useState("");
  const [submittingBug, setSubmittingBug] = useState(false);

  // Suggestion Form State
  const [suggestSubject, setSuggestSubject] = useState("");
  const [suggestDescription, setSuggestDescription] = useState("");
  const [submittingSuggest, setSubmittingSuggest] = useState(false);

  // General Feedback Form State
  const [gfRating, setGfRating] = useState(0);
  const [gfDescription, setGfDescription] = useState("");
  const [submittingGF, setSubmittingGF] = useState(false);

  useEffect(() => {
    loadTickets();
  }, []);

  const loadTickets = async () => {
    setLoadingTickets(true);
    setTicketsError("");
    try {
      const res = await api.get("/support/my");
      const sorted = (res.data || []).sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      setTickets(sorted);
    } catch (err) {
      setTicketsError("Failed to load your support tickets.");
    } finally {
      setLoadingTickets(false);
    }
  };

  const handleBugSubmit = async (e) => {
    e.preventDefault();
    if (!bugSubject.trim() || !bugDescription.trim()) {
      addToast("Subject and Description are required.", "error");
      return;
    }
    setSubmittingBug(true);
    try {
      await api.post("/support", {
        type: "BUG_REPORT",
        subject: bugSubject.trim(),
        description: bugDescription.trim(),
      });
      addToast("Bug report submitted successfully!", "success");
      setBugSubject("");
      setBugDescription("");
      loadTickets();
    } catch (err) {
      const errMsg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Failed to submit bug report.";
      addToast(errMsg, "error");
    } finally {
      setSubmittingBug(false);
    }
  };

  const handleSuggestionSubmit = async (e) => {
    e.preventDefault();
    if (!suggestSubject.trim() || !suggestDescription.trim()) {
      addToast("Subject and Description are required.", "error");
      return;
    }
    setSubmittingSuggest(true);
    try {
      await api.post("/support", {
        type: "SUGGESTION",
        subject: suggestSubject.trim(),
        description: suggestDescription.trim(),
      });
      addToast("Suggestion submitted successfully!", "success");
      setSuggestSubject("");
      setSuggestDescription("");
      loadTickets();
    } catch (err) {
      const errMsg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Failed to submit suggestion.";
      addToast(errMsg, "error");
    } finally {
      setSubmittingSuggest(false);
    }
  };

  const handleGfSubmit = async (e) => {
    e.preventDefault();
    if (gfRating === 0) {
      addToast("Rating is required.", "error");
      return;
    }
    if (!gfDescription.trim()) {
      addToast("Description is required.", "error");
      return;
    }
    setSubmittingGF(true);
    try {
      await api.post("/support", {
        type: "GENERAL_FEEDBACK",
        rating: gfRating,
        description: gfDescription.trim(),
      });
      addToast("General feedback submitted successfully!", "success");
      setGfRating(0);
      setGfDescription("");
      loadTickets();
    } catch (err) {
      const errMsg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Failed to submit general feedback.";
      addToast(errMsg, "error");
    } finally {
      setSubmittingGF(false);
    }
  };

  const handleViewTicket = (ticket) => {
    setSelectedTicket(ticket);
    setShowDetails(true);
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
          <div className="settings-header">
            <h1>Help & Feedback</h1>
            <p>Get assistance, suggest improvements, and send feedback to improve Decision Hub.</p>
          </div>

          {/* Help Tabs */}
          <div className="feedback-tabs">
            <button
              className={`feedback-tab-btn ${activeTab === "bug-report" ? "active" : ""}`}
              onClick={() => setActiveTab("bug-report")}
            >
              Bug Report
            </button>
            <button
              className={`feedback-tab-btn ${activeTab === "suggestion" ? "active" : ""}`}
              onClick={() => setActiveTab("suggestion")}
            >
              Suggestion
            </button>
            <button
              className={`feedback-tab-btn ${activeTab === "general-feedback" ? "active" : ""}`}
              onClick={() => setActiveTab("general-feedback")}
            >
              General Feedback
            </button>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "2rem" }}>
            {/* Tab content */}
            <div className="tab-container-content">
              {activeTab === "bug-report" && (
                <form onSubmit={handleBugSubmit} className="feedback-form-card glass-panel" style={{ margin: "0" }}>
                  <h2 style={{ fontSize: "1.25rem", marginBottom: "0.5rem" }}>Report a Bug</h2>
                  <div className="feedback-form-group">
                    <label htmlFor="bug-subject">Subject <span className="required-asterisk">*</span></label>
                    <input
                      id="bug-subject"
                      type="text"
                      placeholder="Briefly state the issue..."
                      value={bugSubject}
                      onChange={(e) => setBugSubject(e.target.value)}
                      required
                    />
                  </div>
                  <div className="feedback-form-group">
                    <label htmlFor="bug-desc">Description <span className="required-asterisk">*</span></label>
                    <textarea
                      id="bug-desc"
                      placeholder="Provide steps to reproduce, expected vs actual behavior..."
                      value={bugDescription}
                      onChange={(e) => setBugDescription(e.target.value)}
                      required
                      rows={5}
                    />
                  </div>
                  <button
                    type="submit"
                    className="btn-primary feedback-submit-btn"
                    disabled={submittingBug || !bugSubject.trim() || !bugDescription.trim()}
                  >
                    {submittingBug ? "Submitting..." : "Submit Bug Report"}
                  </button>
                </form>
              )}

              {activeTab === "suggestion" && (
                <form onSubmit={handleSuggestionSubmit} className="feedback-form-card glass-panel" style={{ margin: "0" }}>
                  <h2 style={{ fontSize: "1.25rem", marginBottom: "0.5rem" }}>Submit a Suggestion</h2>
                  <div className="feedback-form-group">
                    <label htmlFor="suggest-subject">Subject <span className="required-asterisk">*</span></label>
                    <input
                      id="suggest-subject"
                      type="text"
                      placeholder="What is your suggestion about?..."
                      value={suggestSubject}
                      onChange={(e) => setSuggestSubject(e.target.value)}
                      required
                    />
                  </div>
                  <div className="feedback-form-group">
                    <label htmlFor="suggest-desc">Description <span className="required-asterisk">*</span></label>
                    <textarea
                      id="suggest-desc"
                      placeholder="Describe your suggestion in detail..."
                      value={suggestDescription}
                      onChange={(e) => setSuggestDescription(e.target.value)}
                      required
                      rows={5}
                    />
                  </div>
                  <button
                    type="submit"
                    className="btn-primary feedback-submit-btn"
                    disabled={submittingSuggest || !suggestSubject.trim() || !suggestDescription.trim()}
                  >
                    {submittingSuggest ? "Submitting..." : "Submit Suggestion"}
                  </button>
                </form>
              )}

              {activeTab === "general-feedback" && (
                <form onSubmit={handleGfSubmit} className="feedback-form-card glass-panel" style={{ margin: "0" }}>
                  <h2 style={{ fontSize: "1.25rem", marginBottom: "0.5rem" }}>General Feedback</h2>
                  <div className="feedback-form-group">
                    <label>Rate Overall Experience <span className="required-asterisk">*</span></label>
                    <StarRating rating={gfRating} onChange={setGfRating} />
                  </div>
                  <div className="feedback-form-group">
                    <label htmlFor="gf-desc">Description <span className="required-asterisk">*</span></label>
                    <textarea
                      id="gf-desc"
                      placeholder="Provide details about your overall experience..."
                      value={gfDescription}
                      onChange={(e) => setGfDescription(e.target.value)}
                      required
                      rows={5}
                    />
                  </div>
                  <button
                    type="submit"
                    className="btn-primary feedback-submit-btn"
                    disabled={submittingGF || gfRating === 0 || !gfDescription.trim()}
                  >
                    {submittingGF ? "Submitting..." : "Submit Feedback"}
                  </button>
                </form>
              )}
            </div>

            {/* My Support Tickets Section */}
            <div className="my-support-tickets-section">
              <h2 style={{ fontSize: "1.4rem", fontFamily: "var(--font-title)", marginBottom: "1rem" }}>My Support Tickets</h2>

              {loadingTickets ? (
                <LoadingState />
              ) : ticketsError ? (
                <div className="empty-state glass-panel">
                  <div className="empty-state-icon" style={{ color: "var(--danger)" }}>
                    <FaExclamationTriangle />
                  </div>
                  <h3>Loading Tickets Failed</h3>
                  <p>{ticketsError}</p>
                </div>
              ) : tickets.length === 0 ? (
                <p style={{ color: "var(--text-secondary)" }}>You have not submitted any support tickets yet.</p>
              ) : (
                <div className="decision-table-wrapper glass-panel animate-fade-in">
                  <table className="decision-table-element">
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Subject</th>
                        <th>Rating</th>
                        <th>Status</th>
                        <th>Created Date</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {tickets.map((ticket) => (
                        <tr key={ticket.id}>
                          <td style={{ fontWeight: 600 }}>{formatTicketType(ticket.type)}</td>
                          <td>{ticket.subject || "—"}</td>
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
                          <td>
                            <button
                              className="btn-secondary"
                              style={{ padding: "0.4rem 0.8rem", fontSize: "0.85rem" }}
                              onClick={() => handleViewTicket(ticket)}
                            >
                              View Details
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Ticket Details Overlay Modal */}
      {showDetails && selectedTicket && (
        <div className="feedback-overlay">
          <div
            className="feedback-dialog glass-panel animate-pop-in"
            style={{ textAlign: "left", alignItems: "stretch", width: "500px", gap: "0" }}
          >
            <h2 style={{ marginBottom: "1.5rem", fontSize: "1.35rem" }}>Ticket Details</h2>

            <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginBottom: "1.75rem" }}>
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
                <p className="feedback-text" style={{ maxHeight: "150px", overflowY: "auto" }}>
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

            <button className="btn-primary" onClick={() => setShowDetails(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
