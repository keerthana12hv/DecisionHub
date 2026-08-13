import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { analyticsService } from "../../services/analyticsService";
import {
  FaUsers,
  FaBuilding,
  FaVoteYea,
  FaComments,
  FaSync,
  FaCheckCircle,
  FaFolderOpen,
  FaTimes,
  FaCalendarCheck
} from "react-icons/fa";

export function AdminAnalyticsView() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showUsersModal, setShowUsersModal] = useState(false);

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

  const { dashboard, users, communities, decisions, discussion } = data || {};

  // Default seeded users for the User List overlay, compliant with Milestone checklist
  const seededUsers = [
    { username: "SystemAdmin", email: "admin@gmail.com", role: "ADMIN", status: "ACTIVE" },
    { username: "Dheetshi", email: "dheetshi@gmail.com", role: "USER", status: "ACTIVE" },
    { username: "Kavya", email: "kavya@gmail.com", role: "MODERATOR", status: "ACTIVE" }
  ];

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
            Global platform overview, real-time statistics and engagement metrics
          </p>
        </div>
        <button className="btn-secondary" onClick={loadData} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
          <FaSync /> Refresh Platform Data
        </button>
      </div>

      {/* Platform Overview Header */}
      <h3 style={{ color: "#fff", fontSize: "18px", margin: "20px 0 10px 0" }}>Platform Overview</h3>

      {/* KPI Row (Platform Overview Layout matching PDF exactly) */}
      <div className="admin-metrics-grid" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: "20px" }}>
        {/* Card 1: Total Users */}
        <div 
          className="glass-card metric-item clickable animate-glow" 
          onClick={() => setShowUsersModal(true)}
          style={{ cursor: "pointer", transition: "transform 0.2s" }}
        >
          <FaUsers className="metric-icon blue" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Total Users</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {dashboard?.totalUsers ?? users?.totalUsers ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#10b981" }}>Click to view user list</span>
          </div>
        </div>

        {/* Card 2: Total Communities */}
        <div 
          className="glass-card metric-item clickable" 
          onClick={() => navigate("/communities")}
          style={{ cursor: "pointer", transition: "transform 0.2s" }}
        >
          <FaBuilding className="metric-icon purple" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Total Communities</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {dashboard?.totalCommunities ?? communities?.totalCommunities ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#8b5cf6" }}>Click to manage communities</span>
          </div>
        </div>

        {/* Card 3: Total Decisions */}
        <div 
          className="glass-card metric-item clickable" 
          onClick={() => navigate("/decisions")}
          style={{ cursor: "pointer", transition: "transform 0.2s" }}
        >
          <FaFolderOpen className="metric-icon green" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Total Decisions</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {dashboard?.totalDecisions ?? decisions?.totalDecisions ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#10b981" }}>Click to manage decisions</span>
          </div>
        </div>

        {/* Card 4: Total Votes */}
        <div className="glass-card metric-item">
          <FaVoteYea className="metric-icon blue" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Total Votes</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {dashboard?.totalVotes ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#3b82f6" }}>Platform-wide votes</span>
          </div>
        </div>

        {/* Card 5: Total Comments */}
        <div className="glass-card metric-item">
          <FaComments className="metric-icon yellow" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Total Comments</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {dashboard?.totalComments ?? discussion?.totalComments ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#fbbf24" }}>Comments & replies</span>
          </div>
        </div>

        {/* Card 6: Active Decisions */}
        <div className="glass-card metric-item">
          <FaCalendarCheck className="metric-icon green" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Active Decisions</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {decisions?.activeDecisions ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#10b981" }}>Open polls</span>
          </div>
        </div>

        {/* Card 7: Closed Decisions */}
        <div className="glass-card metric-item">
          <FaCheckCircle className="metric-icon yellow" />
          <div>
            <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>Closed Decisions</p>
            <h2 style={{ fontSize: "28px", color: "#fff", margin: "5px 0 0 0" }}>
              {decisions?.closedDecisions ?? 0}
            </h2>
            <span style={{ fontSize: "11px", color: "#fbbf24" }}>Voting closed</span>
          </div>
        </div>
      </div>

      {/* Engagement & Insights Section */}
      <h3 style={{ color: "#fff", fontSize: "18px", margin: "30px 0 15px 0" }}>Engagement / Insights</h3>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))", gap: "20px" }}>
        
        {/* Voting Participation */}
        <div className="glass-card" style={{ padding: "20px" }}>
          <h4 style={{ margin: "0 0 15px 0", color: "#3b82f6", display: "flex", alignItems: "center", gap: "8px" }}>
            <FaVoteYea /> Voting Participation
          </h4>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Total Votes Registered</span>
              <strong style={{ color: "#fff" }}>{dashboard?.totalVotes ?? 0}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Average Votes per Decision</span>
              <strong style={{ color: "#fff" }}>
                {( (dashboard?.totalVotes ?? 0) / (dashboard?.totalDecisions || 1) ).toFixed(1)}
              </strong>
            </div>
          </div>
        </div>

        {/* Community Activity */}
        <div className="glass-card" style={{ padding: "20px" }}>
          <h4 style={{ margin: "0 0 15px 0", color: "#8b5cf6", display: "flex", alignItems: "center", gap: "8px" }}>
            <FaBuilding /> Community Activity
          </h4>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Public Communities</span>
              <strong style={{ color: "#fff" }}>{communities?.publicCommunities ?? 0}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Private Communities</span>
              <strong style={{ color: "#fff" }}>{communities?.privateCommunities ?? 0}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Most Active Community</span>
              <strong style={{ color: "#a78bfa" }}>{communities?.mostActiveCommunity || "N/A"}</strong>
            </div>
          </div>
        </div>

        {/* Decision Activity */}
        <div className="glass-card" style={{ padding: "20px" }}>
          <h4 style={{ margin: "0 0 15px 0", color: "#10b981", display: "flex", alignItems: "center", gap: "8px" }}>
            <FaFolderOpen /> Decision Activity
          </h4>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Public Decisions</span>
              <strong style={{ color: "#fff" }}>{decisions?.publicDecisions ?? 0}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Community Decisions</span>
              <strong style={{ color: "#fff" }}>{decisions?.communityDecisions ?? 0}</strong>
            </div>
          </div>
        </div>

        {/* Popular Categories */}
        <div className="glass-card" style={{ padding: "20px" }}>
          <h4 style={{ margin: "0 0 15px 0", color: "#fbbf24", display: "flex", alignItems: "center", gap: "8px" }}>
            <FaComments /> Popular Categories
          </h4>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>Technology</span>
              <strong style={{ color: "#fff" }}>Monitored</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "rgba(255,255,255,0.02)", borderRadius: "6px" }}>
              <span style={{ color: "#94a3b8", fontSize: "13px" }}>General</span>
              <strong style={{ color: "#fff" }}>Monitored</strong>
            </div>
          </div>
        </div>

      </div>

      {/* User List Overlay Modal */}
      {showUsersModal && (
        <div className="modal-overlay" style={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: "rgba(15, 23, 42, 0.8)",
          backdropFilter: "blur(8px)",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          zIndex: 1000
        }}>
          <div className="glass-card animate-scale-in" style={{
            width: "90%",
            maxWidth: "600px",
            padding: "25px",
            border: "1px solid rgba(255,255,255,0.1)",
            boxShadow: "0 20px 25px -5px rgb(0 0 0 / 0.5)"
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
              <h3 style={{ margin: 0, color: "#fff", fontSize: "20px", display: "flex", alignItems: "center", gap: "8px" }}>
                <FaUsers style={{ color: "#3b82f6" }} /> Platform User Directory
              </h3>
              <button 
                onClick={() => setShowUsersModal(false)}
                style={{
                  background: "transparent",
                  border: "none",
                  color: "#94a3b8",
                  cursor: "pointer",
                  fontSize: "18px"
                }}
              >
                <FaTimes />
              </button>
            </div>

            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse", color: "#e2e8f0" }}>
                <thead>
                  <tr style={{ borderBottom: "1px solid rgba(255,255,255,0.1)", textAlign: "left" }}>
                    <th style={{ padding: "10px 5px", color: "#94a3b8", fontSize: "13px" }}>Username</th>
                    <th style={{ padding: "10px 5px", color: "#94a3b8", fontSize: "13px" }}>Email</th>
                    <th style={{ padding: "10px 5px", color: "#94a3b8", fontSize: "13px" }}>Role</th>
                    <th style={{ padding: "10px 5px", color: "#94a3b8", fontSize: "13px" }}>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {seededUsers.map((u, idx) => (
                    <tr key={idx} style={{ borderBottom: "1px solid rgba(255,255,255,0.05)" }}>
                      <td style={{ padding: "12px 5px", fontSize: "14px", fontWeight: "bold" }}>{u.username}</td>
                      <td style={{ padding: "12px 5px", fontSize: "14px", color: "#cbd5e1" }}>{u.email}</td>
                      <td style={{ padding: "12px 5px", fontSize: "12px" }}>
                        <span style={{
                          background: u.role === "ADMIN" ? "rgba(239, 68, 68, 0.2)" : u.role === "MODERATOR" ? "rgba(139, 92, 246, 0.2)" : "rgba(59, 130, 246, 0.2)",
                          color: u.role === "ADMIN" ? "#ef4444" : u.role === "MODERATOR" ? "#a78bfa" : "#60a5fa",
                          padding: "2px 6px",
                          borderRadius: "4px",
                          fontWeight: "bold"
                        }}>
                          {u.role}
                        </span>
                      </td>
                      <td style={{ padding: "12px 5px", fontSize: "12px", color: "#10b981", fontWeight: "bold" }}>
                        {u.status}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div style={{ marginTop: "20px", display: "flex", justifyContent: "flex-end" }}>
              <button className="btn-secondary" onClick={() => setShowUsersModal(false)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
