import { FaRedo, FaExclamationCircle } from "react-icons/fa";

function ErrorPage({ error, resetErrorBoundary }) {
  return (
    <div className="login-container">
      <div className="glow-sphere main-glow"></div>
      <div className="glow-sphere sub-glow"></div>

      <div className="login-card glass-panel animate-fade-in" style={{ textAlign: "center", padding: "4rem 2rem" }}>
        <FaExclamationCircle style={{ fontSize: "3.5rem", color: "var(--danger)", marginBottom: "1.5rem" }} />

        <h2 style={{ fontFamily: "var(--font-title)", fontSize: "1.75rem", margin: "0 0 0.5rem" }}>Something went wrong</h2>
        
        <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem", margin: "0 0 2rem", lineHeight: 1.5 }}>
          An unexpected error has occurred. Details: <code style={{
            background: "rgba(255,255,255,0.06)",
            padding: "2px 6px",
            borderRadius: "4px",
            color: "var(--danger)"
          }}>{error?.message || "Unknown Application Error"}</code>
        </p>

        <button className="btn-primary" onClick={() => window.location.reload()} style={{ margin: "0 auto" }}>
          <FaRedo /> Reload Platform
        </button>
      </div>
    </div>
  );
}

export default ErrorPage;
