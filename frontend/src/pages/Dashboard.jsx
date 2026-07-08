import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import StatCard from "../components/StatCard";
import QuickAction from "../components/QuickAction";
import RecentDecision from "../components/RecentDecision";
import Activity from "../components/Activity";
import NotificationCard from "../components/NotificationCard";
import { FaPlus } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import "../styles/Dashboard.css";

const STORAGE_KEY = "decisionhub-decisions";

function Dashboard() {
  const navigate = useNavigate();
  const [decisions, setDecisions] = useState([]);

  useEffect(() => {
    const storedDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setDecisions(storedDecisions);
  }, []);

  const stats = useMemo(() => {
    const totalDecisions = decisions.length;
    const totalVotes = decisions.reduce((sum, decision) => sum + (decision.options || []).reduce((optSum, option) => optSum + option.votes, 0), 0);
    const activePolls = decisions.filter((decision) => decision.status === "Active").length;

    return {
      totalDecisions,
      totalVotes,
      activePolls,
    };
  }, [decisions]);

  return (
    <div className="dashboard">

      <Sidebar />

      <div className="dashboard-main">

        <Navbar />

        <div className="dashboard-content">

          {/* Welcome Banner */}

          <div className="welcome-banner">

            <div className="welcome-text">

              <h1>Welcome Back, Mythili 👋</h1>

              <p>
                Make smarter decisions, collaborate with your team,
                and track every poll from one place.
              </p>

              <button
                className="banner-btn"
                onClick={() => navigate("/create-decision")}
              >
                <FaPlus />
                Create Decision
              </button>

            </div>

            <div className="welcome-image">

              <img
                src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
                alt="User"
              />

            </div>

          </div>

          {/* Statistics */}

          <div className="stats-grid">

            <StatCard title="Total Decisions" value={stats.totalDecisions.toString()} />

            <StatCard title="Total Votes" value={stats.totalVotes.toString()} />

            <StatCard title="Communities" value="5" />

            <StatCard title="Active Polls" value={stats.activePolls.toString()} />

          </div>

          {/* Quick Actions */}

          <QuickAction />

          {/* Recent Decisions */}

          <RecentDecision decisions={decisions} />

          {/* Activity */}

          <Activity />

          {/* Notifications */}

          <NotificationCard />

        </div>

      </div>

    </div>
  );
}

export default Dashboard;