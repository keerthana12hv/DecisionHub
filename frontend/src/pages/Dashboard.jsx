import { Link } from "react-router-dom";

function Dashboard() {
  return (
    <div style={{ padding: "30px", textAlign: "center" }}>
      <h1>DecisionHub Dashboard</h1>

      <hr />

      <h2>Welcome, User 👋</h2>

      <Link to="/create-decision">
        <button
          style={{
            padding: "10px 20px",
            backgroundColor: "#4F46E5",
            color: "white",
            border: "none",
            cursor: "pointer",
            borderRadius: "5px",
            marginTop: "10px",
          }}
        >
          Create Decision
        </button>
      </Link>

      <h2 style={{ marginTop: "30px" }}>Your Decisions</h2>

      <ul style={{ listStyle: "none", padding: 0 }}>
        <li>💻 Should I buy a Laptop?</li>
        <li>🎓 MBA or Job?</li>
        <li>📱 Android or iPhone?</li>
      </ul>
    </div>
  );
}

export default Dashboard;