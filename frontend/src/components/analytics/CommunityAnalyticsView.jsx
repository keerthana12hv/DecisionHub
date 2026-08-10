import { useEffect, useState } from "react";
import { analyticsService } from "../../services/analyticsService";
import { FaUsers, FaVoteYea, FaComments, FaShieldAlt, FaSync, FaLock, FaUnlock, FaThumbtack, FaExclamationTriangle } from "react-icons/fa";

export function CommunityAnalyticsView({ communityId = 1 }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await analyticsService.fetchAllCommunityAnalytics(communityId);
      setData(res);
    } catch (err) {
      console.error("Error fetching community analytics", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [communityId]);

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
        <FaSync className="spin" style={{ fontSize: "24px", marginBottom: "10px" }} />
        <p>Fetching Moderator Community Analytics...</p>
      </div>
    );
  }

  const { overview, decisions, voting, discussion, activity, moderation } = data || {};

  return (
    <div className="analytics-tab-content space-y-6">
      {/* Community Header */}
      <div className="glass-card" style={{ padding: "20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <span style={{ fontSize: "12px", background: "rgba(59, 130, 246, 0.2)", color: "#60a5fa", padding: "3px 8px", borderRadius: "12px", fontWeight: "bold" }}>
            Community Moderator Analytics #{communityId}
          </span>
          <h2 style={{ margin: "10px 0 5px 0", fontSize: "22px", color: "#fff" }}>
            {overview?.communityName || `Community Analytics #${communityId}`}
          </h2>
          <p style={{ fontSize: "12px", color: "#94a3b8", margin: 0 }}>
            Member Engagement, Voting Statistics & Moderation Overview
          </p>
        </div>
        <button className="btn-secondary" onClick={loadData} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <FaSync /> Refresh
        </button>
      </div>

      {/* Metrics Row */}
      <div className="stats-grid analytics-metrics">
        <div className="glass-card metric-item animate-glow">
          <FaUsers className="metric-icon blue" />
          <div>
            <p>Community Members</p>
            <h2>{overview?.totalMembers ?? overview?.memberCount ?? 0}</h2>
            <span className="trend positive">Registered Members</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaVoteYea className="metric-icon purple" />
          <div>
            <p>Total Decisions</p>
            <h2>{decisions?.totalDecisions ?? 0}</h2>
            <span className="trend-text">{decisions?.activeDecisions ?? 0} Active | {decisions?.closedDecisions ?? 0} Closed</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaComments className="metric-icon green" />
          <div>
            <p>Community Discussion</p>
            <h2>{discussion?.totalComments ?? 0}</h2>
            <span className="trend-text">{discussion?.totalReplies ?? 0} Total Replies</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaShieldAlt className="metric-icon yellow" />
          <div>
            <p>Moderation Actions</p>
            <h2>{moderation?.removedComments ?? 0}</h2>
            <span className="trend-text">Removed Comments</span>
          </div>
        </div>
      </div>

      {/* Moderation Analytics Panel */}
      <div className="glass-card chart-container">
        <h3>Moderation & Control Analytics</h3>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(130px, 1fr))", gap: "15px", marginTop: "15px" }}>
          <div style={{ background: "rgba(255,255,255,0.03)", padding: "15px", borderRadius: "10px", textAlign: "center" }}>
            <FaLock style={{ color: "#ef4444", fontSize: "20px", marginBottom: "5px" }} />
            <h3 style={{ margin: "5px 0", color: "#fff" }}>{moderation?.lockedDiscussions ?? 0}</h3>
            <span style={{ fontSize: "11px", color: "#94a3b8" }}>Locked Discussions</span>
          </div>

          <div style={{ background: "rgba(255,255,255,0.03)", padding: "15px", borderRadius: "10px", textAlign: "center" }}>
            <FaUnlock style={{ color: "#10b981", fontSize: "20px", marginBottom: "5px" }} />
            <h3 style={{ margin: "5px 0", color: "#fff" }}>{moderation?.unlockedDiscussions ?? 0}</h3>
            <span style={{ fontSize: "11px", color: "#94a3b8" }}>Unlocked Discussions</span>
          </div>

          <div style={{ background: "rgba(255,255,255,0.03)", padding: "15px", borderRadius: "10px", textAlign: "center" }}>
            <FaThumbtack style={{ color: "#f59e0b", fontSize: "20px", marginBottom: "5px" }} />
            <h3 style={{ margin: "5px 0", color: "#fff" }}>{moderation?.pinnedDecisions ?? 0}</h3>
            <span style={{ fontSize: "11px", color: "#94a3b8" }}>Pinned Decisions</span>
          </div>

          <div style={{ background: "rgba(255,255,255,0.03)", padding: "15px", borderRadius: "10px", textAlign: "center" }}>
            <FaExclamationTriangle style={{ color: "#f97316", fontSize: "20px", marginBottom: "5px" }} />
            <h3 style={{ margin: "5px 0", color: "#fff" }}>{moderation?.reportedComments ?? 0}</h3>
            <span style={{ fontSize: "11px", color: "#94a3b8" }}>Reported Comments</span>
          </div>
        </div>
      </div>

      {/* Member Activity Leaderboards */}
      <div className="charts-grid bottom-charts">
        <div className="glass-card chart-container">
          <h3>Most Active Members</h3>
          <div style={{ marginTop: "12px", display: "flex", flexDirection: "column", gap: "8px" }}>
            {activity?.mostActiveMembers && activity.mostActiveMembers.length > 0 ? (
              activity.mostActiveMembers.map((user, idx) => (
                <div key={user.userId || idx} style={{ display: "flex", justifyContent: "space-between", padding: "8px 12px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
                  <span style={{ color: "#fff", fontWeight: "600", fontSize: "13px" }}>#{idx + 1} {user.username}</span>
                  <span style={{ color: "#60a5fa", fontWeight: "bold", fontSize: "12px" }}>{user.value} Actions</span>
                </div>
              ))
            ) : (
              <p style={{ color: "#64748b", fontSize: "12px" }}>No member activity recorded</p>
            )}
          </div>
        </div>

        <div className="glass-card chart-container">
          <h3>Top Decision Creators</h3>
          <div style={{ marginTop: "12px", display: "flex", flexDirection: "column", gap: "8px" }}>
            {activity?.topDecisionCreators && activity.topDecisionCreators.length > 0 ? (
              activity.topDecisionCreators.map((user, idx) => (
                <div key={user.userId || idx} style={{ display: "flex", justifyContent: "space-between", padding: "8px 12px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
                  <span style={{ color: "#fff", fontWeight: "600", fontSize: "13px" }}>#{idx + 1} {user.username}</span>
                  <span style={{ color: "#c084fc", fontWeight: "bold", fontSize: "12px" }}>{user.value} Decisions</span>
                </div>
              ))
            ) : (
              <p style={{ color: "#64748b", fontSize: "12px" }}>No decision creators recorded</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
