import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import EditDecisionModal from "../components/EditDecisionModal";
import DeleteModal from "../components/DeleteModal";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import {
  FaEdit,
  FaTrash,
  FaVoteYea,
  FaChartBar,
  FaSearch,
  FaCopy,
  FaPlusCircle,
  FaArchive,
  FaCheck,
  FaChevronLeft,
  FaChevronRight,
  FaUndo
} from "react-icons/fa";
import { Link } from "react-router-dom";
import "../styles/DecisionList.css";

const STORAGE_KEY = "decisionhub-decisions";

function DecisionList() {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [searchParams] = useSearchParams();

  const [decisions, setDecisions] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("All"); // "All", "Active", "Closed"
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Modals state
  const [showDelete, setShowDelete] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [selectedDecision, setSelectedDecision] = useState(null);

  // Load from query params or localStorage
  useEffect(() => {
    const q = searchParams.get("search");
    if (q) setSearchQuery(q);

    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      setDecisions(JSON.parse(stored));
    }
  }, [searchParams]);

  const persistDecisions = (nextDecisions) => {
    setDecisions(nextDecisions);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextDecisions));
  };

  const isAdmin = user?.role === "ADMIN";

  // Actions
  const handleCloseReopenPoll = (id) => {
    if (!isAdmin) {
      addToast("Unauthorized: Admins only.", "error");
      return;
    }
    const updated = decisions.map((d) => {
      if (d.id === id) {
        const nextStatus = d.status === "Active" ? "Closed" : "Active";
        addToast(`Poll ${nextStatus === "Active" ? "reopened" : "closed"} successfully!`, "success");
        return { ...d, status: nextStatus };
      }
      return d;
    });
    persistDecisions(updated);
  };

  const handleDuplicate = (decision) => {
    if (!isAdmin) {
      addToast("Unauthorized: Admins only.", "error");
      return;
    }
    const newDec = {
      ...decision,
      id: Date.now(),
      title: `${decision.title} (Copy)`,
      userVoteOptionId: null,
      options: decision.options.map(o => ({ ...o, votes: 0 })),
      comments: []
    };
    persistDecisions([...decisions, newDec]);
    addToast("Decision duplicated successfully!", "success");
  };

  const handleShare = (id) => {
    const shareUrl = `${window.location.origin}/vote?id=${id}`;
    navigator.clipboard.writeText(shareUrl);
    addToast("Shareable link copied to clipboard!", "success");
  };

  const handleDelete = () => {
    if (!selectedDecision) return;
    const updated = decisions.filter((d) => d.id !== selectedDecision.id);
    persistDecisions(updated);
    addToast("Decision deleted successfully!", "success");
    setShowDelete(false);
    setSelectedDecision(null);
  };

  const handleEditSave = (updatedDecision) => {
    const updated = decisions.map((d) => d.id === updatedDecision.id ? updatedDecision : d);
    persistDecisions(updated);
    addToast("Decision updated successfully!", "success");
    setShowEdit(false);
    setSelectedDecision(null);
  };

  // Filter & Search Logic
  const filteredDecisions = decisions.filter((d) => {
    const matchesSearch = d.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          d.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = categoryFilter === "" || d.category === categoryFilter;
    const matchesStatus = statusFilter === "All" || d.status === statusFilter;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  // Pagination Logic
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
                <p>Browse active polls, inspect outcomes, duplicate proposals, or deploy new ones.</p>
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
                  <option>Education</option>
                  <option>Career</option>
                  <option>Technology</option>
                  <option>Business</option>
                  <option>Travel</option>
                  <option>Others</option>
                </select>

                <select className="filter" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}>
                  <option value="All">All Statuses</option>
                  <option value="Active">Active Only</option>
                  <option value="Closed">Closed Only</option>
                </select>

                {isAdmin && (
                  <Link to="/create-decision">
                    <button className="btn-primary create-decision-btn">
                      <FaPlusCircle /> Create Decision
                    </button>
                  </Link>
                )}
              </div>
            </div>

            <div className="decision-table-wrapper glass-panel">
              <table className="decision-table-element">
                <thead>
                  <tr>
                    <th>Decision Title</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Votes</th>
                    <th>Visibility</th>
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
                      const totalVotes = decision.options.reduce((sum, opt) => sum + opt.votes, 0);
                      return (
                        <tr key={decision.id}>
                          <td className="decision-title-col">
                            <span className="title-text">{decision.title}</span>
                            <span className="desc-preview">{decision.description}</span>
                          </td>
                          <td><span className="category-tag">{decision.category}</span></td>
                          <td>
                            <span className={`status-badge ${decision.status.toLowerCase()}`}>
                              {decision.status}
                            </span>
                          </td>
                          <td><span className="vote-count-txt">{totalVotes} votes</span></td>
                          <td>
                            <span className={`visibility-tag ${decision.visibility?.toLowerCase() || "public"}`}>
                              {decision.visibility || "Public"}
                            </span>
                          </td>
                          <td className="actions-col">
                            {/* Vote Action */}
                            <Link to="/vote" title="Vote Room">
                              <button className="action-row-btn-icon vote">
                                <FaVoteYea />
                              </button>
                            </Link>

                            {/* Analytics Action */}
                            <Link to="/analytics" title="View Analytics">
                              <button className="action-row-btn-icon analytics">
                                <FaChartBar />
                              </button>
                            </Link>

                            {/* Share Action */}
                            <button
                              className="action-row-btn-icon share"
                              onClick={() => handleShare(decision.id)}
                              title="Copy Share Link"
                            >
                              <FaCopy />
                            </button>

                            {/* Admin actions */}
                            {isAdmin && (
                              <>
                                <button
                                  className="action-row-btn-icon toggle-status"
                                  onClick={() => handleCloseReopenPoll(decision.id)}
                                  title={decision.status === "Active" ? "Close Poll" : "Reopen Poll"}
                                >
                                  {decision.status === "Active" ? <FaArchive /> : <FaUndo />}
                                </button>

                                <button
                                  className="action-row-btn-icon duplicate"
                                  onClick={() => handleDuplicate(decision)}
                                  title="Duplicate"
                                >
                                  <FaPlusCircle />
                                </button>

                                <button
                                  className="action-row-btn-icon edit"
                                  onClick={() => {
                                    setSelectedDecision(decision);
                                    setShowEdit(true);
                                  }}
                                  title="Edit"
                                >
                                  <FaEdit />
                                </button>

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
                              </>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>

              {/* Pagination Controls */}
              {totalPages > 1 && (
                <div className="pagination-bar">
                  <button
                    className="pagination-btn"
                    onClick={() => paginate(currentPage - 1)}
                    disabled={currentPage === 1}
                  >
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
                  <button
                    className="pagination-btn"
                    onClick={() => paginate(currentPage + 1)}
                    disabled={currentPage === totalPages}
                  >
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

      {showEdit && selectedDecision && (
        <EditDecisionModal
          decision={selectedDecision}
          onClose={() => { setShowEdit(false); setSelectedDecision(null); }}
          onSave={handleEditSave}
        />
      )}
    </div>
  );
}

export default DecisionList;