import {
  FaHome,
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartBar,
  FaBell,
  FaUser,
  FaCog,
  FaSignOutAlt
} from "react-icons/fa";

import { Link } from "react-router-dom";
import "../styles/Sidebar.css";

function Sidebar() {
  return (
    <div className="sidebar">

      <div className="logo">
        <h2>DecisionHub</h2>
      </div>

      <ul>

        <li>
          <Link to="/dashboard">
            <FaHome /> Dashboard
          </Link>
        </li>

        <li>
          <Link to="/create-decision">
            <FaPlusCircle /> Create Decision
          </Link>
        </li>

        <li>
          <Link to="/vote">
            <FaVoteYea /> Voting
          </Link>
        </li>

        <li>
          <Link to="/communities">
            <FaUsers /> Communities
          </Link>
        </li>

        <li>
          <Link to="/analytics">
            <FaChartBar /> Analytics
          </Link>
        </li>

        <li>
          <Link to="/notifications">
            <FaBell /> Notifications
          </Link>
        </li>

        <li>
          <Link to="/profile">
            <FaUser /> Profile
          </Link>
        </li>

        <li>
          <Link to="/settings">
            <FaCog /> Settings
          </Link>
        </li>

      </ul>

      <div className="logout">

        <Link to="/login">
          <FaSignOutAlt /> Logout
        </Link>

      </div>

    </div>
  );
}

export default Sidebar;