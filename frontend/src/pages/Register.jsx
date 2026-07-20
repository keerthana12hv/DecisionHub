import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUser, FaEnvelope, FaLock, FaUserShield } from "react-icons/fa";
import "../styles/Register.css";
import api from "../services/api";

function Register() {
  const navigate = useNavigate();
  //const { register } = useAuth();
  const { addToast } = useToast();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("USER"); // Option to select Admin/User on register
  const [confirmPassword, setConfirmPassword] = useState("");

const handleRegister = async (e) => {
  e.preventDefault();

  if (!username || !email || !password || !confirmPassword) {
    addToast("Please fill in all fields.", "error");
    return;
  }

  if (password !== confirmPassword) {
    addToast("Passwords do not match.", "error");
    return;
  }

  if (password.length < 6) {
    addToast("Password must be at least 6 characters.", "error");
    return;
  }

  try {
    await api.post("/api/auth/register", {
      username,
      email,
      password,
    });

    addToast("Account created successfully!", "success");

    navigate("/login");

  } catch (error) {
    addToast(
      error.response?.data?.error ||
      error.response?.data?.message ||
      "Registration failed.",
      "error"
    );
  }
};

  return (
    <div className="login-container">
      <div className="glow-sphere main-glow"></div>
      <div className="glow-sphere sub-glow"></div>

      <div className="login-card glass-panel animate-fade-in">
        <div className="brand-header">
          <div className="brand-logo">DH</div>
          <h1>Create Account</h1>
          <p>Join DecisionHub and start polling today</p>
        </div>

        <form onSubmit={handleRegister} className="login-form">
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
            <FaUser className="input-icon" />
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
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

          <div className="input-group">
            <FaLock className="input-icon" />
            <input
              type="password"
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary login-btn">
            Register as {role === "ADMIN" ? "Admin" : "User"}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account?
          <Link to="/login"> Login Here</Link>
        </p>
      </div>
    </div>
  );
}

export default Register;