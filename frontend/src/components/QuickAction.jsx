import {
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartPie,
  FaCog,
  FaUser
} from "react-icons/fa";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../styles/QuickAction.css";

function QuickAction() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  return (
    <div className="quick-actions-container">
      <h2 className="section-title">Quick Actions</h2>

      <div className="quick-grid">
        {isAdmin && (
          <Link to="/create-decision" className="quick-card glass-card">
            <div className="quick-icon-wrapper purple">
              <FaPlusCircle className="quick-icon" />
            </div>
            <h3>Create Decision</h3>
            <p>Draft a new decision poll and invite voters.</p>
          </Link>
        )}

        <Link to="/vote" className="quick-card glass-card">
          <div className="quick-icon-wrapper blue">
            <FaVoteYea className="quick-icon" />
          </div>
          <h3>Vote Now</h3>
          <p>Cast your vote on active polling questions.</p>
        </Link>

        <Link to="/communities" className="quick-card glass-card">
          <div className="quick-icon-wrapper green">
            <FaUsers className="quick-icon" />
          </div>
          <h3>Communities</h3>
          <p>Explore, join or manage group channels.</p>
        </Link>

        <Link to="/analytics" className="quick-card glass-card">
          <div className="quick-icon-wrapper yellow">
            <FaChartPie className="quick-icon" />
          </div>
          <h3>Analytics Panel</h3>
          <p>Visualize community voting participation trends.</p>
        </Link>

        {!isAdmin && (
          <>
            <Link to="/profile" className="quick-card glass-card">
              <div className="quick-icon-wrapper purple">
                <FaUser className="quick-icon" />
              </div>
              <h3>My Profile</h3>
              <p>View your votes, posts, and achievements.</p>
            </Link>
            
            <Link to="/settings" className="quick-card glass-card">
              <div className="quick-icon-wrapper blue">
                <FaCog className="quick-icon" />
              </div>
              <h3>Settings</h3>
              <p>Configure theme modes and security.</p>
            </Link>
          </>
        )}
      </div>
    </div>
  );
}

export default QuickAction;