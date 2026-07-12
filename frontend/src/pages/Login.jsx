import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaGoogle, FaUserShield, FaUser, FaLock, FaEnvelope } from "react-icons/fa";
import "../styles/Login.css";

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { addToast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("USER"); // Default role
  const [rememberMe, setRememberMe] = useState(false);
  const [showForgot, setShowForgot] = useState(false);
  const [forgotEmail, setForgotEmail] = useState("");

  const handleLogin = (e) => {
    e.preventDefault();
    if (!email || !password) {
      addToast("Please fill in all fields.", "error");
      return;
    }
    
    // JWT Ready simulated authentication
    login(email, password, role, rememberMe);
    addToast(`Successfully logged in as ${role === "ADMIN" ? "Admin" : "User"}!`, "success");
    navigate("/dashboard");
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    if (!forgotEmail) {
      addToast("Please enter your email.", "error");
      return;
    }
    addToast(`Password reset link sent to ${forgotEmail}`, "info");
    setShowForgot(false);
    setForgotEmail("");
  };

  return (
    <div className="login-container">
      <div className="glow-sphere main-glow"></div>
      <div className="glow-sphere sub-glow"></div>

      <div className="login-card glass-panel animate-fade-in">
        <div className="brand-header">
          <div className="brand-logo">DH</div>
          <h1>DecisionHub</h1>
          <p>Collaborative Polling & Decision Making Platform</p>
        </div>

        <form onSubmit={handleLogin} className="login-form">
          {/* Role Selection */}
          <div className="role-selection">
            <button
              type="button"
              className={`role-btn ${role === "USER" ? "active-user" : ""}`}
              onClick={() => setRole("USER")}
            >
              <FaUser /> User
            </button>
            <button
              type="button"
              className={`role-btn ${role === "ADMIN" ? "active-admin" : ""}`}
              onClick={() => setRole("ADMIN")}
            >
              <FaUserShield /> Admin
            </button>
          </div>

          <div className="input-group">
            <FaEnvelope className="input-icon" />
            <input
              type="email"
              placeholder="Email Address"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="input-group">
            <FaLock className="input-icon" />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="login-options">
            <label className="checkbox-container">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <span className="checkmark"></span>
              Remember Me
            </label>
            <button
              type="button"
              className="forgot-link"
              onClick={() => setShowForgot(true)}
            >
              Forgot Password?
            </button>
          </div>

          <button type="submit" className="btn-primary login-btn">
            Login as {role === "ADMIN" ? "Admin" : "User"}
          </button>
        </form>

        <div className="separator">
          <span>or continue with</span>
        </div>

        <button
          type="button"
          className="btn-secondary google-btn"
          onClick={() => addToast("Google authentication is UI-only in mock mode.", "info")}
        >
          <FaGoogle /> Google Login
        </button>

        <p className="auth-footer">
          Don't have an account?
          <Link to="/register"> Register Now</Link>
        </p>
      </div>

      {showForgot && (
        <div className="forgot-modal-overlay">
          <div className="forgot-modal glass-panel animate-pop-in">
            <h2>Reset Password</h2>
            <p>Enter your email to receive a password reset link.</p>
            <form onSubmit={handleForgotPassword}>
              <div className="input-group">
                <FaEnvelope className="input-icon" />
                <input
                  type="email"
                  placeholder="name@domain.com"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                  required
                />
              </div>
              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowForgot(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Send Link
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Login;