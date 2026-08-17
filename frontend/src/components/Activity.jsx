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
    <div className="activity-container" style={{ marginBottom: "1rem" }}>
      <h3 className="section-title" style={{ fontSize: "1.1rem", marginBottom: "0.75rem" }}>Recent Activity</h3>

      <div className="activity-card glass-panel">
        {loading ? (
          <div className="empty-activity" style={{ padding: "1rem" }}>Loading...</div>
        ) : activities.length === 0 ? (
          <div className="empty-activity" style={{ padding: "1.5rem", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
            <FaChartLine style={{ fontSize: "1.5rem", color: "var(--text-muted)", marginBottom: "8px" }} />
            <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>No recent platform activity</span>
          </div>
        ) : (
          activities.map((item) => (
            <div className="activity-item" key={item.id} style={{ padding: "0.75rem 1.25rem" }}>
              <div className="activity-icon-wrapper" style={{ width: "30px", height: "30px", fontSize: "0.95rem" }}>{getIcon(item.icon)}</div>
              <div className="activity-details">
                <span className="activity-text" style={{ fontSize: "0.85rem" }}>{item.text}</span>
                <span className="activity-time" style={{ fontSize: "0.7rem" }}>{item.time}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Activity;