import { useState, useEffect, useCallback } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import api from "../services/api";
import {
  FaEdit, FaTrash, FaVoteYea, FaChartBar, FaSearch,
  FaCopy, FaPlusCircle, FaChevronLeft, FaChevronRight,
  FaSync, FaExternalLinkAlt,
} from "react-icons/fa";
import "../styles/DecisionList.css";

const PAGE_SIZE = 10;

export default function DecisionList() {
  const { user }      = useAuth();
  const { addToast }  = useToast();
  const navigate      = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [decisions,    setDecisions]    = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [searchQuery,  setSearchQuery]  = useState(searchParams.get("search") ?? "");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage,  setCurrentPage]  = useState(1);

  const isAdmin = user?.role === "ADMIN";

  // ── fetch from backend ────────────────────────────────────────────────────
  const fetchDecisions = useCallback(async () => {
    setLoading(true);
    try {
      const params = {};
      if (statusFilter !== "ALL") params.status = statusFilter;
      const res = await api.get("/api/decisions", { params });
      setDecisions(res.data ?? []);
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to load decisions.", "error");
    } finally {
      setLoading(false);
    }
  }, [statusFilter, addToast]);

  useEffect(() => { fetchDecisions(); }, [fetchDecisions]);

  // ── delete ────────────────────────────────────────────────────────────────
  const handleDelete = async (decision) => {
    if (!window.confirm(`Delete "${decision.title}"? This cannot be undone.`)) return;
    try {
      await api.delete(`/api/decisions/${decision.id}`);
      setDecisions((p) => p.filter((d) => d.id !== decision.id));
      addToast("Decision deleted.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Delete failed.", "error");
    }
  };

  // ── share ─────────────────────────────────────────────────────────────────
  const handleShare = (id) => {
    navigator.clipboard.writeText(`${window.location.origin}/decision/${id}`);
    addToast("Decision link copied!", "success");
  };

  // ── filter + paginate (client-side on the fetched list) ───────────────────
  const filtered = decisions.filter((d) => {
    const q = searchQuery.toLowerCase();
    return (
      d.title?.toLowerCase().includes(q) ||
      d.description?.toLowerCase().includes(q)
    );
  });

  const totalPages   = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage     = Math.min(currentPage, totalPages);
  const pageItems    = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const paginate = (n) => {
    if (n >= 1 && n <= totalPages) setCurrentPage(n);
  };

  const statusColor = (s) => {
    if (!s) return "";
    const map = { ACTIVE: "active", CLOSED: "closed", DRAFT: "draft", ARCHIVED: "archived" };
    return map[s.toUpperCase()] ?? s.toLowerCase();
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="decision-page">

            {/* ── Header ── */}
            <div className="decision-header">
              <div>
                <h1>Decision Management</h1>
                <p>All decisions from the database. Click a title to open its detail page.</p>
              </div>
              <div className="decision-actions-header">
                <div className="search-box table-search">
                  <FaSearch />
                  <input
                    type="text"
                    placeholder="Search decisions…"
                    value={searchQuery}
                    onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
                  />
                </div>

                <select
                  className="filter"
                  value={statusFilter}
                  onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}
                >
                  <option value="ALL">All Statuses</option>
                  <option value="ACTIVE">Active</option>
                  <option value="DRAFT">Draft</option>
                  <option value="CLOSED">Closed</option>
                  <option value="ARCHIVED">Archived</option>
                </select>

                <button className="vp-refresh-btn" onClick={fetchDecisions} disabled={loading} title="Refresh">
                  <FaSync className={loading ? "spin" : ""} />
                </button>

                {isAdmin && (
                  <Link to="/create-decision">
                    <button className="btn-primary create-decision-btn">
                      <FaPlusCircle /> Create Decision
                    </button>
                  </Link>
                )}
              </div>
            </div>

            {/* ── Table ── */}
            <div className="decision-table-wrapper glass-panel">
              {loading ? (
                <div className="dl-loading">
                  <FaSync className="spin" /> Loading decisions…
                </div>
              ) : (
                <table className="decision-table-element">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Decision Title</th>
                      <th>Type</th>
                      <th>Status</th>
                      <th>Options</th>
                      <th>Visibility</th>
                      <th style={{ textAlign: "right" }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pageItems.length === 0 ? (
                      <tr>
                        <td colSpan="7" className="empty-table">
                          {decisions.length === 0
                            ? "No decisions found. Create one to get started."
                            : "No decisions match your search."}
                        </td>
                      </tr>
                    ) : (
                      pageItems.map((d) => (
                        <tr key={d.id}>
                          {/* ID chip */}
                          <td>
                            <span className="decision-id-chip">#{d.id}</span>
                          </td>

                          {/* Clickable title → /decision/{id} */}
                          <td className="decision-title-col">
                            <button
                              className="decision-title-link"
                              onClick={() => navigate(`/decision/${d.id}`)}
                              title={`Open decision #${d.id}`}
                            >
                              {d.title}
                              <FaExternalLinkAlt className="title-ext-icon" />
                            </button>
                            {d.description && (
                              <span className="desc-preview">{d.description}</span>
                            )}
                          </td>

                          {/* Voting type */}
                          <td>
                            <span className={`voting-type-tag voting-type-tag--${d.votingType?.toLowerCase()}`}>
                              {d.votingType?.replace("_", " ") ?? "—"}
                            </span>
                          </td>

                          {/* Status */}
                          <td>
                            <span className={`status-badge ${statusColor(d.status)}`}>
                              {d.status}
                            </span>
                          </td>

                          {/* Option count */}
                          <td>
                            <span className="vote-count-txt">
                              {d.options?.length ?? 0} options
                            </span>
                          </td>

                          {/* Visibility */}
                          <td>
                            <span className={`visibility-tag ${d.visibility?.toLowerCase() ?? "public"}`}>
                              {d.visibility ?? "PUBLIC"}
                            </span>
                          </td>

                          {/* Actions */}
                          <td className="actions-col">
                            <button
                              className="action-row-btn-icon vote"
                              onClick={() => navigate(`/decision/${d.id}`)}
                              title="Open decision"
                            >
                              <FaVoteYea />
                            </button>

                            <button
                              className="action-row-btn-icon analytics"
                              onClick={() => navigate("/analytics")}
                              title="Analytics"
                            >
                              <FaChartBar />
                            </button>

                            <button
                              className="action-row-btn-icon share"
                              onClick={() => handleShare(d.id)}
                              title="Copy link"
                            >
                              <FaCopy />
                            </button>

                            {isAdmin && (
                              <>
                                <button
                                  className="action-row-btn-icon edit"
                                  onClick={() => navigate(`/decision/${d.id}`)}
                                  title="Edit (open detail)"
                                >
                                  <FaEdit />
                                </button>
                                <button
                                  className="action-row-btn-icon delete"
                                  onClick={() => handleDelete(d)}
                                  title="Delete"
                                >
                                  <FaTrash />
                                </button>
                              </>
                            )}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}

              {/* Pagination */}
              {!loading && totalPages > 1 && (
                <div className="pagination-bar">
                  <button className="pagination-btn" onClick={() => paginate(safePage - 1)} disabled={safePage === 1}>
                    <FaChevronLeft /> Prev
                  </button>
                  <div className="pagination-numbers">
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((n) => (
                      <button
                        key={n}
                        className={`pagination-num-btn${safePage === n ? " active-page" : ""}`}
                        onClick={() => paginate(n)}
                      >
                        {n}
                      </button>
                    ))}
                  </div>
                  <button className="pagination-btn" onClick={() => paginate(safePage + 1)} disabled={safePage === totalPages}>
                    Next <FaChevronRight />
                  </button>
                </div>
              )}
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}
