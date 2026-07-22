import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import RatingPanel from "../components/RatingPanel";

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

  useEffect(() => {
    fetchDecision();
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

  if (loading) return <p>Loading decision...</p>;
  if (error) return <p>{error}</p>;
  if (!decision) return <p>Decision not found.</p>;

  const pollOpen = decision.poll?.status === "OPEN";

  return (
    <div className="decision-detail-page">
      <h2>{decision.title}</h2>
      <p>{decision.description}</p>

      {decision.votingType === "RATING_BASED" ? (
        <RatingPanel decision={decision} pollOpen={pollOpen} />
      ) : (
        <p>This decision uses {decision.votingType} voting — not built yet.</p>
      )}
    </div>
  );
}