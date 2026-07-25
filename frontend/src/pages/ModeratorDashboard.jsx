import { useState, useEffect } from "react";
import { FaShieldAlt, FaUsers, FaEye, FaTag, FaChevronDown, FaChevronUp } from "react-icons/fa";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import ModeratorPanel from "../components/ModeratorPanel";
import { getModeratingCommunities } from "../services/moderationService";
import { useToast } from "../components/Toast";
import "../styles/ModeratorPanel.css";

export default function ModeratorDashboard() {
  const { addToast } = useToast();
  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    fetchCommunities();
  }, []);

  const fetchCommunities = async () => {
    try {
      setLoading(true);
      const data = await getModeratingCommunities();
      setCommunities(data);
    } catch {
      addToast("Failed to load your communities", "error");
    } finally {
      setLoading(false);
    }
  };

  const toggleManage = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <div className="mod-dashboard">
          {/* Header */}
          <div className="mod-dashboard-header">
            <FaShieldAlt className="mod-dashboard-icon" />
            <div>
              <h1 className="mod-dashboard-title">Moderator Dashboard</h1>
              <p className="mod-dashboard-sub">
                Manage your communities, members, rules, and decisions.
              </p>
            </div>
          </div>

          {/* Section: My Moderating Communities */}
          <section className="mod-communities-section">
            <h2 className="mod-section-heading">
              <FaShieldAlt /> My Moderating Communities
              <span className="mod-badge">{communities.length}</span>
            </h2>

            {loading ? (
              <div className="mod-loading">Loading communities...</div>
            ) : communities.length === 0 ? (
              <div className="mod-empty-state">
                <FaShieldAlt size={40} />
                <p>You are not moderating any communities yet.</p>
                <small>
                  Create a community to automatically become its moderator.
                </small>
              </div>
            ) : (
              <div className="mod-community-list">
                {communities.map((community) => (
                  <div key={community.id} className="mod-community-card">
                    {/* Community Info Row */}
                    <div className="mod-community-info">
                      <div className="mod-community-avatar">
                        {community.name?.[0]?.toUpperCase() || "C"}
                      </div>
                      <div className="mod-community-details">
                        <h3 className="mod-community-name">{community.name}</h3>
                        <div className="mod-community-meta">
                          <span className="mod-meta-chip">
                            <FaEye />
                            &nbsp;
                            {community.visibility}
                          </span>
                          <span className="mod-meta-chip">
                            <FaTag />
                            &nbsp;
                            {community.category || "Uncategorized"}
                          </span>
                          <span className="mod-meta-chip">
                            <FaUsers />
                            &nbsp;
                            {community.memberCount ?? 0} members
                          </span>
                        </div>
                      </div>
                      <button
                        className={`mod-manage-btn ${
                          expandedId === community.id ? "active" : ""
                        }`}
                        onClick={() => toggleManage(community.id)}
                      >
                        {expandedId === community.id ? (
                          <>
                            <FaChevronUp /> Close
                          </>
                        ) : (
                          <>
                            <FaChevronDown /> Manage
                          </>
                        )}
                      </button>
                    </div>

                    {/* Expandable Moderator Panel */}
                    {expandedId === community.id && (
                      <div className="mod-panel-wrapper">
                        <ModeratorPanel
                          communityId={community.id}
                          visibility={community.visibility}
                        />
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}