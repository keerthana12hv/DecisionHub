import {
  FaHome,
  FaPlusCircle,
  FaUsers,
  FaChartBar,
  FaBell,
  FaUser,
  FaSignOutAlt,
  FaUserShield,
  FaCommentDots,
  FaGavel
} from "react-icons/fa";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/Sidebar.css";

function Sidebar() {
  const location = useLocation();
  const { user, logout } = useAuth();

  const isModerator = user?.role === "MODERATOR";
  const isAdmin = user?.role === "ADMIN";

  const isActive = (path) => {
    return location.pathname === path ? "active-link" : "";
  };
  // useEffect(() => {
  //   if (user && user.role === "MODERATOR") {
  //     api.get("/communities/moderating")
  //         .then(res => {
  //           if (res.data && res.data.length > 0) {
  //             setIsModerator(true);
  //           } else {
  //             setIsModerator(false);
  //           }
  //         })
  //         .catch(err => {
  //           console.error("Failed to fetch moderating communities:", err);
  //           setIsModerator(false);
  //         });
  //
  //   } else {
  //     setIsModerator(false);
  //   }
  // }, [user]);

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

          {/* Create Decision - Hidden for Admin */}
          {!isAdmin && (
            <li className={isActive("/create-decision")}>
              <Link to="/create-decision">
                <FaPlusCircle /> <span>Create Decision</span>
              </Link>
            </li>
          )}

          {/* Manage Decisions - Hidden for Admin */}
          {!isAdmin && (
            <li className={isActive("/decisions")}>
              <Link to="/decisions">
                <FaGavel /> <span>Manage Decisions</span>
              </Link>
            </li>
          )}
          {isModerator && (
            <li className={isActive("/moderator-dashboard")}>
              <Link to="/moderator-dashboard">
                <FaUserShield /> <span>Moderator</span>
              </Link>
            </li>
          )}

          <li className={isActive("/communities")}>
            <Link to="/communities">
              <FaUsers /> <span>Communities</span>
            </Link>
          </li>

          {isAdmin && (
            <li className={isActive("/admin/decisions")}>
              <Link to="/admin/decisions">
                <FaGavel /> <span>Public Decisions</span>
              </Link>
            </li>
          )}

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

          <li className={isActive("/account")}>
            <Link to="/account">
              <FaUser /> <span>Account</span>
            </Link>
          </li>

          <li className={isActive(isAdmin ? "/feedback-dashboard" : "/feedback")}>
            <Link to={isAdmin ? "/feedback-dashboard" : "/feedback"}>
              <FaCommentDots /> <span>Help & Feedback</span>
            </Link>
          </li>
        </ul>
      </nav>

      {user && (
        <div className="user-profile-footer">
          {user.photo ? (
            <img src={user.photo} alt={user.username} className="footer-avatar" />
          ) : (
            <div className="footer-avatar" style={{ display: "flex", alignItems: "center", justifyContent: "center", background: "var(--bg-tertiary)" }}>
              <FaUser style={{ fontSize: "14px", color: "var(--accent-purple)" }} />
            </div>
          )}
          <div className="footer-user-info">
            <span className="footer-username">{user.username}</span>
            <div className="footer-role-badge">
              {isAdmin ? (
                  <span className="badge-role badge-admin">
    <FaUserShield /> Admin
  </span>
              ) : isModerator ? (
                  <span className="badge-role badge-admin">
    <FaUserShield /> Moderator
  </span>
              ) : (
                  <span className="badge-role badge-user">
    <FaUser /> User
  </span>
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