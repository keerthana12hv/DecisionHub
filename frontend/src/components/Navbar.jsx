import { useState, useEffect, useRef } from "react";
import { FaBell, FaSearch, FaUserCircle, FaCog, FaUser, FaSignOutAlt, FaCheck, FaTrash } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import "../styles/Navbar.css";
import {
  getNotifications,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
  deleteNotification as apiDeleteNotification
} from "../services/notificationService";

function Navbar() {
  const { user, logout } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [searchVal, setSearchVal] = useState("");
  const [showNotif, setShowNotif] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [notifications, setNotifications] = useState([]);

  const notifRef = useRef(null);
  const profileRef = useRef(null);

  // Initialize notifications from backend
  useEffect(() => {
    if (user) {
      loadNotifications();
      const interval = setInterval(loadNotifications, 15000); // Poll every 15 seconds
      return () => clearInterval(interval);
    }
  }, [user]);

  const loadNotifications = async () => {
    try {
      const data = await getNotifications();
      setNotifications(data);
    } catch (err) {
      console.error("Failed to load notifications:", err);
    }
  };

  // Close dropdowns on click outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notifRef.current && !notifRef.current.contains(event.target)) {
        setShowNotif(false);
      }
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setShowProfileMenu(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchVal.trim()) {
      navigate(`/decisions?search=${encodeURIComponent(searchVal)}`);
    }
  };

  const markAllAsRead = async () => {
    try {
      await apiMarkAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, unread: false })));
      addToast("All notifications marked as read.", "success");
    } catch (err) {
      console.error(err);
      addToast("Failed to mark all as read.", "error");
    }
  };

  const markAsRead = async (id) => {
    try {
      await apiMarkAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, unread: false } : n))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const deleteNotification = async (id) => {
    try {
      await apiDeleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      addToast("Notification deleted.", "info");
    } catch (err) {
      console.error(err);
      addToast("Failed to delete notification.", "error");
    }
  };

  const handleLogout = () => {
    logout();
    addToast("Successfully logged out.", "info");
    navigate("/login");
  };

  const unreadCount = notifications.filter((n) => n.unread).length;

  return (
    <div className="navbar glass-panel">
      {/* Search box */}
      <form onSubmit={handleSearchSubmit} className="search-box">
        <FaSearch className="search-icon" />
        <input
          type="text"
          placeholder="Search decisions or categories..."
          value={searchVal}
          onChange={(e) => setSearchVal(e.target.value)}
        />
      </form>

      <div className="navbar-right">
        {/* Notifications Dropdown */}
        <div className="notification-wrapper" ref={notifRef}>
          <button className="navbar-btn notification-bell" onClick={() => setShowNotif(!showNotif)}>
            <FaBell />
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
          </button>

          {showNotif && (
            <div className="notif-dropdown glass-panel animate-pop-in">
              <div className="dropdown-header">
                <h3>Notifications</h3>
                {unreadCount > 0 && (
                  <button className="mark-read-btn" onClick={markAllAsRead}>
                    Mark all read
                  </button>
                )}
              </div>
              <div className="dropdown-list">
                {notifications.length === 0 ? (
                  <div className="empty-notifs">No notifications.</div>
                ) : (
                  notifications.map((n) => (
                    <div key={n.id} className={`notif-item ${n.unread ? "unread" : ""}`}>
                      <div className="notif-content" onClick={() => markAsRead(n.id)}>
                        <p className="notif-text">{n.text}</p>
                        <span className="notif-time">{n.time}</span>
                      </div>
                      <div className="notif-actions">
                        {n.unread && (
                          <button className="action-btn read-action" onClick={() => markAsRead(n.id)} title="Mark as Read">
                            <FaCheck />
                          </button>
                        )}
                        <button className="action-btn delete-action" onClick={() => deleteNotification(n.id)} title="Delete">
                          <FaTrash />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
              <div className="dropdown-footer">
                <button className="view-all-btn" onClick={() => { navigate("/notifications"); setShowNotif(false); }}>
                  View All Notifications
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Profile Dropdown */}
        {user ? (
          <div className="profile-wrapper" ref={profileRef}>
            <div className="profile-trigger" onClick={() => setShowProfileMenu(!showProfileMenu)}>
              <img src={user.photo} alt={user.username} className="nav-avatar" />
              <div className="nav-profile-info">
                <h4>{user.username}</h4>
                <span>{user.role}</span>
              </div>
            </div>

            {showProfileMenu && (
              <div className="profile-dropdown glass-panel animate-pop-in">
                <div className="profile-dropdown-header">
                  <h4>{user.username}</h4>
                  <span>{user.email}</span>
                </div>
                <ul>
                  <li onClick={() => { navigate("/profile"); setShowProfileMenu(false); }}>
                    <FaUser /> My Profile
                  </li>
                  <li onClick={() => { navigate("/settings"); setShowProfileMenu(false); }}>
                    <FaCog /> Settings
                  </li>
                  <li className="logout-item" onClick={handleLogout}>
                    <FaSignOutAlt /> Logout
                  </li>
                </ul>
              </div>
            )}
          </div>
        ) : (
          <div className="profile">
            <FaUserCircle className="profile-icon" onClick={() => navigate("/login")} />
          </div>
        )}
      </div>
    </div>
  );
}

export default Navbar;