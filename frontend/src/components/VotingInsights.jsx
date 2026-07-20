import { useEffect, useMemo, useState } from "react";
import { FaVoteYea, FaChartPie, FaClock, FaArrowUp } from "react-icons/fa";
import api from "../services/api";

function VotingInsights() {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await api.get("/api/decisions");
        setDecisions(response.data || []);
      } catch (error) {
        console.error("Failed to load voting insights", error);
        setDecisions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const summary = useMemo(() => {
    const activePolls = decisions.filter((decision) => decision.status === "ACTIVE");
    const totalVotes = decisions.reduce(
      (sum, decision) => sum + (decision.options?.reduce((optionSum, option) => optionSum + (option.comparisonScores?.length || 0), 0) || 0),
      0
    );

    const topPoll = [...decisions].sort((a, b) => {
      const totalA = a.options?.reduce((sum, option) => sum + (option.comparisonScores?.length || 0), 0) || 0;
      const totalB = b.options?.reduce((sum, option) => sum + (option.comparisonScores?.length || 0), 0) || 0;
      return totalB - totalA;
    })[0];

    const topOption = topPoll?.options?.reduce((current, option) => {
      const currentVotes = option.comparisonScores?.length || 0;
      return currentVotes > current.votes ? { ...option, votes: currentVotes } : current;
    }, topPoll?.options?.[0] ? { ...topPoll.options[0], votes: topPoll.options[0].comparisonScores?.length || 0 } : { title: "No data", votes: 0 });

    return {
      activePolls: activePolls.length,
      totalVotes,
      topPollTitle: topPoll?.title || "No polls yet",
      topOptionName: topOption?.title || "No data",
      topOptionVotes: topOption?.votes || 0,
    };
  }, [decisions]);

  return (
    <div className="voting-insights glass-card">
      <div className="comparison-header">
        <div>
          <h3>Voting UI & Live Results</h3>
          <p>See live vote activity, participation, and the leading option for each active decision.</p>
        </div>
        <span className="comparison-badge">Live vote count</span>
      </div>

      <div className="voting-summary-grid">
        <div className="summary-pill">
          <FaVoteYea />
          <div>
            <p>Live Votes</p>
            <h4>{loading ? "…" : summary.totalVotes}</h4>
          </div>
        </div>
        <div className="summary-pill">
          <FaClock />
          <div>
            <p>Active Polls</p>
            <h4>{loading ? "…" : summary.activePolls}</h4>
          </div>
        </div>
        <div className="summary-pill">
          <FaChartPie />
          <div>
            <p>Leading Choice</p>
            <h4>{loading ? "…" : summary.topOptionName}</h4>
          </div>
        </div>
        <div className="summary-pill">
          <FaArrowUp />
          <div>
            <p>Momentum</p>
            <h4>{loading ? "…" : `${summary.topOptionVotes} votes`}</h4>
          </div>
        </div>
      </div>

      <div className="result-visualization">
        {loading ? (
          <div className="comparison-empty">Loading vote results…</div>
        ) : decisions.length === 0 ? (
          <div className="comparison-empty">No decision data available yet.</div>
        ) : decisions.map((decision) => {
          const totalDecisionVotes = (decision.options || []).reduce((sum, option) => sum + (option.comparisonScores?.length || 0), 0);
          return (
            <div key={decision.id} className="result-row">
              <div className="result-row-header">
                <div>
                  <h4>{decision.title}</h4>
                  <span>{decision.status}</span>
                </div>
                <strong>{totalDecisionVotes} votes</strong>
              </div>
              <div className="result-bars">
                {(decision.options || []).map((option) => {
                  const optionVotes = option.comparisonScores?.length || 0;
                  const percentage = totalDecisionVotes === 0 ? 0 : Math.round((optionVotes / totalDecisionVotes) * 100);
                  return (
                    <div key={option.id} className="result-bar-item">
                      <div className="result-bar-labels">
                        <span>{option.title}</span>
                        <span>{percentage}%</span>
                      </div>
                      <div className="result-bar-track">
                        <div className="result-bar-fill" style={{ width: `${percentage}%` }}></div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default VotingInsights;
