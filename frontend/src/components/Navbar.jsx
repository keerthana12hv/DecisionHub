import { useState, useEffect, useRef } from "react";
import { 
  FaBell, 
  FaSearch, 
  FaUserCircle, 
  FaCog, 
  FaUser, 
  FaSignOutAlt, 
  FaCheck, 
  FaTrash 
} from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { useNotifications } from "../context/NotificationsContext";
import "../styles/Navbar.css";

function Navbar() {
  const { user, logout } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const [searchVal, setSearchVal] = useState("");
  const [showNotif, setShowNotif] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [imgError, setImgError] = useState(false);

  const {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotif
  } = useNotifications();

  const notifRef = useRef(null);
  const profileRef = useRef(null);

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

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      addToast("All notifications marked as read.", "success");
    } catch (err) {
      console.error(err);
      addToast("Failed to mark all notifications as read.", "error");
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await markAsRead(id);
    } catch (err) {
      console.error(err);
      addToast("Failed to mark notification as read.", "error");
    }
  };

  const handleDeleteNotif = async (id) => {
    try {
      await deleteNotif(id);
      addToast("Notification deleted.", "success");
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

  const handleToggleNotif = () => {
    if (!showNotif) {
      fetchNotifications(true);
      fetchUnreadCount(true);
    }
    setShowNotif(!showNotif);
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

      return date.toLocaleDateString(undefined, { 
        month: "short", 
        day: "numeric", 
        hour: "2-digit", 
        minute: "2-digit" 
      });
    } catch (e) {
      return dateStr;
    }
  };

  const handleNotifClick = async (n) => {
    try {
      if (!n.isRead) {
        await markAsRead(n.id);
      }
      setShowNotif(false);
      if (n.actionUrl) {
        navigate(n.actionUrl);
      }
    } catch (err) {
      console.error(err);
      addToast("Failed to handle notification click.", "error");
    }
  };

  return (
    <div className="navbar glass-panel">
      {/* Search Form with Icon & Submit */}
      <form className="navbar-search search-box" onSubmit={handleSearchSubmit}>
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
          <button className="navbar-btn notification-bell" onClick={handleToggleNotif}>
            <FaBell />
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
          </button>

          {showNotif && (
            <div className="notif-dropdown glass-panel animate-pop-in">
              <div className="dropdown-header">
                <h3>Notifications</h3>
                {unreadCount > 0 && (
                  <button className="mark-read-btn" onClick={handleMarkAllRead}>
                    Mark all read
                  </button>
                )}
              </div>

              <div className="dropdown-list">
                {loading ? (
                  <div className="empty-notifs">Loading notifications...</div>
                ) : notifications.length === 0 ? (
                  <div className="empty-notifs">No notifications.</div>
                ) : (
                  notifications.slice(0, 5).map((n) => (
                    <div key={n.id} className={`notif-item ${!n.isRead ? "unread" : ""}`}>
                      <div className="notif-content" onClick={() => handleNotifClick(n)}>
                        <p className="notif-text">
                          {n.title ? <strong>{n.title}: </strong> : ""}
                          {n.message}
                        </p>
                        <span className="notif-time">{formatTime(n.createdAt)}</span>
                      </div>
                      <div className="notif-actions">
                        {!n.isRead && (
                          <button
                            className="action-btn read-action"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleMarkAsRead(n.id);
                            }}
                            title="Mark as Read"
                          >
                            <FaCheck />
                          </button>
                        )}
                        <button
                          className="action-btn delete-action"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteNotif(n.id);
                          }}
                          title="Delete"
                        >
                          <FaTrash />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>

              <div className="dropdown-footer">
                <button
                  className="view-all-btn"
                  onClick={() => {
                    navigate("/notifications");
                    setShowNotif(false);
                  }}
                >
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
              {user.photo && !imgError ? (
                <img
                  src={user.photo}
                  alt={user.username || "User profile"}
                  className="nav-avatar"
                  onError={() => setImgError(true)}
                />
              ) : (
                <FaUserCircle className="profile-icon nav-avatar-icon" />
              )}

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