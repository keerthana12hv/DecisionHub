import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaBell,
  FaArrowRight,
  FaCommentDots,
  FaUsers,
  FaCheckCircle,
  FaInfoCircle
} from "react-icons/fa";
import "../styles/NotificationCard.css";

function NotificationCard() {
  const [notifications, setNotifications] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const stored = localStorage.getItem("decisionhub-notifications");
    if (stored) {
      setNotifications(JSON.parse(stored).slice(0, 3)); // show first 3
    }
  }, []);

  const getIcon = (text) => {
    const txt = text.toLowerCase();
    if (txt.includes("vote")) return <FaBell style={{ color: "var(--accent-blue)" }} />;
    if (txt.includes("comment")) return <FaCommentDots style={{ color: "var(--accent-purple)" }} />;
    if (txt.includes("community")) return <FaUsers style={{ color: "var(--success)" }} />;
    if (txt.includes("close") || txt.includes("end")) return <FaCheckCircle style={{ color: "var(--danger)" }} />;
    return <FaInfoCircle style={{ color: "var(--text-muted)" }} />;
  };

  return (
    <div className="notif-card-container">
      <div className="section-header">
        <h2 className="section-title">Latest Alerts</h2>
        <button className="view-all-link-btn" onClick={() => navigate("/notifications")}>
          Open Inbox <FaArrowRight />
        </button>
      </div>

      <div className="notification-card-element glass-panel">
        {notifications.length === 0 ? (
          <div className="empty-notifs">No new notifications.</div>
        ) : (
          notifications.map((item) => (
            <div key={item.id} className={`notification-item ${item.unread ? "unread" : ""}`}>
              <div className="notification-icon">
                {getIcon(item.text)}
              </div>
              <div className="notif-details">
                <span className="notif-msg">{item.text}</span>
                <span className="notif-time">{item.time}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default NotificationCard;