import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaMoon, FaSun, FaLock, FaGlobe, FaBell, FaShieldAlt } from "react-icons/fa";
import "../styles/Settings.css";

function Settings() {
  const { user, updateProfile } = useAuth();
  const { addToast } = useToast();

  const [theme, setTheme] = useState(() => localStorage.getItem("decisionhub-theme") || "dark");
  const [language, setLanguage] = useState("English");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [notifPreferences, setNotifPreferences] = useState({
    email: true,
    push: true,
    weeklyDigest: false
  });
  const [profilePrivate, setProfilePrivate] = useState(false);

  // Set the theme attribute on mount and theme change
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("decisionhub-theme", theme);
  }, [theme]);

  const toggleTheme = (selectedTheme) => {
    setTheme(selectedTheme);
    addToast(`Theme changed to ${selectedTheme} mode.`, "success");
  };

  const handlePasswordChange = (e) => {
    e.preventDefault();
    if (!oldPassword || !newPassword || !confirmPassword) {
      addToast("Please fill all password fields.", "error");
      return;
    }
    if (newPassword !== confirmPassword) {
      addToast("New passwords do not match.", "error");
      return;
    }
    if (newPassword.length < 6) {
      addToast("New password must be at least 6 characters.", "error");
      return;
    }
    addToast("Password changed successfully!", "success");
    setOldPassword("");
    setNewPassword("");
    setConfirmPassword("");
  };

  const savePreferences = () => {
    addToast("Preferences saved successfully.", "success");
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="settings-header">
            <h1>System Settings</h1>
            <p>Manage your account settings, UI theme, and privacy preferences.</p>
          </div>

          <div className="settings-grid">
            {/* Left: General Settings */}
            <div className="settings-left-col">
              {/* Theme Settings */}
              <div className="settings-card glass-panel">
                <div className="settings-section-title">
                  <FaMoon />
                  <h3>Theme & Styling</h3>
                </div>
                <p className="settings-desc">Choose between a dark background or a clean light background.</p>
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

              {/* Preferences Settings */}
              <div className="settings-card glass-panel">
                <div className="settings-section-title">
                  <FaGlobe />
                  <h3>Localization & Preferences</h3>
                </div>
                <div className="settings-row">
                  <label>Language</label>
                  <select value={language} onChange={(e) => setLanguage(e.target.value)}>
                    <option>English</option>
                    <option>Spanish</option>
                    <option>French</option>
                    <option>German</option>
                    <option>Hindi</option>
                  </select>
                </div>
                <button className="btn-primary" onClick={savePreferences}>Save Preferences</button>
              </div>

              {/* Notification preferences */}
              <div className="settings-card glass-panel">
                <div className="settings-section-title">
                  <FaBell />
                  <h3>Notification Channels</h3>
                </div>
                <div className="notif-toggle-list">
                  <label className="checkbox-container settings-checkbox">
                    <input
                      type="checkbox"
                      checked={notifPreferences.email}
                      onChange={(e) => setNotifPreferences({ ...notifPreferences, email: e.target.checked })}
                    />
                    <span className="checkmark"></span>
                    Email Notifications
                  </label>
                  <label className="checkbox-container settings-checkbox">
                    <input
                      type="checkbox"
                      checked={notifPreferences.push}
                      onChange={(e) => setNotifPreferences({ ...notifPreferences, push: e.target.checked })}
                    />
                    <span className="checkmark"></span>
                    Push Notifications
                  </label>
                  <label className="checkbox-container settings-checkbox">
                    <input
                      type="checkbox"
                      checked={notifPreferences.weeklyDigest}
                      onChange={(e) => setNotifPreferences({ ...notifPreferences, weeklyDigest: e.target.checked })}
                    />
                    <span className="checkmark"></span>
                    Weekly Analytics Digest
                  </label>
                </div>
                <button className="btn-primary" onClick={savePreferences}>Save Notification Channels</button>
              </div>
            </div>

            {/* Right: Security & Privacy */}
            <div className="settings-right-col">
              {/* Change Password */}
              <div className="settings-card glass-panel">
                <div className="settings-section-title">
                  <FaLock />
                  <h3>Security & Credentials</h3>
                </div>
                <form onSubmit={handlePasswordChange} className="password-form">
                  <div className="settings-row">
                    <label>Current Password</label>
                    <input
                      type="password"
                      placeholder="••••••••"
                      value={oldPassword}
                      onChange={(e) => setOldPassword(e.target.value)}
                      required
                    />
                  </div>
                  <div className="settings-row">
                    <label>New Password</label>
                    <input
                      type="password"
                      placeholder="Min. 6 characters"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                    />
                  </div>
                  <div className="settings-row">
                    <label>Confirm New Password</label>
                    <input
                      type="password"
                      placeholder="Confirm password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn-primary">Update Password</button>
                </form>
              </div>

              {/* Privacy settings */}
              <div className="settings-card glass-panel">
                <div className="settings-section-title">
                  <FaShieldAlt />
                  <h3>Privacy Controls</h3>
                </div>
                <div className="settings-row">
                  <label className="checkbox-container settings-checkbox">
                    <input
                      type="checkbox"
                      checked={profilePrivate}
                      onChange={(e) => setProfilePrivate(e.target.checked)}
                    />
                    <span className="checkmark"></span>
                    Make Profile Private (Admins only see your activities)
                  </label>
                </div>
                <button className="btn-primary" onClick={savePreferences}>Save Privacy settings</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Settings;
