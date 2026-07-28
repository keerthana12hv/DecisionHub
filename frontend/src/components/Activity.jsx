import { useEffect, useState } from "react";
import axios from "axios";
import {
  FaPlusCircle,
  FaChartLine
} from "react-icons/fa";
import "../styles/Activity.css";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

function timeAgo(dateString) {
  const diffMs = Date.now() - new Date(dateString).getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return "Just now";
  if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? "s" : ""} ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hr${diffHours > 1 ? "s" : ""} ago`;
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;
}

function Activity() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchActivity();
  }, []);

  const fetchActivity = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`${API}/decisions`, headers());
      const sorted = [...res.data]
        .filter((d) => d.createdAt)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
        .map((d) => ({
          id: d.id,
          icon: "create",
          text: `Decision created: "${d.title}"`,
          time: timeAgo(d.createdAt)
        }));
      setActivities(sorted);
    } catch (err) {
      console.error("Failed to load activity:", err);
    } finally {
      setLoading(false);
    }
  };

  const getIcon = (type) => {
    switch (type) {
      case "create":
        return <FaPlusCircle style={{ color: "var(--accent-purple)" }} />;
      default:
        return <FaChartLine style={{ color: "var(--text-muted)" }} />;
    }
  };

  return (
    <div className="activity-container">
      <h2 className="section-title">Recent Activity</h2>

      <div className="activity-card glass-panel">
        {loading ? (
          <div className="empty-activity">Loading...</div>
        ) : activities.length === 0 ? (
          <div className="empty-activity">No recent activities.</div>
        ) : (
          activities.map((item) => (
            <div className="activity-item" key={item.id}>
              <div className="activity-icon-wrapper">{getIcon(item.icon)}</div>
              <div className="activity-details">
                <span className="activity-text">{item.text}</span>
                <span className="activity-time">{item.time}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Activity;