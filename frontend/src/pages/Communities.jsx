import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
//import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaUsers, FaUserPlus, FaArrowRight, FaTimes, FaGlobe, FaChevronRight, FaStar, FaPlusCircle, FaPaperPlane } from "react-icons/fa";
import "../styles/Communities.css";
import {
  getCommunities,
  createCommunity,
  joinCommunity,
  leaveCommunity,
} from "../services/communityService";
import api from "../services/api";

const STORAGE_KEY = "decisionhub-communities";

function Communities() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [communities, setCommunities] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [activeCommDetail, setActiveCommDetail] = useState(null); // holds community object for view mode

  // Form states for creating community
  const [newName, setNewName] = useState("");
  const [newCategory, setNewCategory] = useState("");
  const [newBanner, setNewBanner] = useState("");

  // Feed post state
  const [feedInput, setFeedInput] = useState("");

 useEffect(() => {
   loadCommunities();
 }, []);

 const loadCommunities = async () => {
   try {
     const data = await getCommunities();
     setCommunities(data);
   } catch (error) {
     console.error(error);
     addToast("Failed to load communities", "error");
   }
 };

  const persistCommunities = (nextComms) => {
    setCommunities(nextComms);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextComms));
  };

const handleJoinToggle = async (community) => {
  try {
    if (community.joined) {
      await leaveCommunity(community.id);

      setCommunities(prev =>
        prev.map(c =>
          c.id === community.id
            ? {
                ...c,
                joined: false,
                memberCount: c.memberCount - 1
              }
            : c
        )
      );

      addToast("Left community successfully!", "success");

    } else {
      await joinCommunity(community.id);

      setCommunities(prev =>
        prev.map(c =>
          c.id === community.id
            ? {
                ...c,
                joined: true,
                memberCount: c.memberCount + 1
              }
            : c
        )
      );

      addToast("Joined community successfully!", "success");
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
console.log("handleCreateCommunity called");
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
       visibility: "PUBLIC",
     });

     addToast("Community created successfully!", "success");

     // Reload communities from backend
     await loadCommunities();

     // Reset form
     setNewName("");
     setNewCategory("");
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
            {/* View Detail Panel mode */}
            {activeCommDetail ? (
              <div className="community-detail-panel animate-fade-in">
                <button className="btn-secondary back-btn-detail" onClick={() => setActiveCommDetail(null)}>
                  <FaTimes /> Close Community View
                </button>

                <div className="detail-banner-card glass-panel">
                  <img src={activeCommDetail.banner} alt={activeCommDetail.name} className="comm-banner-img" />
                  <div className="banner-overlay-info">
                    <span className="category-tag">{activeCommDetail.category}</span>
                    <h2>{activeCommDetail.name}</h2>
                    <p>{activeCommDetail.members} Active Members</p>
                  </div>
                </div>

                <div className="comm-detail-grid">
                  {/* Left: Feed & Discussions */}
                  <div className="comm-feed-col">
                    <div className="glass-card feed-container">
                      <h3>Community Activity Feed</h3>

                      {activeCommDetail.joined ? (
                        <form onSubmit={handlePostFeed} className="feed-post-form">
                          <input
                            type="text"
                            placeholder="Share an update, link or question with members..."
                            value={feedInput}
                            onChange={(e) => setFeedInput(e.target.value)}
                            required
                          />
                          <button type="submit" className="btn-primary">
                            <FaPaperPlane /> Post
                          </button>
                        </form>
                      ) : (
                        <div className="feed-locked-banner">
                          <p>You must join this community to post or interact in the feed.</p>
                          <button className="btn-primary" onClick={() => handleJoinToggle(activeCommDetail.id)}>
                            Join Community
                          </button>
                        </div>
                      )}

                      <div className="feed-feed-list">
                        {(activeCommDetail.feed || []).map((post) => (
                          <div key={post.id} className="feed-feed-item animate-pop-in">
                            <div className="feed-avatar">{post.user.charAt(0).toUpperCase()}</div>
                            <div>
                              <div className="feed-user-meta">
                                <strong>{post.user}</strong>
                                <span className="feed-time">Just now</span>
                              </div>
                              <p className="feed-text">{post.text}</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Right: Info, Admins & Members list */}
                  <div className="comm-info-col">
                    <div className="glass-card info-card-side">
                      <h3>Community Workspace</h3>
                      <div className="info-stat-row">
                        <span>Role Status</span>
                        {activeCommDetail.joined ? (
                          <span className="joined-indicator">Member</span>
                        ) : (
                          <span className="guest-indicator">Visitor</span>
                        )}
                      </div>
                      <button className={`btn-${activeCommDetail.joined ? "secondary" : "primary"} join-toggle-detail-btn`} onClick={() => handleJoinToggle(activeCommDetail.id)}>
                        {activeCommDetail.joined ? "Leave Community" : "Join Community"}
                      </button>
                    </div>

                    <div className="glass-card info-card-side">
                      <h3>Admins & Moderators</h3>
                      <div className="admin-member-list">
                        {activeCommDetail.admins.map((adm) => (
                          <div key={adm} className="member-list-item">
                            <div className="member-avatar-mini admin-av"><FaStar /></div>
                            <span>{adm}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              // Main grid communities list mode
              <>
                <div className="community-header-sec">
                  <div>
                    <h1>Communities & Channels</h1>
                    <p>Assemble with like-minded colleagues, coordinate objectives, and pin decisions.</p>
                  </div>
                  <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
                    <FaPlusCircle /> Create Community
                  </button>
                </div>

                <div className="community-grid">
                  {communities.map((community) => (
                    <div key={community.id} className="community-card glass-panel animate-pop-in">
                      <div className="comm-banner-header">
                        <img src={community.banner} alt={community.name} />
                      </div>

                      <div className="comm-body">
                        <span className="category-tag">{community.category}</span>
                        <h2>{community.name}</h2>

                        <div className="comm-stats-row">
                          <FaUsers />
                          <span>{community.members} Members</span>
                        </div>

                        <div className="community-buttons-sec">
                          <button className="btn-secondary view-btn" onClick={() => navigate(`/communities/${community.id}`)}>
                            <FaArrowRight /> View Workspace
                          </button>

                          <button
                            className={`btn-${community.joined ? "secondary" : "primary"} join-btn`}
                            onClick={() => handleJoinToggle(community)}
                          >
                            {community.joined ? "Leave" : "Join"}
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </>
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
                <label>Banner Image URL (Optional)</label>
                <input
                  type="text"
                  placeholder="https://images.unsplash.com/... or blank"
                  value={newBanner}
                  onChange={(e) => setNewBanner(e.target.value)}
                />
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