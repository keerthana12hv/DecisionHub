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

export default function DecisionDetail() {
  const { decisionId } = useParams();
  const [decision, setDecision] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);

  useEffect(() => {
    fetchDecision();
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

  const isModerator =
    decision?.creator && String(decision.creator.id) === String(currentUserId);

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
              <h2>{decision.title}</h2>
              <p>{decision.description}</p>

              {isModerator && (
                <DecisionModerationControls
                  decision={decision}
                  onUpdate={(updated) => setDecision(updated)}
                />
              )}

              {decision.votingType === "RATING_BASED" ? (
                <RatingPanel decision={decision} pollOpen={decision.poll?.status === "OPEN"} />
              ) : (
                <p className="voting-placeholder">
                  This decision uses {decision.votingType} voting — not built yet.
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}