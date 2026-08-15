import { useEffect, useState } from "react";
import { analyticsService } from "../../services/analyticsService";
import { FaVoteYea, FaUsers, FaComments, FaStar, FaTrophy, FaSync } from "react-icons/fa";

export function DecisionAnalyticsView({ decisionId = 5 }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await analyticsService.fetchAllDecisionAnalytics(decisionId);
      setData(res);
    } catch (err) {
      console.error("Error fetching decision analytics", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [decisionId]);

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
        <FaSync className="spin" style={{ fontSize: "24px", marginBottom: "10px" }} />
        <p>Fetching Live Decision Analytics from Backend...</p>
      </div>
    );
  }

  const { overview, voteStats, distribution, participation, discussion, ranking, feedback } = data || {};

  const isExpired = overview?.votingEndTime ? (new Date() >= new Date(overview.votingEndTime)) : false;
  const displayPollStatus = (overview?.pollStatus === "OPEN" && !isExpired) ? "OPEN" : "CLOSED";

  return (
    <div className="analytics-tab-content space-y-6">
      {/* Decision Header */}
      <div className="glass-card" style={{ padding: "20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <span style={{ fontSize: "12px", background: "rgba(139, 92, 246, 0.2)", color: "#c084fc", padding: "3px 8px", borderRadius: "12px", fontWeight: "bold" }}>
            Decision #{decisionId}
          </span>
          <h2 style={{ margin: "10px 0 5px 0", fontSize: "22px", color: "#fff" }}>
            {overview?.title || `Decision #${decisionId} Overview`}
          </h2>
          <p style={{ fontSize: "12px", color: "#94a3b8", margin: 0 }}>
            Status: <strong>{displayPollStatus}</strong> | Total Votes: <strong>{overview?.totalVotes ?? 0}</strong>
          </p>
        </div>
        <button className="btn-secondary" onClick={loadData} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <FaSync /> Refresh
        </button>
      </div>

      {/* Metrics Row */}
      <div className="stats-grid analytics-metrics">
        <div className="glass-card metric-item animate-glow">
          <FaVoteYea className="metric-icon purple" />
          <div>
            <p>Total Votes</p>
            <h2>{voteStats?.totalVotes ?? overview?.totalVotes ?? 0}</h2>
            <span className="trend-text">{voteStats?.numberOfOptions ?? 0} Options</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaUsers className="metric-icon blue" />
          <div>
            <p>Total Participants</p>
            <h2>{voteStats?.totalParticipants ?? overview?.totalParticipants ?? 0}</h2>
            <span className="trend positive">{voteStats?.votePercentage?.toFixed(1) ?? 100}% Turnout</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaComments className="metric-icon green" />
          <div>
            <p>Total Comments</p>
            <h2>{discussion?.totalComments ?? 0}</h2>
            <span className="trend-text">{discussion?.totalReplies ?? 0} Replies</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaStar className="metric-icon yellow" />
          <div>
            <p>Feedback Rating</p>
            <h2>{feedback?.averageRating ? `${feedback.averageRating.toFixed(1)} / 5` : "N/A"}</h2>
            <span className="trend-text">{feedback?.feedbackCount ?? 0} Feedback Reviews</span>
          </div>
        </div>
      </div>

      {/* Distribution & Ranking */}
      <div className="charts-grid">
        {/* Vote Distribution */}
        <div className="glass-card chart-container">
          <h3>Vote Distribution by Option</h3>
          <p className="chart-desc">Real-time percentage and vote breakdown</p>

          <div style={{ marginTop: "15px", display: "flex", flexDirection: "column", gap: "12px" }}>
            {distribution && distribution.length > 0 ? (
              distribution.map((opt, idx) => (
                <div key={opt.optionId || idx}>
                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: "13px", marginBottom: "4px" }}>
                    <span style={{ color: "#e2e8f0", fontWeight: "600" }}>{opt.optionName}</span>
                    <span style={{ color: "#94a3b8" }}>{opt.voteCount} votes ({opt.percentage?.toFixed(1)}%)</span>
                  </div>
                  <div style={{ width: "100%", height: "8px", background: "rgba(255,255,255,0.1)", borderRadius: "4px", overflow: "hidden" }}>
                    <div
                      style={{
                        width: `${opt.percentage}%`,
                        height: "100%",
                        background: idx === 0 ? "#8b5cf6" : idx === 1 ? "#3b82f6" : "#f59e0b",
                        borderRadius: "4px",
                        transition: "width 0.5s ease"
                      }}
                    />
                  </div>
                </div>
              ))
            ) : (
              <p style={{ color: "#64748b", fontSize: "13px" }}>No distribution data available</p>
            )}
          </div>
        </div>

        {/* Final Ranking */}
        <div className="glass-card chart-container">
          <h3>Final Option Ranking</h3>
          <p className="chart-desc">Ranked results after voting closed</p>

          <div style={{ marginTop: "15px", display: "flex", flexDirection: "column", gap: "10px" }}>
            {ranking && ranking.length > 0 ? (
              ranking.map((item, idx) => (
                <div
                  key={item.optionId || idx}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "10px 14px",
                    background: "rgba(255,255,255,0.03)",
                    borderRadius: "8px",
                    border: "1px solid rgba(255,255,255,0.05)"
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                    <span
                      style={{
                        width: "24px",
                        height: "24px",
                        borderRadius: "50%",
                        background: idx === 0 ? "#f59e0b" : "#334155",
                        color: idx === 0 ? "#0f172a" : "#94a3b8",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontSize: "12px",
                        fontWeight: "bold"
                      }}
                    >
                      #{idx + 1}
                    </span>
                    <span style={{ color: "#fff", fontWeight: "600", fontSize: "14px" }}>{item.optionName}</span>
                    {idx === 0 && <FaTrophy style={{ color: "#f59e0b" }} />}
                  </div>
                  <span style={{ color: "#c084fc", fontWeight: "bold", fontSize: "13px" }}>
                    {item.voteCount} votes ({item.percentage?.toFixed(1)}%)
                  </span>
                </div>
              ))
            ) : (
              <p style={{ color: "#64748b", fontSize: "13px" }}>Ranking data will appear once poll closes</p>
            )}
          </div>
        </div>
      </div>

      {/* Participation & Rating */}
      <div className="charts-grid bottom-charts">
        <div className="glass-card chart-container">
          <h3>Participation Summary</h3>
          <div style={{ padding: "15px", display: "flex", justifyContent: "space-around", alignItems: "center" }}>
            <div style={{ textAlign: "center" }}>
              <p style={{ fontSize: "12px", color: "#94a3b8", margin: 0 }}>Participation Rate</p>
              <h2 style={{ fontSize: "32px", color: "#10b981", margin: "5px 0" }}>
                {participation?.participationPercentage?.toFixed(1) ?? 0}%
              </h2>
            </div>
            <div style={{ fontSize: "13px", color: "#cbd5e1" }}>
              <p style={{ margin: "4px 0" }}>Eligible Users: <strong>{participation?.eligibleUsers ?? 0}</strong></p>
              <p style={{ margin: "4px 0" }}>Voted: <strong style={{ color: "#10b981" }}>{participation?.usersVoted ?? 0}</strong></p>
              <p style={{ margin: "4px 0" }}>Not Voted: <strong style={{ color: "#ef4444" }}>{participation?.usersNotVoted ?? 0}</strong></p>
            </div>
          </div>
        </div>

        <div className="glass-card chart-container">
          <h3>Feedback Rating Breakdown</h3>
          <div style={{ padding: "15px", display: "flex", gap: "20px", alignItems: "center" }}>
            <div style={{ textAlign: "center", minWidth: "90px" }}>
              <h1 style={{ fontSize: "36px", color: "#f59e0b", margin: 0 }}>
                {feedback?.averageRating ? feedback.averageRating.toFixed(1) : "0.0"}
              </h1>
              <span style={{ fontSize: "11px", color: "#94a3b8" }}>{feedback?.feedbackCount ?? 0} Reviews</span>
            </div>
            <div style={{ flex: 1, fontSize: "12px", color: "#cbd5e1", display: "flex", flexDirection: "column", gap: "4px" }}>
              <div>5 Stars: {feedback?.fiveStar ?? 0}</div>
              <div>4 Stars: {feedback?.fourStar ?? 0}</div>
              <div>3 Stars: {feedback?.threeStar ?? 0}</div>
              <div>2 Stars: {feedback?.twoStar ?? 0}</div>
              <div>1 Star: {feedback?.oneStar ?? 0}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
