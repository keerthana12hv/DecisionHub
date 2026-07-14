import {
  FaHome,
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartBar,
  FaBell,
  FaUser,
  FaCog,
  FaSignOutAlt,
  FaUserShield
} from "react-icons/fa";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/Sidebar.css";

function Sidebar() {
  const location = useLocation();
  const { user, logout } = useAuth();
  
  const isActive = (path) => {
    return location.pathname === path ? "active-link" : "";
  };

  const isAdmin = user?.role === "ADMIN";

  return (
    <div className="sidebar glass-panel">
      <div className="logo-container">
        <div className="logo-icon">DH</div>
        <h2>DecisionHub</h2>
      </div>

      <nav className="nav-menu">
        <ul>
          <li className={isActive("/dashboard")}>
            <Link to="/dashboard">
              <FaHome /> <span>Dashboard</span>
            </Link>
          </li>

          {/* Admin Only Route */}
          {isAdmin && (
            <li className={isActive("/create-decision")}>
              <Link to="/create-decision">
                <FaPlusCircle /> <span>Create Decision</span>
              </Link>
            </li>
          )}

          <li className={isActive("/decisions")}>
            <Link to="/decisions">
              <FaCog /> <span>Manage Decisions</span>
            </Link>
          </li>

          <li className={isActive("/vote")}>
            <Link to="/vote">
              <FaVoteYea /> <span>Voting Room</span>
            </Link>
          </li>

          <li className={isActive("/communities")}>
            <Link to="/communities">
              <FaUsers /> <span>Communities</span>
            </Link>
          </li>

          <li className={isActive("/analytics")}>
            <Link to="/analytics">
              <FaChartBar /> <span>Analytics Panel</span>
            </Link>
          </li>

          <li className={isActive("/notifications")}>
            <Link to="/notifications">
              <FaBell /> <span>Notifications</span>
            </Link>
          </li>

          <li className={isActive("/profile")}>
            <Link to="/profile">
              <FaUser /> <span>My Profile</span>
            </Link>
          </li>

          <li className={isActive("/settings")}>
            <Link to="/settings">
              <FaCog /> <span>Settings</span>
            </Link>
          </li>
        </ul>
      </nav>

      {user && (
        <div className="user-profile-footer">
          <img src={user.photo} alt={user.username} className="footer-avatar" />
          <div className="footer-user-info">
            <span className="footer-username">{user.username}</span>
            <div className="footer-role-badge">
              {isAdmin ? (
                <span className="badge-role badge-admin"><FaUserShield /> Admin</span>
              ) : (
                <span className="badge-role badge-user"><FaUser /> User</span>
              )}
            </div>
          </div>
          <button onClick={logout} className="logout-btn" title="Logout">
            <FaSignOutAlt />
          </button>
        </div>
      )}
    </div>
  );
}

export default Sidebar;