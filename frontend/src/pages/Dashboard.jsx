import { useEffect, useState } from "react";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StatCard from "../components/StatCard";
import QuickAction from "../components/QuickAction";
import RecentDecision from "../components/RecentDecision";
import Activity from "../components/Activity";
import NotificationCard from "../components/NotificationCard";
import { FaPlus, FaInbox, FaChartPie, FaUsers, FaShieldAlt, FaArrowRight } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { getAdminPlatformOverview, getAdminDecisionStats } from "../services/analyticsService";
import "../styles/Dashboard.css";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  // User Dashboard State
  const [stats, setStats] = useState({
    decisionsCount: 0,
    activePollsCount: 0,
    communitiesCount: 0,
    moderatingCount: 0
  });

  // Admin Dashboard State
  const [adminStats, setAdminStats] = useState({
    usersCount: 0,
    communitiesCount: 0,
    decisionsCount: 0,
    activePollsCount: 0
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate("/login");
      return;
    }
    loadDashboardData();
  }, [user, navigate]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(false);
      if (user?.role === "ADMIN") {
        const [overviewRes, decisionsRes] = await Promise.all([
          getAdminPlatformOverview(),
          getAdminDecisionStats()
        ]);
        
        setAdminStats({
          usersCount: overviewRes?.totalUsers ?? 0,
          communitiesCount: overviewRes?.totalCommunities ?? 0,
          decisionsCount: overviewRes?.totalDecisions ?? 0,
          activePollsCount: decisionsRes?.activeDecisions ?? 0
        });
      } else {
        const [decisionsRes, communitiesRes, moderatingRes] = await Promise.all([
          axios.get(`${API}/decisions`, headers()),
          axios.get(`${API}/communities/my`, headers()),
          axios.get(`${API}/communities/moderating`, headers())
        ]);

        const decisions = decisionsRes.data;
        const communities = communitiesRes.data;
        const moderating = moderatingRes.data;

        const activePolls = decisions.filter((d) => d.status === "ACTIVE");

        setStats({
          decisionsCount: decisions.length,
          activePollsCount: activePolls.length,
          communitiesCount: communities.length,
          moderatingCount: moderating.length
        });
      }
    } catch (err) {
      console.error("Failed to load dashboard data:", err);
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  const handleModeratingClick = () => {
    if (stats.moderatingCount > 0) {
      navigate("/moderator-dashboard");
    } else {
      addToast("You are not moderating any communities yet.", "info");
    }
  };

  if (!user) return null;

  // Render Error State
  if (error) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in" style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", minHeight: "60vh", gap: "1rem" }}>
            <FaShieldAlt style={{ fontSize: "3rem", color: "var(--warning)" }} />
            <h2 style={{ color: "#f87171", fontSize: "1.5rem" }}>Unable to load dashboard data.</h2>
            <button className="btn-primary" onClick={() => loadDashboardData()}>
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Render Admin simplified platform dashboard layout
  if (user.role === "ADMIN") {
    return (
      <div className="dashboard">
        <Sidebar />

        <div className="dashboard-main">
          <Navbar />

          <div className="dashboard-content animate-fade-in" style={{ padding: "1.25rem", display: "flex", flexDirection: "column", gap: "1.25rem" }}>
            
            {/* Compact Welcome Section */}
            <div className="welcome-banner glass-panel animate-glow" style={{ padding: "1rem 1.5rem", marginBottom: "0", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div className="welcome-text">
                <h1 style={{ fontSize: "1.6rem", margin: "0 0 0.25rem 0" }}>Welcome Back, {user.username} 👋</h1>
                <p style={{ fontSize: "0.9rem", margin: "0", color: "var(--text-secondary)" }}>
                  Platform overview
                </p>
              </div>
            </div>

            {/* Statistics - Compact and aligned in one row */}
            <div className="admin-stats-row">
              <div className="admin-stat-card">
                <StatCard
                  title="Total Users"
                  value={loading ? "…" : adminStats.usersCount}
                  icon={<FaUsers />}
                />
              </div>
              <div className="admin-stat-card">
                <StatCard
                  title="Total Communities"
                  value={loading ? "…" : adminStats.communitiesCount}
                  icon={<FaUsers />}
                />
              </div>
              <div className="admin-stat-card">
                <StatCard
                  title="Total Decisions"
                  value={loading ? "…" : adminStats.decisionsCount}
                  icon={<FaInbox />}
                />
              </div>
              <div className="admin-stat-card">
                <StatCard
                  title="Active Polls"
                  value={loading ? "…" : adminStats.activePollsCount}
                  icon={<FaChartPie />}
                />
              </div>
            </div>

            {/* Compact Recent Activity list */}
            <div style={{ flex: 1, minHeight: 0 }}>
              <Activity />
            </div>

          </div>
        </div>
      </div>
    );
  }

  // Render Zero-Data Empty State for standard user
  const isEmptyState =
    !loading &&
    stats.decisionsCount === 0 &&
    stats.activePollsCount === 0 &&
    stats.communitiesCount === 0 &&
    stats.moderatingCount === 0;

  if (isEmptyState) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
            
            {/* Welcome banner */}
            <div className="welcome-banner glass-panel animate-glow" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem 2rem", marginBottom: "0" }}>
              <div className="welcome-text">
                <h1 style={{ fontSize: "1.8rem" }}>Welcome Back, {user.username} 👋</h1>
                <p style={{ fontSize: "0.95rem", margin: "0" }}>
                  Collaborate, vote and make decisions together.
                </p>
              </div>
              <button
                className="btn-primary banner-btn"
                onClick={() => navigate("/create-decision")}
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <FaPlus /> Create Decision
              </button>
            </div>

            {/* Empty Dashboard Content Card */}
            <div className="glass-panel animate-glow" style={{ padding: "3rem 2rem", textAlign: "center", display: "flex", flexDirection: "column", alignItems: "center", gap: "1.5rem", maxWidth: "600px", margin: "3rem auto" }}>
              <div style={{ background: "rgba(139, 92, 246, 0.1)", color: "var(--accent-purple)", width: "64px", height: "64px", borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "2rem" }}>
                <FaInbox />
              </div>
              <div>
                <h2 style={{ fontSize: "1.4rem", color: "#fff", marginBottom: "0.5rem" }}>Welcome to DecisionHub!</h2>
                <p style={{ color: "var(--text-secondary)", fontSize: "0.9rem", maxWidth: "420px", margin: "0 auto", lineHeight: "1.5" }}>
                  Create your first decision or explore communities to get started.
                </p>
              </div>
              <div style={{ display: "flex", gap: "1rem", flexWrap: "wrap", justifyContent: "center" }}>
                <button className="btn-primary" onClick={() => navigate("/create-decision")} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <FaPlus /> Create Decision
                </button>
                <button className="btn-secondary" onClick={() => navigate("/communities")} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <FaUsers /> Explore Communities
                </button>
              </div>
            </div>

          </div>
        </div>
      </div>
    );
  }

  // Render standard user dashboard layout
  return (
    <div className="dashboard">
      <Sidebar />

      <div className="dashboard-main">
        <Navbar />

        <div className="dashboard-content animate-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          
          {/* Welcome Banner */}
          <div className="welcome-banner glass-panel animate-glow" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem 2rem", marginBottom: "0" }}>
            <div className="welcome-text">
              <h1 style={{ fontSize: "1.8rem" }}>Welcome Back, {user.username} 👋</h1>
              <p style={{ fontSize: "0.95rem", margin: "0" }}>
                Collaborate, vote and make decisions together.
              </p>
            </div>
            <button
              className="btn-primary banner-btn"
              onClick={() => navigate("/create-decision")}
              style={{ display: "flex", alignItems: "center", gap: "6px" }}
            >
              <FaPlus /> Create Decision
            </button>
          </div>

          {/* Clickable Statistics Cards */}
          <div className="stats-grid" style={{ marginBottom: "0" }}>
            <StatCard
              title="Total Decisions"
              value={loading ? "" : stats.decisionsCount}
              icon={<FaInbox />}
              onClick={() => navigate("/decisions")}
            />
            <StatCard
              title="Active Polls"
              value={loading ? "" : stats.activePollsCount}
              icon={<FaChartPie />}
              onClick={() => navigate("/decisions?status=ACTIVE")}
            />
            <StatCard
              title="Joined Communities"
              value={loading ? "" : stats.communitiesCount}
              icon={<FaUsers />}
              onClick={() => navigate("/communities")}
            />
            <StatCard
              title="Communities Moderating"
              value={loading ? "" : stats.moderatingCount}
              icon={<FaShieldAlt />}
              onClick={handleModeratingClick}
            />
          </div>

          {/* View Analytics Shortcut Link */}
          <div style={{ display: "flex", justifyContent: "flex-start", marginTop: "-0.5rem" }}>
            <button 
              className="view-all-link-btn" 
              onClick={() => navigate("/analytics")}
              style={{ fontSize: "0.9rem", display: "flex", alignItems: "center", gap: "6px" }}
            >
              View Analytics <FaArrowRight style={{ fontSize: "0.8rem" }} />
            </button>
          </div>

          {/* Actions and Recent Decisions columns */}
          <div className="dashboard-columns">
            <div className="dashboard-left-column" style={{ gap: "1.5rem" }}>
              <QuickAction />
            </div>

            <div className="dashboard-right-column">
              <RecentDecision />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;