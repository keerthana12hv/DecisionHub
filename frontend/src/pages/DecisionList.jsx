import { useState, useEffect } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import DeleteModal from "../components/DeleteModal";
import { useToast } from "../components/Toast";
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
import "../styles/DecisionList.css";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

function DecisionList() {
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentUserId, setCurrentUserId] = useState(null);

  const [searchQuery, setSearchQuery] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("All");

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  const [showDelete, setShowDelete] = useState(false);
  const [selectedDecision, setSelectedDecision] = useState(null);

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
  }, [searchParams]);

  const fetchDecisions = async () => {
    try {
      setLoading(true);
      const res = await axios.get(`${API}/decisions`, headers());
      setDecisions(res.data);
    } catch (err) {
      console.error("Failed to fetch decisions:", err);
      addToast("Failed to load decisions", "error");
    } finally {
      setLoading(false);
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
      await axios.delete(`${API}/decisions/${selectedDecision.id}`, headers());
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

  // Category options built from real data, not hardcoded
  const categoryOptions = [...new Set(decisions.map((d) => d.categoryName).filter(Boolean))];

  const filteredDecisions = decisions.filter((d) => {
    const matchesSearch =
      d.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (d.description || "").toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = categoryFilter === "" || d.categoryName === categoryFilter;
    const matchesStatus = statusFilter === "All" || d.status === statusFilter;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  const totalPages = Math.ceil(filteredDecisions.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredDecisions.slice(indexOfFirstItem, indexOfLastItem);

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
                <h1>Decision Management</h1>
                <p>Browse active polls, inspect outcomes, or create new ones.</p>
              </div>

              <div className="decision-actions-header">
                <div className="search-box table-search">
                  <FaSearch />
                  <input
                    type="text"
                    placeholder="Search decision details..."
                    value={searchQuery}
                    onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
                  />
                </div>

                <select className="filter" value={categoryFilter} onChange={(e) => { setCategoryFilter(e.target.value); setCurrentPage(1); }}>
                  <option value="">All Categories</option>
                  {categoryOptions.map((cat) => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>

                <select className="filter" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}>
                  <option value="All">All Statuses</option>
                  <option value="DRAFT">Draft</option>
                  <option value="ACTIVE">Active</option>
                  <option value="CLOSED">Closed</option>
                  <option value="ARCHIVED">Archived</option>
                </select>

                <Link to="/create-decision">
                  <button className="btn-primary create-decision-btn">
                    <FaPlusCircle /> Create Decision
                  </button>
                </Link>
              </div>
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
                      <th>Status</th>
                      <th style={{ textAlign: "right" }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.length === 0 ? (
                      <tr>
                        <td colSpan="5" className="empty-table">No matching decisions found.</td>
                      </tr>
                    ) : (
                      currentItems.map((decision) => {
                        const isCreator = String(decision.creator?.id) === String(currentUserId);
                        return (
                          <tr key={decision.id}>
                            <td className="decision-title-col">
                              <span className="title-text">
                                {decision.pinned && <FaThumbtack title="Pinned" style={{ marginRight: 6, color: "#a5a0ff" }} />}
                                {decision.locked && <FaLock title="Locked" style={{ marginRight: 6, color: "#f87171" }} />}
                                {decision.title}
                              </span>
                              <span className="desc-preview">{decision.description}</span>
                            </td>
                            <td><span className="category-tag">{decision.categoryName}</span></td>
                            <td>{decision.communityName || "Personal"}</td>
                            <td>
                              <span className={`status-badge ${decision.status.toLowerCase()}`}>
                                {decision.status}
                              </span>
                            </td>
                            <td className="actions-col">
                              <button
                                className="action-row-btn-icon vote"
                                onClick={() => navigate(`/decision/${decision.id}`)}
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

                              {isCreator && (
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