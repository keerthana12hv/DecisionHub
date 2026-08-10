import { useEffect, useState } from "react";
import { analyticsService } from "../../services/analyticsService";
import { FaUsers, FaBuilding, FaVoteYea, FaComments, FaSync, FaCheckCircle, FaUserSlash, FaGlobe, FaLock } from "react-icons/fa";

export function AdminAnalyticsView() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await analyticsService.fetchAllAdminAnalytics();
      setData(res);
    } catch (err) {
      console.error("Error fetching admin platform analytics", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: "40px", color: "#94a3b8" }}>
        <FaSync className="spin" style={{ fontSize: "24px", marginBottom: "10px" }} />
        <p>Fetching Platform-Wide Admin Analytics...</p>
      </div>
    );
  }

  const { dashboard, users, communities, decisions, discussion, feedback } = data || {};

  return (
    <div className="analytics-tab-content space-y-6">
      {/* Admin Header */}
      <div className="glass-card" style={{ padding: "20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <span style={{ fontSize: "12px", background: "rgba(245, 158, 11, 0.2)", color: "#fbbf24", padding: "3px 8px", borderRadius: "12px", fontWeight: "bold" }}>
            Admin Platform System Overview
          </span>
          <h2 style={{ margin: "10px 0 5px 0", fontSize: "22px", color: "#fff" }}>
            DecisionHub Platform Analytics
          </h2>
          <p style={{ fontSize: "12px", color: "#94a3b8", margin: 0 }}>
            Global user statistics, community growth & platform decision metrics
          </p>
        </div>
        <button className="btn-secondary" onClick={loadData} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <FaSync /> Refresh Platform Data
        </button>
      </div>

      {/* KPI Row */}
      <div className="stats-grid analytics-metrics">
        <div className="glass-card metric-item animate-glow">
          <FaUsers className="metric-icon blue" />
          <div>
            <p>Total Users</p>
            <h2>{dashboard?.totalUsers ?? users?.totalUsers ?? 0}</h2>
            <span className="trend positive">{users?.activeUsers ?? 0} Active Users</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaBuilding className="metric-icon purple" />
          <div>
            <p>Total Communities</p>
            <h2>{dashboard?.totalCommunities ?? communities?.totalCommunities ?? 0}</h2>
            <span className="trend-text">{communities?.publicCommunities ?? 0} Public | {communities?.privateCommunities ?? 0} Private</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaVoteYea className="metric-icon green" />
          <div>
            <p>Total Decisions</p>
            <h2>{dashboard?.totalDecisions ?? decisions?.totalDecisions ?? 0}</h2>
            <span className="trend-text">{dashboard?.totalVotes ?? 0} Total Votes</span>
          </div>
        </div>

        <div className="glass-card metric-item">
          <FaComments className="metric-icon yellow" />
          <div>
            <p>Platform Comments</p>
            <h2>{dashboard?.totalComments ?? discussion?.totalComments ?? 0}</h2>
            <span className="trend-text">{dashboard?.totalReplies ?? discussion?.totalReplies ?? 0} Replies</span>
          </div>
        </div>
      </div>

      {/* Platform Breakdown */}
      <div className="charts-grid">
        {/* User Distribution */}
        <div className="glass-card chart-container">
          <h3>User Statistics & Status</h3>
          <div style={{ marginTop: "15px", display: "flex", flexDirection: "column", gap: "10px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#10b981", display: "flex", alignItems: "center", gap: "6px", fontSize: "13px" }}>
                <FaCheckCircle /> Active Users
              </span>
              <strong style={{ color: "#fff" }}>{users?.activeUsers ?? 0}</strong>
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", display: "flex", alignItems: "center", gap: "6px", fontSize: "13px" }}>
                <FaUserSlash /> Inactive Users
              </span>
              <strong style={{ color: "#fff" }}>{users?.inactiveUsers ?? 0}</strong>
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#ef4444", display: "flex", alignItems: "center", gap: "6px", fontSize: "13px" }}>
                <FaUserSlash /> Suspended Users
              </span>
              <strong style={{ color: "#ef4444" }}>{users?.suspendedUsers ?? 0}</strong>
            </div>
          </div>
        </div>

        {/* Community & Decision Lifecycle */}
        <div className="glass-card chart-container">
          <h3>Decision & Community Metrics</h3>
          <div style={{ marginTop: "15px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
            <div style={{ background: "rgba(255,255,255,0.02)", padding: "12px", borderRadius: "8px" }}>
              <span style={{ fontSize: "11px", color: "#94a3b8" }}>Draft Decisions</span>
              <h3 style={{ margin: "5px 0 0 0", color: "#f59e0b" }}>{decisions?.draftDecisions ?? 0}</h3>
            </div>
            <div style={{ background: "rgba(255,255,255,0.02)", padding: "12px", borderRadius: "8px" }}>
              <span style={{ fontSize: "11px", color: "#94a3b8" }}>Active Decisions</span>
              <h3 style={{ margin: "5px 0 0 0", color: "#10b981" }}>{decisions?.activeDecisions ?? 0}</h3>
            </div>
            <div style={{ background: "rgba(255,255,255,0.02)", padding: "12px", borderRadius: "8px" }}>
              <span style={{ fontSize: "11px", color: "#94a3b8" }}>Closed Decisions</span>
              <h3 style={{ margin: "5px 0 0 0", color: "#64748b" }}>{decisions?.closedDecisions ?? 0}</h3>
            </div>
            <div style={{ background: "rgba(255,255,255,0.02)", padding: "12px", borderRadius: "8px" }}>
              <span style={{ fontSize: "11px", color: "#94a3b8" }}>Top Community</span>
              <p style={{ margin: "5px 0 0 0", color: "#fff", fontWeight: "bold", fontSize: "12px" }}>
                {communities?.mostActiveCommunity || "Java Developers"}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
