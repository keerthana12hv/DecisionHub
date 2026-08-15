import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUser, FaEnvelope, FaUserShield, FaCheckCircle, FaMoon, FaSun } from "react-icons/fa";
import "../styles/Account.css";

function Account() {
  const { user, refreshProfile } = useAuth();
  const { addToast } = useToast();
  const [theme, setTheme] = useState(() => localStorage.getItem("decisionhub-theme") || "dark");

  const isAdmin = user?.role === "ADMIN";

  useEffect(() => {
    if (refreshProfile) {
      refreshProfile();
    }
  }, [refreshProfile]);

  // Apply theme globally and save to localStorage
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("decisionhub-theme", theme);
  }, [theme]);

  const toggleTheme = (selectedTheme) => {
    setTheme(selectedTheme);
    addToast(`Theme changed to ${selectedTheme} mode.`, "success");
  };

  if (!user) return null;

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="account-page-wrapper">
            <div className="account-header">
              <h1>Account Settings</h1>
              <p>Manage your profile and account preferences.</p>
            </div>

            <div className="account-card glass-panel">
              <div className="account-profile-section">
                <div className="default-avatar-circle">
                  <FaUser className="default-avatar-icon" />
                </div>
                <div className="account-profile-info">
                  <h2>{user.username}</h2>
                  <span className={`badge-role ${isAdmin ? "badge-admin" : "badge-user"}`}>
                    <FaUserShield /> {user.role}
                  </span>
                </div>
              </div>

              <div className="account-details-grid">
                <div className="account-detail-item">
                  <FaUser className="detail-icon" />
                  <div>
                    <label>Username</label>
                    <p>{user.username}</p>
                  </div>
                </div>
                <div className="account-detail-item">
                  <FaEnvelope className="detail-icon" />
                  <div>
                    <label>Email Address</label>
                    <p>{user.email}</p>
                  </div>
                </div>
                <div className="account-detail-item">
                  <FaUserShield className="detail-icon" />
                  <div>
                    <label>Role</label>
                    <p>{user.role}</p>
                  </div>
                </div>
                <div className="account-detail-item">
                  <FaCheckCircle className="detail-icon" />
                  <div>
                    <label>Status</label>
                    <p className="status-active">{user.status}</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="account-card glass-panel">
              <div className="theme-settings-section">
                <h3>Theme & Styling</h3>
                <p className="theme-desc">Choose between a dark background or a clean light background.</p>
                <div className="theme-toggle-options">
                  <button
                    className={`theme-btn ${theme === "dark" ? "active-theme" : ""}`}
                    onClick={() => toggleTheme("dark")}
                  >
                    <FaMoon /> Dark Theme
                  </button>
                  <button
                    className={`theme-btn ${theme === "light" ? "active-theme" : ""}`}
                    onClick={() => toggleTheme("light")}
                  >
                    <FaSun /> Light Theme
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Account;
