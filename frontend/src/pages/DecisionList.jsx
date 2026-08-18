import { useState, useEffect } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import DeleteModal from "../components/DeleteModal";
import { useToast } from "../components/Toast";
import { useAuth } from "../context/AuthContext";
import {
  FaTrash,
  FaSearch,
  FaCopy,
  FaPlusCircle,
  FaChevronLeft,
  FaChevronRight,
  FaThumbtack,
  FaLock,
  FaArrowRight
} from "react-icons/fa";
import { getModeratingCommunities } from "../services/moderationService";
import "../styles/DecisionList.css";


  const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
function DecisionList() {
  const { addToast } = useToast();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const isAdmin = user?.role === "ADMIN";

  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentUserId, setCurrentUserId] = useState(null);

  const [searchQuery, setSearchQuery] = useState("");
  const [activeTab, setActiveTab] = useState("All Decisions");

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  const [showDelete, setShowDelete] = useState(false);
  const [selectedDecision, setSelectedDecision] = useState(null);
  const [moderatingCommunities, setModeratingCommunities] = useState([]);
  const [pollStatuses, setPollStatuses] = useState({});

  useEffect(() => {
    const q = searchParams.get("search");
    if (q) setSearchQuery(q);

    try {
      const payload = JSON.parse(atob(token().split(".")[1]));
      setCurrentUserId(payload.id);
    } catch (err) {
      console.error("Failed to decode token:", err);
    }

    fetchDecisions();
    fetchModeratingCommunities();
  }, [searchParams]);

  const fetchDecisions = async () => {
    try {
      setLoading(true);
      const res = await api.get("/decisions");
      const parsed = (res.data || []).map((d) => {
        if (!d.description) return d;
        const match = d.description.match(/^\[Cat:([^\]]+)\]\s*(.*)/s);
        if (match) {
          return {
            ...d,
            categoryName: match[1],
            description: match[2]
          };
        }
        return d;
      });
      setDecisions(parsed);

      // Fetch poll statuses in parallel
      const pollStatusesMap = {};
      await Promise.all(
        parsed.map(async (d) => {
          if (d.status === "DRAFT") {
            pollStatusesMap[d.id] = "—";
            return;
          }
          try {
            const pollRes = await api.get(`/decisions/${d.id}/poll`);
            const p = pollRes.data;
            const isExpired = p?.endTime ? (new Date() >= new Date(p.endTime)) : false;
            pollStatusesMap[d.id] = (p?.status === "OPEN" && !isExpired) ? "Active" : "Closed";
          } catch (err) {
            pollStatusesMap[d.id] = "—";
          }
        })
      );
      setPollStatuses(pollStatusesMap);
    } catch (err) {
      console.error("Failed to fetch decisions:", err);
      addToast("Failed to load decisions", "error");
    } finally {
      setLoading(false);
    }
  };

  const fetchModeratingCommunities = async () => {
    try {
      const data = await getModeratingCommunities();
      setModeratingCommunities(data || []);
    } catch (err) {
      console.error("Failed to fetch moderating communities:", err);
      setModeratingCommunities([]);
    }
  };

  const handleShare = (id) => {
    const shareUrl = `${window.location.origin}/decision/${id}`;
    navigator.clipboard.writeText(shareUrl);
    addToast("Shareable link copied to clipboard!", "success");
  };

  const handleDelete = async () => {
    if (!selectedDecision) return;
    try {
      await api.delete(`/decisions/${selectedDecision.id}`);
      addToast("Decision deleted successfully!", "success");
      setShowDelete(false);
      setSelectedDecision(null);
      fetchDecisions();
    } catch (err) {
      console.error("Failed to delete decision:", err);
      addToast(
        err.response?.data?.message || "Failed to delete decision",
        "error"
      );
    }
  };

  const filteredDecisions = decisions.filter((d) => {
    let matchesTab = true;
    if (activeTab === "My Decisions") {
      matchesTab = String(d.creator?.id) === String(currentUserId);
    } else if (activeTab === "Public Decisions") {
      matchesTab = !d.communityName;
    } else if (activeTab === "Community Decisions") {
      matchesTab = !!d.communityName;
    }

    const matchesSearch =
      d.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (d.description || "").toLowerCase().includes(searchQuery.toLowerCase());

    return matchesTab && matchesSearch;
  });

  const sortedFilteredDecisions = [...filteredDecisions].sort((a, b) => (a.pinned && !b.pinned ? -1 : !a.pinned && b.pinned ? 1 : 0));
  const totalPages = Math.ceil(sortedFilteredDecisions.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = sortedFilteredDecisions.slice(indexOfFirstItem, indexOfLastItem);

  const paginate = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= totalPages) {
      setCurrentPage(pageNumber);
    }
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="decision-page">
            <div className="decision-header">
              <div>
                <h1>Manage Decisions</h1>
                <p>View, manage and participate in your decisions</p>
              </div>
            </div>

            <div className="decision-search-row">
              <div className="search-box table-search">
                <FaSearch />
                <input
                  type="text"
                  placeholder="🔍 Search decisions..."
                  value={searchQuery}
                  onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
                />
              </div>
            </div>

            <div className="decision-tabs-row">
              <div className="decision-tabs">
                <button
                  className={`decision-tab-btn ${activeTab === "All Decisions" ? "active" : ""}`}
                  onClick={() => { setActiveTab("All Decisions"); setCurrentPage(1); }}
                >
                  All Decisions
                </button>
                <button
                  className={`decision-tab-btn ${activeTab === "My Decisions" ? "active" : ""}`}
                  onClick={() => { setActiveTab("My Decisions"); setCurrentPage(1); }}
                >
                  My Decisions
                </button>
                <button
                  className={`decision-tab-btn ${activeTab === "Public Decisions" ? "active" : ""}`}
                  onClick={() => { setActiveTab("Public Decisions"); setCurrentPage(1); }}
                >
                  Public Decisions
                </button>
                <button
                  className={`decision-tab-btn ${activeTab === "Community Decisions" ? "active" : ""}`}
                  onClick={() => { setActiveTab("Community Decisions"); setCurrentPage(1); }}
                >
                  Community Decisions
                </button>
              </div>

              {!isAdmin && (
                <Link to="/create-decision">
                  <button className="btn-primary create-decision-btn">
                    <FaPlusCircle /> Create Decision
                  </button>
                </Link>
              )}
            </div>

            <div className="decision-table-wrapper glass-panel">
              {loading ? (
                <p style={{ padding: "20px" }}>Loading decisions...</p>
              ) : (
                <table className="decision-table-element">
                  <thead>
                    <tr>
                      <th>Decision Title</th>
                      <th>Category</th>
                      <th>Community</th>
                      <th>Decision Status</th>
                      <th>Poll Status</th>
                      <th style={{ textAlign: "right" }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="empty-table">No matching decisions found.</td>
                      </tr>
                    ) : (
                      currentItems.map((decision) => {
                        const isCreator = String(decision.creator?.id) === String(currentUserId);
                        const isModerator = !!decision.communityName && moderatingCommunities.some((c) => c.name === decision.communityName);
                        return (
                          <tr key={decision.id}>
                            <td className="decision-title-col">
                              <div className="decision-title-container">
                                <span className="title-text">
                                  {decision.pinned && <FaThumbtack title="Pinned" style={{ marginRight: 6, color: "#a5a0ff" }} />}
                                  {decision.locked && <FaLock title="Locked" style={{ marginRight: 6, color: "#f87171" }} />}
                                  {decision.title}
                                </span>
                                <span className="desc-preview">{decision.description}</span>
                              </div>
                            </td>
                            <td>
                              {decision.categoryName ? (
                                <span className="category-tag">{decision.categoryName}</span>
                              ) : (
                                <span style={{ color: "var(--text-muted)", fontSize: "0.85rem" }}>—</span>
                              )}
                            </td>
                            <td>{decision.communityName || "Public"}</td>
                            <td>
                              <span className={`status-badge ${decision.status.toLowerCase()}`}>
                                {decision.status.charAt(0).toUpperCase() + decision.status.slice(1).toLowerCase()}
                              </span>
                            </td>
                            <td>
                              {pollStatuses[decision.id] === "Active" || pollStatuses[decision.id] === "Closed" ? (
                                <span className={`status-badge ${pollStatuses[decision.id].toLowerCase()}`}>
                                  {pollStatuses[decision.id]}
                                </span>
                              ) : (
                                <span style={{ color: "var(--text-muted)", fontSize: "0.85rem" }}>—</span>
                              )}
                            </td>
                            <td className="actions-col">
                              <div className="actions-btn-group">
                                <button
                                  className="action-row-btn-icon vote"
                                  onClick={() => navigate(`/decisions/${decision.id}`)}
                                  title="View Decision"
                                >
                                  <FaArrowRight />
                                </button>

                                <button
                                  className="action-row-btn-icon share"
                                  onClick={() => handleShare(decision.id)}
                                  title="Copy Share Link"
                                >
                                  <FaCopy />
                                </button>

                                {(isCreator || isModerator) && (
                                  <button
                                    className="action-row-btn-icon delete"
                                    onClick={() => {
                                      setSelectedDecision(decision);
                                      setShowDelete(true);
                                    }}
                                    title="Delete"
                                  >
                                    <FaTrash />
                                  </button>
                                )}
                              </div>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              )}

              {totalPages > 1 && (
                <div className="pagination-bar">
                  <button className="pagination-btn" onClick={() => paginate(currentPage - 1)} disabled={currentPage === 1}>
                    <FaChevronLeft /> Prev
                  </button>
                  <div className="pagination-numbers">
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((n) => (
                      <button
                        key={n}
                        className={`pagination-num-btn ${currentPage === n ? "active-page" : ""}`}
                        onClick={() => paginate(n)}
                      >
                        {n}
                      </button>
                    ))}
                  </div>
                  <button className="pagination-btn" onClick={() => paginate(currentPage + 1)} disabled={currentPage === totalPages}>
                    Next <FaChevronRight />
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {showDelete && selectedDecision && (
        <DeleteModal
          decisionTitle={selectedDecision.title}
          onCancel={() => { setShowDelete(false); setSelectedDecision(null); }}
          onDelete={handleDelete}
        />
      )}
    </div>
  );
}

export default DecisionList;