import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUsers, FaArrowRight, FaTimes, FaPlusCircle, FaChevronDown } from "react-icons/fa";
import "../styles/Communities.css";
import {
  getCommunities,
  getCategories,
  createCommunity,
  joinCommunity,
  leaveCommunity,
} from "../services/communityService";
import { analyticsService } from "../services/analyticsService";

function Communities() {
  const { user, refreshProfile } = useAuth();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [expandedCommunityId, setExpandedCommunityId] = useState(null);
  const [moderationData, setModerationData] = useState({});

  const handleToggleExpand = (communityId) => {
    setExpandedCommunityId(prev => (prev === communityId ? null : communityId));
  };

  // Form states for creating community
  const [newName, setNewName] = useState("");
  const [selectedCategoryId, setSelectedCategoryId] = useState("");
  const [newVisibility, setNewVisibility] = useState("PUBLIC");
  const [newBanner, setNewBanner] = useState("");

  useEffect(() => {
    loadCommunities();

    loadCategories();
    // Membership approvals happen from a moderator's browser, not this one —
    // there's no push channel, so poll periodically while this page is open
    // so "Request Pending" flips to "Joined" without a manual refresh.
    const intervalId = setInterval(() => loadCommunities(true), 8000);
    return () => clearInterval(intervalId);
  }, [user]);

  const loadCategories = async () => {
    try {
      const data = await getCategories();
      setCategories(data);
    } catch (error) {
      console.error(error);
      addToast("Failed to load categories", "error");
    }
  };

  // silent=true skips the loading spinner for background refreshes so the
  // grid doesn't flash/reset every poll.
  const loadCommunities = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const data = await getCommunities();
      setCommunities(data);

      if (user?.role === "ADMIN") {
        const modPromises = data.map(async (community) => {
          try {
            const modInfo = await analyticsService.getCommunityModeration(community.id);
            return { id: community.id, info: modInfo };
          } catch (err) {
            console.error(`Failed to load moderation info for community ${community.id}:`, err);
            return { id: community.id, info: { reportedComments: 0 } };
          }
        });
        const results = await Promise.all(modPromises);
        const modMap = {};
        results.forEach((res) => {
          modMap[res.id] = res.info;
        });
        setModerationData(modMap);
      }
    } catch (error) {
      console.error(error);
      if (!silent) addToast("Failed to load communities", "error");
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const handleJoinToggle = async (community) => {
    try {
      if (community.isMember) {
        await leaveCommunity(community.id);
        setCommunities((prev) =>
          prev.map((c) =>
            c.id === community.id
              ? { ...c, isMember: false, memberCount: c.memberCount - 1 }
              : c
          )
        );
        addToast("Left community successfully!", "success");
      } else {
        await joinCommunity(community.id);
        const isPending = community.visibility === "PRIVATE";
        setCommunities((prev) =>
          prev.map((c) =>
            c.id === community.id
              ? isPending
                ? { ...c, requestPending: true }
                : { ...c, isMember: true, memberCount: c.memberCount + 1 }
              : c
          )
        );
        addToast(
          isPending ? "Join request sent!" : "Joined community successfully!",
          "success"
        );
      }
    } catch (error) {
      console.error(error);
      addToast(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Failed to update community",
        "error"
      );
    }
  };

  const handleCreateCommunity = async (e) => {
    e.preventDefault();

    if (!newName || !selectedCategoryId) {
      addToast("Please fill in required fields.", "error");
      return;
    }

    try {
      await createCommunity({
        name: newName,
        slug: newName.toLowerCase().replace(/\s+/g, "-"),
        description: "",
        categoryId: Number(selectedCategoryId),
        visibility: newVisibility,
      });

      addToast("Community created successfully!", "success");

      // Reload communities from backend so the new one shows real data, not a guess
      await loadCommunities();
      await refreshProfile();

      setNewName("");
      setSelectedCategoryId("");
      setNewVisibility("PUBLIC");
      setNewBanner("");
      setShowCreateModal(false);
    } catch (error) {
      console.error(error);
      addToast(
        error.response?.data?.message || "Failed to create community",
        "error"
      );
    }
  };

  // Add post inside community feed
  const handlePostFeed = (e) => {
    e.preventDefault();
    if (!feedInput.trim()) return;

    const updatedFeedItem = {
      id: Date.now(),
      user: user?.username || "Anonymous",
      text: feedInput.trim(),
      likes: 0,
      reactions: {}
    };

    const nextComms = communities.map((c) => {
      if (c.id === activeCommDetail.id) {
        const updatedFeed = [...(c.feed || []), updatedFeedItem];
        return { ...c, feed: updatedFeed };
      }
      return c;
    });

    persistCommunities(nextComms);
    setActiveCommDetail(nextComms.find(c => c.id === activeCommDetail.id));
    setFeedInput("");
    addToast("Post submitted to feed!", "success");
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="community-page">
            <div className="community-header-sec">
              <div>
                <h1>{user?.role === "ADMIN" ? "Community Management" : "Communities & Channels"}</h1>
                <p>
                  {user?.role === "ADMIN"
                    ? "Monitor community activity, check reported comments, and view workspaces."
                    : "Assemble with like-minded colleagues, coordinate objectives, and pin decisions."}
                </p>
              </div>
              {user?.role !== "ADMIN" && (
                <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
                  <FaPlusCircle /> Create Community
                </button>
              )}
            </div>

            {loading ? (
              <p>Loading communities...</p>
            ) : communities.length === 0 ? (
              <p className="empty-community-decisions">
                {user?.role === "ADMIN"
                  ? "No communities found on the platform."
                  : "No communities yet. Be the first to create one."}
              </p>
            ) : user?.role === "ADMIN" ? (
              <div className="decision-table-wrapper glass-panel admin-table-container">
                <table className="decision-table-element admin-communities-table">
                  <thead>
                    <tr>
                      <th style={{ width: "30%", textAlign: "left", padding: "1.25rem 1rem" }}>
                        Community
                      </th>

                      <th style={{ width: "17%", textAlign: "center", padding: "1.25rem 1rem" }}>
                        Moderator
                      </th>

                      <th style={{ width: "12%", textAlign: "center", padding: "1.25rem 1rem" }}>
                        Members
                      </th>

                      <th style={{ width: "17%", textAlign: "center", padding: "1.25rem 1rem" }}>
                        Reported Comments
                      </th>

                      <th style={{ width: "12%", textAlign: "center", padding: "1.25rem 1rem" }}>
                        Status
                      </th>

                      <th style={{ width: "12%", textAlign: "left", padding: "1.25rem 1rem" }}>
                        Action
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {communities.map((community) => {
                      const isExpanded = expandedCommunityId === community.id;
                      const reportedCount =
                        moderationData[community.id]?.reportedComments ?? 0;

                      return (
                        <tr
                          key={community.id}
                          className="admin-community-row-group"
                          style={{ borderBottom: "1px solid var(--border-glass)" }}
                        >
                          <td colSpan="6" style={{ padding: 0 }}>
                            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                              <tbody>
                                <tr className="admin-community-row">

                                  {/* Community - KEEP AS IT IS */}
                                  <td
                                    className="decision-title-col"
                                    style={{
                                      width: "30%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "center"
                                    }}
                                  >
                                    <span className="title-text">
                                      {community.name}
                                    </span>

                                    <span
                                      className={`desc-preview ${!community.description ? "empty-desc" : ""
                                        }`}
                                    >
                                      {community.description || "No description available"}
                                    </span>
                                  </td>

                                  {/* Moderator - CENTERED */}
                                  <td
                                    style={{
                                      width: "17%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "left"
                                    }}
                                  >
                                    {community.ownerUsername || "—"}
                                  </td>

                                  {/* Members - CENTERED */}
                                  <td
                                    style={{
                                      width: "12%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "left"
                                    }}
                                  >
                                    {community.memberCount}
                                  </td>

                                  {/* Reported Comments - CENTERED */}
                                  <td
                                    style={{
                                      width: "14%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "center"
                                    }}
                                  >
                                    {reportedCount}
                                  </td>

                                  {/* Status - CENTERED */}
                                  <td
                                    style={{
                                      width: "15%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "center"
                                    }}
                                  >
                                    <span className="status-badge active">
                                      Active
                                    </span>
                                  </td>

                                  {/* Action - KEEP POSITION */}
                                  <td
                                    style={{
                                      width: "12%",
                                      padding: "1.25rem 1rem",
                                      borderBottom: "none",
                                      textAlign: "left"
                                    }}
                                  >
                                    <button
                                      className="action-row-btn-icon dropdown-toggle-btn"
                                      onClick={() => handleToggleExpand(community.id)}
                                      title={isExpanded ? "Collapse" : "Expand"}
                                    >
                                      <FaChevronDown
                                        style={{
                                          transform: isExpanded
                                            ? "rotate(180deg)"
                                            : "rotate(0deg)",
                                          transition: "transform 0.2s"
                                        }}
                                      />
                                    </button>
                                  </td>

                                </tr>

                                {isExpanded && (
                                  <tr className="admin-community-details-row">
                                    <td
                                      colSpan="6"
                                      style={{
                                        padding: "0 1.75rem 1.25rem",
                                        borderBottom: "none"
                                      }}
                                    >
                                      <div className="community-expanded-details glass-panel animate-fade-in">

                                        <div className="details-grid">

                                          <div className="details-item">
                                            <span className="details-label">
                                              Visibility:
                                            </span>
                                            <span className="details-value">
                                              {community.visibility}
                                            </span>
                                          </div>

                                          <div className="details-item">
                                            <span className="details-label">
                                              Category:
                                            </span>
                                            <span
                                              className="category-tag"
                                              style={{ width: "fit-content" }}
                                            >
                                              {community.categoryName}
                                            </span>
                                          </div>

                                          <div className="details-item">
                                            <span className="details-label">
                                              Slug:
                                            </span>
                                            <span className="details-value">
                                              /{community.slug}
                                            </span>
                                          </div>

                                        </div>

                                        <div className="details-actions">
                                          <button
                                            className="btn-secondary"
                                            onClick={() =>
                                              navigate(`/communities/${community.id}`)
                                            }
                                          >
                                            <FaArrowRight /> View Workspace
                                          </button>
                                        </div>

                                      </div>
                                    </td>
                                  </tr>
                                )}

                              </tbody>
                            </table>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

            ) : (
              <div className="community-grid">
                {communities.map((community) => (
                  <div key={community.id} className="community-card glass-panel animate-pop-in">
                    <div className="comm-banner-header comm-banner-fallback">
                      <span>{community.name?.charAt(0).toUpperCase()}</span>
                    </div>

                    <div className="comm-body">
                      <span className="category-tag">{community.categoryName}</span>
                      <h2>{community.name}</h2>
                      {community.description && <p className="comm-description">{community.description}</p>}

                      <div className="comm-stats-row">
                        <FaUsers />
                        <span>{community.memberCount} Members</span>
                      </div>

                      <div className="community-buttons-sec">
                        <button
                          className="btn-secondary view-btn"
                          onClick={() => navigate(`/communities/${community.id}`)}
                        >
                          <FaArrowRight /> View Workspace
                        </button>

                        {String(community.ownerId) === String(user?.id) ? (
                          <span className="owner-badge-pill">Owner</span>
                        ) : (
                          <button
                            className={`btn-${community.isMember ? "secondary" : "primary"} join-btn`}
                            disabled={community.requestPending}
                            onClick={() => handleJoinToggle(community)}
                          >
                            {community.isMember
                              ? "Leave"
                              : community.requestPending
                                ? "Request Pending"
                                : community.visibility === "PRIVATE"
                                  ? "Request to Join"
                                  : "Join"}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Create Community Modal */}
      {showCreateModal && (
        <div className="forgot-modal-overlay">
          <div className="create-comm-modal glass-panel animate-pop-in">
            <div className="modal-header">
              <h2>Create New Community</h2>
              <button className="close-x-btn" onClick={() => setShowCreateModal(false)}>
                <FaTimes />
              </button>
            </div>
            <form onSubmit={handleCreateCommunity} className="comm-create-form">
              <div className="form-group">
                <label>Community Name</label>
                <input
                  type="text"
                  placeholder="e.g. Frontend Enthusiasts"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Category</label>
                <select value={selectedCategoryId} onChange={(e) => setSelectedCategoryId(e.target.value)} required>
                  <option value="">Select Category</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Visibility</label>
                <select value={newVisibility} onChange={(e) => setNewVisibility(e.target.value)}>
                  <option value="PUBLIC">Public (Anyone can join instantly)</option>
                  <option value="PRIVATE">Private (Requires approval to join)</option>
                </select>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Create Workspace
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Communities;