import {
  FaPlusCircle,
  FaVoteYea,
  FaUsers
} from "react-icons/fa";
import { Link } from "react-router-dom";
import "../styles/QuickAction.css";

function QuickAction() {
  return (
    <div className="quick-actions-container">
      <h2 className="section-title">Quick Actions</h2>

      <div className="quick-grid">
        <Link to="/create-decision" className="quick-card glass-card">
          <div className="quick-icon-wrapper purple">
            <FaPlusCircle className="quick-icon" />
          </div>
          <h3>Create Decision</h3>
          <p>Draft a new decision poll and invite voters.</p>
        </Link>

        <Link to="/decisions?status=ACTIVE" className="quick-card glass-card">
          <div className="quick-icon-wrapper blue">
            <FaVoteYea className="quick-icon" />
          </div>
          <h3>Vote Now</h3>
          <p>Browse active decisions and cast your vote.</p>
        </Link>

        <Link to="/communities" className="quick-card glass-card">
          <div className="quick-icon-wrapper green">
            <FaUsers className="quick-icon" />
          </div>
          <h3>Communities</h3>
          <p>Explore, join or manage group channels.</p>
        </Link>
      </div>
    </div>
  );
}

export default QuickAction;