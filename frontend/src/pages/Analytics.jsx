import { useEffect, useState } from "react";
import "../styles/Analytics.css";

const STORAGE_KEY = "decisionhub-decisions";

function Analytics() {
  const [decisions, setDecisions] = useState([]);

  useEffect(() => {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setDecisions(saved);
  }, []);

  // Simple analytics: show stats for decisions created by current user
  const currentUser = localStorage.getItem("decisionhub-current-user") || "Mythili";
  const myDecisions = decisions.filter((d) => d.creator === currentUser);

  return (
    <div className="analytics-page">
      <h1>Analytics</h1>
      <p>Showing analytics for your decisions ({myDecisions.length})</p>

      {myDecisions.map((d) => (
        <div key={d.id} className="analytics-card">
          <h3>{d.title}</h3>
          <p>Votes: {(d.options || []).reduce((s, o) => s + (o.votes || 0), 0)}</p>
          <p>Comments: {(d.comments || []).length}</p>
        </div>
      ))}
    </div>
  );
}

export default Analytics;
