import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { FaArrowRight, FaVoteYea, FaThumbtack, FaLock } from "react-icons/fa";
import "../styles/RecentDecision.css";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

function RecentDecision() {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchRecentDecisions();
  }, []);

  const fetchRecentDecisions = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`${API}/decisions`, headers());
      // Most recently created first, limit to 3
      const sorted = [...res.data].sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setDecisions(sorted.slice(0, 3));
    } catch (err) {
      console.error("Failed to load recent decisions:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="recent-decisions-container">
      <div className="section-header">
        <h2 className="section-title">Recent Decisions</h2>
        <button className="view-all-link-btn" onClick={() => navigate("/decisions")}>
          View All <FaArrowRight />
        </button>
      </div>

      <div className="decision-table glass-panel">
        <div className="table-header">
          <span>Decision Title</span>
          <span>Category</span>
          <span>Status</span>
          <span style={{ textAlign: "right" }}>Action</span>
        </div>

        {loading ? (
          <div className="empty-table-row">Loading...</div>
        ) : decisions.length === 0 ? (
          <div className="empty-table-row">No decisions created yet.</div>
        ) : (
          decisions.map((decision) => (
            <div className="table-row" key={decision.id}>
              <span className="decision-title-cell">
                {decision.pinned && <FaThumbtack title="Pinned" style={{ marginRight: 6, fontSize: "0.8em" }} />}
                {decision.locked && <FaLock title="Locked" style={{ marginRight: 6, fontSize: "0.8em" }} />}
                {decision.title}
              </span>
              <span className="decision-cat-cell">{decision.categoryName}</span>
              <span>
                <span className={`status-badge ${decision.status.toLowerCase()}`}>
                  {decision.status}
                </span>
              </span>
              <span className="action-cell">
                <button className="row-action-btn" onClick={() => navigate(`/decision/${decision.id}`)}>
                  <FaVoteYea /> View
                </button>
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default RecentDecision;