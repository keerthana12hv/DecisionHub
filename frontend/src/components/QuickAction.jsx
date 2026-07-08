import {
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartPie
} from "react-icons/fa";

import { Link } from "react-router-dom";
import "../styles/QuickAction.css";

function QuickAction() {
  return (
    <>
      <h2 className="section-title">Quick Actions</h2>

      <div className="quick-grid">

        <Link to="/create-decision" className="quick-card">
          <FaPlusCircle className="quick-icon" />
          <h3>Create Decision</h3>
          <p>Create a new poll or decision.</p>
        </Link>

        <Link to="/vote" className="quick-card">
          <FaVoteYea className="quick-icon" />
          <h3>Vote Now</h3>
          <p>Participate in active decisions.</p>
        </Link>

        <Link to="/communities" className="quick-card">
          <FaUsers className="quick-icon" />
          <h3>Communities</h3>
          <p>Manage your communities.</p>
        </Link>

        <Link to="/analytics" className="quick-card">
          <FaChartPie className="quick-icon" />
          <h3>Analytics</h3>
          <p>View voting statistics.</p>
        </Link>

      </div>
    </>
  );
}

export default QuickAction;