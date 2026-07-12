import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUser, FaEnvelope, FaPhone, FaUserShield, FaAward, FaCalendarAlt, FaEdit, FaTimes, FaCamera } from "react-icons/fa";
import "../styles/Profile.css";

function Profile() {
  const { user, updateProfile } = useAuth();
  const { addToast } = useToast();

  const [showEdit, setShowEdit] = useState(false);
  const [editName, setEditName] = useState("");
  const [editPhone, setEditPhone] = useState("");
  const [editPhoto, setEditPhoto] = useState("");

  const [userDecisions, setUserDecisions] = useState([]);
  const [joinedComms, setJoinedComms] = useState([]);

  useEffect(() => {
    if (user) {
      setEditName(user.username);
      setEditPhone(user.phone || "");
      setEditPhoto(user.photo || "");

      // Load Decisions & Communities to show statistics dynamically
      const decisions = JSON.parse(localStorage.getItem("decisionhub-decisions") || "[]");
      const communities = JSON.parse(localStorage.getItem("decisionhub-communities") || "[]");

      setJoinedComms(communities.filter(c => c.joined));

      if (user.role === "ADMIN") {
        setUserDecisions(decisions); // Show all as admin created
      } else {
        // Show voted decisions
        setUserDecisions(decisions.filter(d => d.userVoteOptionId !== null));
      }
    }
  }, [user]);

  const handleSaveProfile = (e) => {
    e.preventDefault();
    if (!editName.trim()) {
      addToast("Display name is required.", "error");
      return;
    }
    updateProfile({
      username: editName.trim(),
      phone: editPhone.trim(),
      photo: editPhoto
    });
    addToast("Profile updated successfully!", "success");
    setShowEdit(false);
  };

  const handleAvatarChange = (avatarUrl) => {
    setEditPhoto(avatarUrl);
  };

  if (!user) return null;

  const isAdmin = user.role === "ADMIN";

  // Avatar Options presets
  const avatars = [
    `https://ui-avatars.com/api/?name=${user.username}&background=8B5CF6&color=fff&size=128`,
    `https://ui-avatars.com/api/?name=${user.username}&background=3B82F6&color=fff&size=128`,
    `https://ui-avatars.com/api/?name=${user.username}&background=10B981&color=fff&size=128`,
    `https://ui-avatars.com/api/?name=${user.username}&background=F59E0B&color=fff&size=128`
  ];

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="profile-page-wrapper">
            {/* Top Header Card */}
            <div className="profile-hero-card glass-panel">
              <div className="profile-hero-flex">
                <div className="profile-avatar-sec">
                  <img src={user.photo} alt={user.username} className="profile-hero-img" />
                  <button className="edit-avatar-btn" onClick={() => setShowEdit(true)} title="Edit Profile">
                    <FaCamera />
                  </button>
                </div>

                <div className="profile-hero-meta">
                  <div className="profile-name-role">
                    <h1>{user.username}</h1>
                    {isAdmin ? (
                      <span className="badge-role badge-admin"><FaUserShield /> Admin Role</span>
                    ) : (
                      <span className="badge-role badge-user"><FaUser /> User Role</span>
                    )}
                  </div>
                  <p className="joined-date-lbl">
                    <FaCalendarAlt /> Member since: {user.joinedDate || "July 2026"}
                  </p>
                  
                  <div className="profile-quick-stats">
                    <div className="quick-stat-box">
                      <strong>{isAdmin ? userDecisions.length : userDecisions.length}</strong>
                      <span>{isAdmin ? "Created Polls" : "Votes Cast"}</span>
                    </div>
                    <div className="quick-stat-box">
                      <strong>{joinedComms.length}</strong>
                      <span>Joined Communities</span>
                    </div>
                    {!isAdmin && (
                      <div className="quick-stat-box">
                        <strong>{user.stats?.participationRate || 85}%</strong>
                        <span>Participation</span>
                      </div>
                    )}
                  </div>
                </div>

                <button className="btn-secondary edit-profile-main-btn" onClick={() => setShowEdit(true)}>
                  <FaEdit /> Edit Profile
                </button>
              </div>
            </div>

            {/* Profile Grid content */}
            <div className="profile-content-grid">
              {/* Left Column: Contact details & Achievements */}
              <div className="profile-left-col-p">
                <div className="glass-card profile-details-card">
                  <h3>Account Credentials</h3>
                  <div className="detail-item-row">
                    <FaEnvelope />
                    <div>
                      <span>Email Address</span>
                      <p>{user.email}</p>
                    </div>
                  </div>
                  <div className="detail-item-row">
                    <FaPhone />
                    <div>
                      <span>Phone Number</span>
                      <p>{user.phone || "No phone added"}</p>
                    </div>
                  </div>
                </div>

                {/* Achievements */}
                <div className="glass-card profile-details-card">
                  <h3>Badges & Achievements</h3>
                  <div className="achievements-list-p">
                    {(user.stats?.achievements || ["Active Thinker"]).map((ach) => (
                      <div key={ach} className="achievement-badge-item">
                        <FaAward className="ach-icon" />
                        <span>{ach}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Right Column: Decisions History & Communities list */}
              <div className="profile-right-col-p">
                <div className="glass-card list-history-card">
                  <h3>{isAdmin ? "My Created Decisions" : "My Voted Decisions"}</h3>
                  <div className="history-items-list">
                    {userDecisions.length === 0 ? (
                      <p className="empty-history-msg">No decision records found.</p>
                    ) : (
                      userDecisions.map((dec) => (
                        <div key={dec.id} className="history-item-row animate-pop-in">
                          <div>
                            <h4>{dec.title}</h4>
                            <span>Category: {dec.category}</span>
                          </div>
                          <span className={`status-badge ${dec.status.toLowerCase()}`}>
                            {dec.status}
                          </span>
                        </div>
                      ))
                    )}
                  </div>
                </div>

                <div className="glass-card list-history-card">
                  <h3>Joined Communities</h3>
                  <div className="history-items-list">
                    {joinedComms.length === 0 ? (
                      <p className="empty-history-msg">No joined communities.</p>
                    ) : (
                      joinedComms.map((comm) => (
                        <div key={comm.id} className="history-item-row animate-pop-in">
                          <h4>{comm.name}</h4>
                          <span>{comm.members} members</span>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Edit Profile Modal */}
      {showEdit && (
        <div className="forgot-modal-overlay">
          <div className="edit-profile-modal glass-panel animate-pop-in">
            <div className="modal-header">
              <h2>Edit Profile</h2>
              <button className="close-x-btn" onClick={() => setShowEdit(false)}><FaTimes /></button>
            </div>
            
            <form onSubmit={handleSaveProfile} className="profile-edit-form">
              <div className="avatar-picker-section">
                <label>Select Avatar Color Theme</label>
                <div className="avatar-presets-grid">
                  {avatars.map((av, idx) => (
                    <img
                      key={idx}
                      src={av}
                      alt="avatar preset"
                      className={`preset-avatar-img ${editPhoto === av ? "selected-avatar" : ""}`}
                      onClick={() => handleAvatarChange(av)}
                    />
                  ))}
                </div>
              </div>

              <div className="form-group">
                <label>Display Username</label>
                <input
                  type="text"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  placeholder="+1 (555) 000-0000"
                  value={editPhone}
                  onChange={(e) => setEditPhone(e.target.value)}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowEdit(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Save Settings
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;