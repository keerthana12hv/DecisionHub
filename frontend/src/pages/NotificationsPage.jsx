import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { FaBell, FaCheckDouble, FaTrash, FaCheck, FaInfoCircle } from "react-icons/fa";
import "../styles/NotificationsPage.css";

function NotificationsPage() {
  const { addToast } = useToast();
  const [notifications, setNotifications] = useState([]);
  const [filter, setFilter] = useState("all"); // "all", "unread"

  useEffect(() => {
    const storedNotifs = localStorage.getItem("decisionhub-notifications");
    if (storedNotifs) {
      setNotifications(JSON.parse(storedNotifs));
    }
  }, []);

  const persistNotifs = (nextNotifs) => {
    setNotifications(nextNotifs);
    localStorage.setItem("decisionhub-notifications", JSON.stringify(nextNotifs));
  };

  const markAllAsRead = () => {
    const updated = notifications.map((n) => ({ ...n, unread: false }));
    persistNotifs(updated);
    addToast("All notifications marked as read.", "success");
  };

  const markAsRead = (id) => {
    const updated = notifications.map((n) => n.id === id ? { ...n, unread: false } : n);
    persistNotifs(updated);
  };

  const deleteNotification = (id) => {
    const updated = notifications.filter((n) => n.id !== id);
    persistNotifs(updated);
    addToast("Notification deleted.", "success");
  };

  const clearAll = () => {
    persistNotifs([]);
    addToast("All notifications deleted.", "info");
  };

  const filteredNotifs = filter === "all"
    ? notifications
    : notifications.filter((n) => n.unread);

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="notifications-page-header">
            <div>
              <h1>Notifications Center</h1>
              <p>Keep track of activities, comments, and voting milestones.</p>
            </div>

            <div className="notifications-page-actions">
              {notifications.some((n) => n.unread) && (
                <button className="btn-secondary" onClick={markAllAsRead}>
                  <FaCheckDouble /> Mark All Read
                </button>
              )}
              {notifications.length > 0 && (
                <button className="btn-secondary clear-btn-all" onClick={clearAll}>
                  <FaTrash /> Delete All
                </button>
              )}
            </div>
          </div>

          <div className="notifications-card glass-panel">
            {/* Filter Tabs */}
            <div className="notifications-tabs">
              <button
                className={`tab-btn ${filter === "all" ? "active-tab" : ""}`}
                onClick={() => setFilter("all")}
              >
                All Notifications <span className="tab-count">{notifications.length}</span>
              </button>
              <button
                className={`tab-btn ${filter === "unread" ? "active-tab" : ""}`}
                onClick={() => setFilter("unread")}
              >
                Unread <span className="tab-count">{notifications.filter(n => n.unread).length}</span>
              </button>
            </div>

            {/* List */}
            <div className="notifications-page-list">
              {filteredNotifs.length === 0 ? (
                <div className="empty-state">
                  <FaBell className="empty-icon" />
                  <h3>No notifications found</h3>
                  <p>You're all caught up! When new activities occur, they'll show up here.</p>
                </div>
              ) : (
                filteredNotifs.map((n) => (
                  <div key={n.id} className={`notif-page-item ${n.unread ? "unread" : ""}`}>
                    <div className="notif-indicator-dot"></div>
                    <div className="notif-page-icon">
                      <FaInfoCircle />
                    </div>
                    <div className="notif-page-content" onClick={() => markAsRead(n.id)}>
                      <p>{n.text}</p>
                      <span className="notif-page-time">{n.time}</span>
                    </div>
                    <div className="notif-page-buttons">
                      {n.unread && (
                        <button
                          className="action-btn-p read-btn-p"
                          onClick={() => markAsRead(n.id)}
                          title="Mark as Read"
                        >
                          <FaCheck /> Mark read
                        </button>
                      )}
                      <button
                        className="action-btn-p delete-btn-p"
                        onClick={() => deleteNotification(n.id)}
                        title="Delete"
                      >
                        <FaTrash />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default NotificationsPage;
