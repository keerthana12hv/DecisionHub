import { FaBell, FaSearch, FaUserCircle } from "react-icons/fa";
import "../styles/Navbar.css";

function Navbar() {
  return (
    <div className="navbar">

      <div className="search-box">
        <FaSearch />
        <input
          type="text"
          placeholder="Search decisions..."
        />
      </div>

      <div className="navbar-right">

        <div className="notification">
          <FaBell />
          <span className="badge">3</span>
        </div>

        <div className="profile">
          <FaUserCircle className="profile-icon" />
          <div>
            <h4>Mythili</h4>
            <p>User</p>
          </div>
        </div>

      </div>

    </div>
  );
}

export default Navbar;