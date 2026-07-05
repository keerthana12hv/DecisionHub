import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

function Dashboard() {
  return (
    <div className="dashboard-container">
      <div className="dashboard-card">

        <h1>DecisionHub</h1>
        <h2>Dashboard</h2>

        <p>Welcome to your DecisionHub workspace 👋</p>

        <div className="dashboard-buttons">

          <Link to="/create-decision">
            <button>Create Decision</button>
          </Link>

          <Link to="/vote">
            <button>Voting Page</button>
          </Link>

          <Link to="/login">
            <button className="logout-btn">Logout</button>
          </Link>

        </div>

      </div>
    </div>
  );
}

export default Dashboard;