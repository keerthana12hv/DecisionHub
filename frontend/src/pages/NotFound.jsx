import { useNavigate } from "react-router-dom";
import { FaHome, FaExclamationTriangle } from "react-icons/fa";

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="login-container">
      <div className="glow-sphere main-glow"></div>
      <div className="glow-sphere sub-glow"></div>

      <div className="login-card glass-panel animate-fade-in" style={{ textAlign: "center", padding: "4rem 2rem" }}>
        <div style={{
          fontSize: "4.5rem",
          background: "var(--gradient-primary)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          fontWeight: 800,
          lineHeight: 1,
          marginBottom: "1rem"
        }}>404</div>

        <FaExclamationTriangle style={{ fontSize: "2.5rem", color: "var(--warning)", marginBottom: "1.5rem" }} />

        <h2 style={{ fontFamily: "var(--font-title)", fontSize: "1.75rem", margin: "0 0 0.5rem" }}>Page Not Found</h2>
        
        <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem", margin: "0 0 2rem", lineHeight: 1.5 }}>
          The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
        </p>

        <button className="btn-primary" onClick={() => navigate("/dashboard")} style={{ margin: "0 auto" }}>
          <FaHome /> Back to Dashboard
        </button>
      </div>
    </div>
  );
}

export default NotFound;
