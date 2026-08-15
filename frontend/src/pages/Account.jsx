import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUser, FaEnvelope, FaUserShield, FaCheckCircle, FaMoon, FaSun, FaTimes } from "react-icons/fa";
import "../styles/Account.css";

function Account() {
  const { user, refreshProfile } = useAuth();
  const { addToast } = useToast();
  const [theme, setTheme] = useState(() => localStorage.getItem("decisionhub-theme") || "dark");

  // Modal State for Edit Profile
  const [showEditModal, setShowEditModal] = useState(false);
  const [editUsername, setEditUsername] = useState("");
  const [editEmail, setEditEmail] = useState("");
  const [editError, setEditError] = useState("");

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

  const handleOpenEditModal = () => {
    setEditUsername(user?.username || "");
    setEditEmail(user?.email || "");
    setEditError("");
    setShowEditModal(true);
  };

  const handleSaveChanges = (e) => {
    e.preventDefault();
    if (!editUsername.trim() || !editEmail.trim()) {
      setEditError("Username and Email are required.");
      return;
    }
    // Show validation error/status since backend does not expose update endpoint
    setEditError("Current backend does not expose a profile-update endpoint.");
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

              {/* Edit Profile button only for standard users (non-admin) */}
              {!isAdmin && (
                <div style={{ marginTop: "1rem" }}>
                  <button className="btn-secondary" onClick={handleOpenEditModal}>
                    Edit Profile
                  </button>
                </div>
              )}
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

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="forgot-modal-overlay">
          <div className="edit-profile-modal glass-panel animate-pop-in">
            <div className="modal-header">
              <h2>Edit Profile</h2>
              <button className="close-x-btn" onClick={() => setShowEditModal(false)}>
                <FaTimes />
              </button>
            </div>

            <form onSubmit={handleSaveChanges} className="profile-edit-form">
              {editError && (
                <div className="error-message" style={{ marginBottom: "1rem" }}>
                  <p>{editError}</p>
                </div>
              )}

              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  value={editUsername}
                  onChange={(e) => setEditUsername(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)}
                  required
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Account;
