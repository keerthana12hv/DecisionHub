import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUsers, FaArrowRight, FaTimes, FaPlusCircle } from "react-icons/fa";
import "../styles/Communities.css";
import {
  getCommunities,
  createCommunity,
  joinCommunity,
  leaveCommunity,
} from "../services/communityService";

function Communities() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form states for creating community
  const [newName, setNewName] = useState("");
  const [newCategory, setNewCategory] = useState("");
  const [newVisibility, setNewVisibility] = useState("PUBLIC");

  useEffect(() => {
    loadCommunities();
  }, []);

  const loadCommunities = async () => {
    setLoading(true);
    try {
      const data = await getCommunities();
      setCommunities(data);
    } catch (error) {
      console.error(error);
      addToast("Failed to load communities", "error");
    } finally {
      setLoading(false);
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

    if (!newName || !newCategory) {
      addToast("Please fill in required fields.", "error");
      return;
    }

    try {
      const categoryMap = {
        Technology: 1,
        Education: 2,
        Career: 3,
        Business: 4,
        Travel: 5,
        Others: 6,
      };

      await createCommunity({
        name: newName,
        slug: newName.toLowerCase().replace(/\s+/g, "-"),
        description: "",
        categoryId: categoryMap[newCategory],
        visibility: newVisibility,
      });

      addToast("Community created successfully!", "success");

      // Reload communities from backend so the new one shows real data, not a guess
      await loadCommunities();

      setNewName("");
      setNewCategory("");
      setNewVisibility("PUBLIC");
      setShowCreateModal(false);
    } catch (error) {
      console.error(error);
      addToast(
        error.response?.data?.message || "Failed to create community",
        "error"
      );
    }
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
                <h1>Communities & Channels</h1>
                <p>Assemble with like-minded colleagues, coordinate objectives, and pin decisions.</p>
              </div>
              <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
                <FaPlusCircle /> Create Community
              </button>
            </div>

            {loading ? (
              <p>Loading communities...</p>
            ) : communities.length === 0 ? (
              <p className="empty-community-decisions">
                No communities yet. Be the first to create one.
              </p>
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
                <select value={newCategory} onChange={(e) => setNewCategory(e.target.value)} required>
                  <option value="">Select Category</option>
                  <option>Education</option>
                  <option>Career</option>
                  <option>Technology</option>
                  <option>Business</option>
                  <option>Travel</option>
                  <option>Others</option>
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