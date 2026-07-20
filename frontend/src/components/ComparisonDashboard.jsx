import { useEffect, useMemo, useState } from "react";
import { FaBalanceScale, FaChartLine, FaUsers, FaCheckCircle } from "react-icons/fa";
import api from "../services/api";

const scoreMeta = [
  { title: "Decision Alignment", icon: <FaBalanceScale />, tone: "purple" },
  { title: "Consensus Score", icon: <FaCheckCircle />, tone: "blue" },
  { title: "Community Engagement", icon: <FaUsers />, tone: "green" },
  { title: "Confidence Index", icon: <FaChartLine />, tone: "yellow" },
];

function ComparisonDashboard() {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await api.get("/api/decisions");
        setDecisions(response.data || []);
      } catch (error) {
        console.error("Failed to load comparison data", error);
        setDecisions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const analytics = useMemo(() => {
    const total = decisions.length;
    const active = decisions.filter((decision) => decision.status === "ACTIVE").length;
    const closed = total - active;

    const rows = decisions.slice(0, 4).map((decision) => {
      const options = decision.options || [];
      const first = options[0];
      const second = options[1];
      const firstVotes = first?.comparisonScores?.length || 0;
      const secondVotes = second?.comparisonScores?.length || 0;

      return {
        category: decision.title,
        optionA: first?.title || "No option",
        optionB: second?.title || "No option",
        scoreA: `${Math.max(firstVotes, 0)} score${firstVotes === 1 ? "" : "s"}`,
        scoreB: `${Math.max(secondVotes, 0)} score${secondVotes === 1 ? "" : "s"}`,
      };
    });

    const scoreCards = [
      { title: "Decision Alignment", value: `${Math.round((active / Math.max(total, 1)) * 100)}%`, change: `${active} active decisions`, icon: scoreMeta[0].icon, tone: scoreMeta[0].tone },
      { title: "Consensus Score", value: `${Math.round((closed / Math.max(total, 1)) * 100)}%`, change: `${closed} closed decisions`, icon: scoreMeta[1].icon, tone: scoreMeta[1].tone },
      { title: "Community Engagement", value: `${decisions.reduce((sum, decision) => sum + (decision.options?.length || 0), 0)}`, change: "options tracked", icon: scoreMeta[2].icon, tone: scoreMeta[2].tone },
      { title: "Confidence Index", value: `${Math.min(100, Math.round(total * 20))}`, change: `${total} decisions loaded`, icon: scoreMeta[3].icon, tone: scoreMeta[3].tone },
    ];

    return { scoreCards, rows };
  }, [decisions]);

  return (
    <div className="comparison-dashboard glass-card">
      <div className="comparison-header">
        <div>
          <h3>Comparison Dashboard</h3>
          <p>Track decision outcomes, community support, and confidence trends in one place.</p>
        </div>
        <span className="comparison-badge">Live insights</span>
      </div>

      <div className="comparison-score-grid">
        {loading ? (
          <div className="comparison-empty">Loading decisions…</div>
        ) : (
          analytics.scoreCards.map((card) => (
            <div key={card.title} className={`comparison-score-card ${card.tone}`}>
              <div className="score-icon">{card.icon}</div>
              <div>
                <p>{card.title}</p>
                <h4>{card.value}</h4>
                <span>{card.change}</span>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="comparison-table-wrapper">
        <table className="comparison-table">
          <thead>
            <tr>
              <th>Category</th>
              <th>Option A</th>
              <th>Option B</th>
              <th>Score A</th>
              <th>Score B</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="5" className="comparison-empty">Loading decision comparison rows…</td>
              </tr>
            ) : analytics.rows.length === 0 ? (
              <tr>
                <td colSpan="5" className="comparison-empty">No decisions available yet.</td>
              </tr>
            ) : analytics.rows.map((row) => (
              <tr key={row.category}>
                <td>{row.category}</td>
                <td>{row.optionA}</td>
                <td>{row.optionB}</td>
                <td>{row.scoreA}</td>
                <td>{row.scoreB}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default ComparisonDashboard;
