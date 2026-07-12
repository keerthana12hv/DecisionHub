import { useEffect, useState } from "react";
import {
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartLine,
  FaFolderMinus,
  FaFolderPlus
} from "react-icons/fa";
import "../styles/Activity.css";

function Activity() {
  const [activities, setActivities] = useState([]);

  useEffect(() => {
    const stored = localStorage.getItem("decisionhub-activities");
    if (stored) {
      setActivities(JSON.parse(stored));
    }
  }, []);

  const getIcon = (type) => {
    switch (type) {
      case "create": return <FaPlusCircle style={{ color: "var(--accent-purple)" }} />;
      case "vote": return <FaVoteYea style={{ color: "var(--accent-blue)" }} />;
      case "join": return <FaUsers style={{ color: "var(--success)" }} />;
      case "close": return <FaFolderMinus style={{ color: "var(--danger)" }} />;
      case "open": return <FaFolderPlus style={{ color: "var(--warning)" }} />;
      default: return <FaChartLine style={{ color: "var(--text-muted)" }} />;
    }
  };

  return (
    <div className="activity-container">
      <h2 className="section-title">Recent Activity</h2>

      <div className="activity-card glass-panel">
        {activities.length === 0 ? (
          <div className="empty-activity">No recent activities.</div>
        ) : (
          activities.map((item) => (
            <div className="activity-item" key={item.id}>
              <div className="activity-icon-wrapper">
                {getIcon(item.icon)}
              </div>
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