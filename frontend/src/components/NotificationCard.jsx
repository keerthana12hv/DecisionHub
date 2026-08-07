import { useNavigate } from "react-router-dom";
import {
  FaBell,
  FaArrowRight,
  FaCommentDots,
  FaUsers,
  FaInfoCircle
} from "react-icons/fa";
import { useNotifications } from "../context/NotificationsContext";
import "../styles/NotificationCard.css";

function NotificationCard() {
  const { notifications, markAsRead } = useNotifications();
  const navigate = useNavigate();

  const getIcon = (type) => {
    switch (type) {
      case "COMMENT":
        return <FaCommentDots style={{ color: "var(--accent-purple)" }} />;
      case "VOTE":
        return <FaBell style={{ color: "var(--accent-blue)" }} />;
      case "COMMUNITY":
        return <FaUsers style={{ color: "var(--success)" }} />;
      case "DECISION":
        return <FaBell style={{ color: "var(--accent-blue)" }} />;
      case "REMINDER":
        return <FaBell style={{ color: "var(--warning, #eab308)" }} />;
      case "SYSTEM":
        return <FaInfoCircle style={{ color: "var(--text-muted)" }} />;
      default:
        return <FaInfoCircle style={{ color: "var(--text-muted)" }} />;
    }
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return "";
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return dateStr;
      
      const now = new Date();
      const diffMs = now - date;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return "Just now";
      if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? "s" : ""} ago`;
      if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? "s" : ""} ago`;
      if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;

      return date.toLocaleDateString(undefined, { month: "short", day: "numeric" });
    } catch (e) {
      return dateStr;
    }
  };

  const handleNotifClick = async (item) => {
    try {
      if (!item.isRead) {
        await markAsRead(item.id);
      }
      if (item.actionUrl) {
        navigate(item.actionUrl);
      }
    } catch (err) {
      console.error("Failed to handle notification click:", err);
    }
  };

  const latestNotifs = notifications.slice(0, 3);

  return (
    <div className="notif-card-container">
      <div className="section-header">
        <h2 className="section-title">Latest Alerts</h2>
        <button className="view-all-link-btn" onClick={() => navigate("/notifications")}>
          Open Inbox <FaArrowRight />
        </button>
      </div>

      <div className="notification-card-element glass-panel">
        {latestNotifs.length === 0 ? (
          <div className="empty-notifs">No new notifications.</div>
        ) : (
          latestNotifs.map((item) => (
            <div 
              key={item.id} 
              className={`notification-item ${!item.isRead ? "unread" : ""}`}
              onClick={() => handleNotifClick(item)}
              style={{ cursor: "pointer" }}
            >
              <div className="notification-icon">
                {getIcon(item.type)}
              </div>
              <div className="notif-details">
                <span className="notif-msg">
                  {item.title ? <strong>{item.title}: </strong> : ""}
                  {item.message}
                </span>
                <span className="notif-time">{formatTime(item.createdAt)}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default NotificationCard;