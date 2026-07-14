import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaArrowRight, FaVoteYea } from "react-icons/fa";
import "../styles/RecentDecision.css";

function RecentDecision() {
  const [decisions, setDecisions] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const stored = localStorage.getItem("decisionhub-decisions");
    if (stored) {
      const all = JSON.parse(stored);
      // Get the last 3 decisions
      setDecisions(all.slice(-3).reverse());
    }
  }, []);

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
          <span>Votes</span>
          <span style={{ textAlign: "right" }}>Action</span>
        </div>

        {decisions.length === 0 ? (
          <div className="empty-table-row">No decisions created yet.</div>
        ) : (
          decisions.map((decision) => {
            const totalVotes = decision.options.reduce((sum, opt) => sum + opt.votes, 0);
            return (
              <div className="table-row" key={decision.id}>
                <span className="decision-title-cell">{decision.title}</span>
                <span className="decision-cat-cell">{decision.category}</span>
                <span>
                  <span className={`status-badge ${decision.status.toLowerCase()}`}>
                    {decision.status}
                  </span>
                </span>
                <span className="votes-count-cell">{totalVotes} votes</span>
                <span className="action-cell">
                  <button className="row-action-btn" onClick={() => navigate("/vote")}>
                    <FaVoteYea /> Vote
                  </button>
                </span>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

export default RecentDecision;