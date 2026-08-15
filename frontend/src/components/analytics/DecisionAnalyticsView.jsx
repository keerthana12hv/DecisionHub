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

  const { overview, voteStats, distribution, participation, discussion, ranking } = data || {};

  // Calculate ranking with ties
  const getRankingWithTies = () => {
    if (!ranking || ranking.length === 0) return [];
    
    // Sort by vote count descending
    const sorted = [...ranking].sort((a, b) => b.voteCount - a.voteCount);
    
    let currentRank = 1;
    let lastVotes = -1;
    
    return sorted.map((item, idx) => {
      if (item.voteCount !== lastVotes) {
        currentRank = idx + 1;
        lastVotes = item.voteCount;
      }
      return {
        ...item,
        rank: currentRank
      };
    });
  };

  const rankedList = getRankingWithTies();
  const isTieForFirst = rankedList.length > 1 && rankedList[0].voteCount === rankedList[1].voteCount;

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
            Status: <strong>{overview?.status || overview?.pollStatus || "CLOSED"}</strong> | Total Votes: <strong>{overview?.totalVotes ?? 0}</strong>
          </p>
        </div>
        <button className="btn-secondary" onClick={loadData} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <FaSync /> Refresh
        </button>
      </div>

      {/* Metrics Row */}
      <div className="stats-grid analytics-metrics" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: "20px" }}>
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
              <p style={{ color: "#64748b", fontSize: "13px" }}>No votes yet</p>
            )}
          </div>
        </div>

        {/* Final Ranking */}
        <div className="glass-card chart-container">
          <h3>Final Option Ranking</h3>
          <p className="chart-desc">Ranked results after voting closed</p>

          <div style={{ marginTop: "15px", display: "flex", flexDirection: "column", gap: "10px" }}>
            {rankedList && rankedList.length > 0 ? (
              rankedList.map((item, idx) => (
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
                        background: item.rank === 1 ? "#f59e0b" : "#334155",
                        color: item.rank === 1 ? "#0f172a" : "#94a3b8",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontSize: "12px",
                        fontWeight: "bold"
                      }}
                    >
                      #{item.rank}
                    </span>
                    <span style={{ color: "#fff", fontWeight: "600", fontSize: "14px" }}>{item.optionName}</span>
                    {item.rank === 1 && !isTieForFirst && item.voteCount > 0 && <FaTrophy style={{ color: "#f59e0b" }} />}
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

      {/* Participation Summary (Spans 100% width) */}
      <div className="glass-card chart-container" style={{ width: "100%" }}>
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
    </div>
  );
}
