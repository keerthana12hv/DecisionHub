import { useEffect, useState } from "react";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StatCard from "../components/StatCard";
import QuickAction from "../components/QuickAction";
import RecentDecision from "../components/RecentDecision";
import Activity from "../components/Activity";
import NotificationCard from "../components/NotificationCard";
import { FaPlus, FaInbox, FaChartPie, FaUsers, FaShieldAlt, FaHourglassHalf } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
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

  const [stats, setStats] = useState({
    decisionsCount: 0,
    activePollsCount: 0,
    communitiesCount: 0,
    moderatingCount: 0
  });

  const [deadlines, setDeadlines] = useState([]);
  const [loading, setLoading] = useState(true);

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

      const upcoming = activePolls
        .filter((d) => d.deadline)
        .slice(0, 3)
        .map((d) => ({ id: d.id, title: d.title, deadline: d.deadline }));
      setDeadlines(upcoming);
    } catch (err) {
      console.error("Failed to load dashboard data:", err);
    } finally {
      setLoading(false);
    }
  };

  const isModerator = user?.role === "MODERATOR" || stats.moderatingCount > 0;

  if (!user) return null;

  return (
    <div className="dashboard">
      <Sidebar />

      <div className="dashboard-main">
        <Navbar />

        <div className="dashboard-content animate-fade-in">
          <div className="welcome-banner glass-panel animate-glow">
            <div className="welcome-text">
              <h1>Welcome Back, {user.username} 👋</h1>
              <p>
                Collaborate with your team, cast votes on key initiatives,
                and analyze polling trends from a single dashboard workspace.
              </p>
              <button
                className="btn-primary banner-btn"
                onClick={() => navigate("/create-decision")}
              >
                <FaPlus /> Create Decision
              </button>
            </div>
            <div className="welcome-image">
              {user.photo ? (
                <img src={user.photo} alt={user.username} />
              ) : (
                <div className="avatar-fallback">
                  {user.username?.[0]?.toUpperCase() || "?"}
                </div>
              )}
            </div>
          </div>

          <div className="stats-grid">
            <StatCard
              title="Total Decisions"
              value={loading ? "…" : stats.decisionsCount}
              icon={<FaInbox />}
            />
            <StatCard
              title="Active Polls"
              value={loading ? "…" : stats.activePollsCount}
              icon={<FaChartPie />}
            />
            <StatCard
              title="Joined Communities"
              value={loading ? "…" : stats.communitiesCount}
              icon={<FaUsers />}
            />
            <StatCard
              title="Communities Moderating"
              value={loading ? "…" : stats.moderatingCount}
              icon={<FaShieldAlt />}
            />
          </div>

          <div className="dashboard-columns">
            <div className="dashboard-left-column">
              <QuickAction />
              <RecentDecision />
            </div>

            <div className="dashboard-right-column">
              <div className="deadlines-container">
                <h2 className="section-title">Upcoming Deadlines</h2>
                <div className="deadlines-card glass-panel">
                  {deadlines.length === 0 ? (
                    <div className="empty-deadlines">No upcoming deadlines.</div>
                  ) : (
                    deadlines.map((d) => (
                      <div key={d.id} className="deadline-item">
                        <FaHourglassHalf className="deadline-icon" />
                        <div>
                          <h4>{d.title}</h4>
                          <p>Closes on: {new Date(d.deadline).toLocaleDateString()}</p>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>

              <Activity />
              <NotificationCard />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;