import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StatCard from "../components/StatCard";
import QuickAction from "../components/QuickAction";
import RecentDecision from "../components/RecentDecision";
import Activity from "../components/Activity";
import NotificationCard from "../components/NotificationCard";
import { FaPlus, FaVoteYea, FaUsers, FaChartPie, FaInbox, FaHourglassHalf } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [stats, setStats] = useState({
    decisionsCount: 0,
    votesCount: 0,
    communitiesCount: 0,
    activePollsCount: 0
  });

  const [deadlines, setDeadlines] = useState([]);

  useEffect(() => {
    // Check if user is logged in, otherwise redirect to login
    if (!user) {
      navigate("/login");
      return;
    }

    // Load data from LocalStorage
    const storedDecisions = JSON.parse(localStorage.getItem("decisionhub-decisions") || "[]");
    const storedCommunities = JSON.parse(localStorage.getItem("decisionhub-communities") || "[]");

    const totalVotes = storedDecisions.reduce((sum, d) => {
      const v = d.options.reduce((s, o) => s + o.votes, 0);
      return sum + v;
    }, 0);

    const activePolls = storedDecisions.filter(d => d.status === "Active");
    const joinedComms = storedCommunities.filter(c => c.joined);

    setStats({
      decisionsCount: storedDecisions.length,
      votesCount: totalVotes,
      communitiesCount: joinedComms.length,
      activePollsCount: activePolls.length
    });

    // Get active deadlines
    const upcoming = activePolls
      .map(d => ({ id: d.id, title: d.title, deadline: d.deadline }))
      .slice(0, 2);
    setDeadlines(upcoming);
  }, [user, navigate]);

  const isAdmin = user?.role === "ADMIN";

  if (!user) return null;

  return (
    <div className="dashboard">
      <Sidebar />

      <div className="dashboard-main">
        <Navbar />

        <div className="dashboard-content animate-fade-in">
          {/* Welcome Banner */}
          <div className="welcome-banner glass-panel animate-glow">
            <div className="welcome-text">
              <h1>Welcome Back, {user.username} 👋</h1>
              <p>
                Collaborate with your team, cast votes on key initiatives,
                and analyze polling trends from a single dashboard workspace.
              </p>
              {isAdmin && (
                <button
                  className="btn-primary banner-btn"
                  onClick={() => navigate("/create-decision")}
                >
                  <FaPlus /> Create Decision
                </button>
              )}
            </div>
            <div className="welcome-image">
              <img src={user.photo} alt={user.username} />
            </div>
          </div>

          {/* Statistics Grid */}
          <div className="stats-grid">
            <StatCard
              title="Total Decisions"
              value={stats.decisionsCount}
              icon={<FaInbox />}
              trend="+2 new this week"
            />
            <StatCard
              title="Total Votes"
              value={stats.votesCount}
              icon={<FaVoteYea />}
              trend="+34 today"
            />
            <StatCard
              title="Joined Communities"
              value={stats.communitiesCount}
              icon={<FaUsers />}
              trend="Active in all"
            />
            <StatCard
              title="Active Polls"
              value={stats.activePollsCount}
              icon={<FaChartPie />}
              trend="Closing soon"
            />
          </div>

          {/* Core Content Grid */}
          <div className="dashboard-columns">
            {/* Left Column: Quick Actions & Decisions Table */}
            <div className="dashboard-left-column">
              <QuickAction />
              <RecentDecision />
            </div>

            {/* Right Column: Deadlines, Activity, Notifications */}
            <div className="dashboard-right-column">
              {/* Upcoming Deadlines */}
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
                          <p>Closes on: {d.deadline}</p>
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