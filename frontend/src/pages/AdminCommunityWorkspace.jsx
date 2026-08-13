import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaTrash, FaArrowRight, FaUsers, FaShieldAlt } from "react-icons/fa";

export default function AdminCommunityWorkspace() {
  const { id: communityId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [community, setCommunity] = useState(null);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Modals state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [memberToRemove, setMemberToRemove] = useState(null);

  useEffect(() => {
    fetchCommunityData();
  }, [communityId]);

  const fetchCommunityData = async () => {
    try {
      setLoading(true);
      const [commRes, membersRes] = await Promise.all([
        api.get(`/api/communities/${communityId}`),
        api.get(`/api/communities/${communityId}/members`)
      ]);
      setCommunity(commRes.data);
      setMembers(membersRes.data || []);
    } catch (err) {
      console.error("Failed to fetch community workspace data:", err);
      addToast("Failed to load community workspace", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteCommunity = async () => {
    try {
      setActionLoading(true);
      await api.delete(`/api/communities/${communityId}`);
      addToast("Community deleted successfully", "success");
      setShowDeleteModal(false);
      navigate("/communities");
    } catch (err) {
      console.error("Failed to delete community:", err);
      addToast(err.response?.data?.message || "Failed to delete community", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemoveMember = async () => {
    if (!memberToRemove) return;
    try {
      setActionLoading(true);
      await api.delete(`/api/communities/${communityId}/members/${memberToRemove.memberId}`);
      addToast("Member removed successfully", "success");
      setMemberToRemove(null);
      // Refresh member list
      const membersRes = await api.get(`/api/communities/${communityId}/members`);
      setMembers(membersRes.data || []);
    } catch (err) {
      console.error("Failed to remove member:", err);
      addToast(err.response?.data?.message || "Failed to remove member", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleChangeRole = async (memberId, newRole) => {
    try {
      setActionLoading(true);
      await api.put(`/api/communities/${communityId}/members/${memberId}/role`, {
        role: newRole
      });
      addToast("Member role updated successfully", "success");
      // Refresh member list
      const membersRes = await api.get(`/api/communities/${communityId}/members`);
      setMembers(membersRes.data || []);
    } catch (err) {
      console.error("Failed to update role:", err);
      addToast(err.response?.data?.message || "Failed to update role", "error");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Loading community workspace...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!community) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Community not found.</p>
          </div>
        </div>
      </div>
    );
  }

  // Derive owner/moderator email and creation date from members list
  const ownerMember = members.find((m) => String(m.userId) === String(community.ownerId));
  const creationDate = ownerMember?.joinedAt
    ? new Date(ownerMember.joinedAt).toLocaleDateString()
    : "—";
  const ownerEmail = ownerMember?.email || "—";

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="community-page" style={{ maxWidth: "1000px", margin: "0 auto" }}>
            
            {/* Header / Overview Section */}
            <div className="community-header-sec" style={{ borderBottom: "1px solid var(--border-glass)", paddingBottom: "1.5rem" }}>
              <div style={{ flex: 1 }}>
                <span className="category-tag" style={{ marginBottom: "0.5rem", display: "inline-block" }}>
                  {community.categoryName}
                </span>
                <h1 style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  {community.name}
                </h1>
                <p style={{ marginTop: "0.5rem" }}>{community.description || "No description available."}</p>
              </div>
              <div>
                <button
                  className="btn-danger"
                  onClick={() => setShowDeleteModal(true)}
                  style={{
                    background: "rgba(239, 68, 68, 0.15)",
                    color: "#F87171",
                    border: "1px solid rgba(239, 68, 68, 0.4)",
                    padding: "8px 16px",
                    borderRadius: "8px",
                    cursor: "pointer",
                    fontWeight: "600",
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}
                >
                  <FaTrash /> Delete Community
                </button>
              </div>
            </div>

            {/* Grid details */}
            <div className="community-expanded-details glass-panel" style={{ margin: "2rem 0", background: "rgba(255,255,255,0.02)" }}>
              <div className="details-grid">
                <div className="details-item">
                  <span className="details-label">Visibility</span>
                  <span className="details-value">{community.visibility}</span>
                </div>
                <div className="details-item">
                  <span className="details-label">Date of Creation</span>
                  <span className="details-value">{creationDate}</span>
                </div>
                <div className="details-item">
                  <span className="details-label">Moderator Name</span>
                  <span className="details-value">{community.ownerUsername || "—"}</span>
                </div>
                <div className="details-item">
                  <span className="details-label">Moderator Email</span>
                  <span className="details-value">{ownerEmail}</span>
                </div>
                <div className="details-item">
                  <span className="details-label">Member Count</span>
                  <span className="details-value" style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                    <FaUsers /> {community.memberCount}
                  </span>
                </div>
                <div className="details-item">
                  <span className="details-label">Slug</span>
                  <span className="details-value">/{community.slug}</span>
                </div>
              </div>

              <div className="details-actions" style={{ justifyContent: "flex-start" }}>
                <button
                  className="btn-primary"
                  onClick={() => navigate(`/admin/communities/${communityId}/decisions`)}
                  style={{ display: "flex", alignItems: "center", gap: "8px" }}
                >
                  <FaShieldAlt /> View Decisions <FaArrowRight />
                </button>
              </div>
            </div>

            {/* Members Section */}
            <div style={{ marginTop: "3rem" }}>
              <h2 style={{ fontSize: "1.4rem", fontWeight: "700", marginBottom: "1.5rem", color: "var(--accent-purple, #A78BFA)" }}>
                Community Members
              </h2>

              <div className="decision-table-wrapper glass-panel">
                {members.length === 0 ? (
                  <p style={{ padding: "20px" }}>No members found</p>
                ) : (
                  <table className="decision-table-element">
                    <thead>
                      <tr>
                        <th style={{ textAlign: "left" }}>Username</th>
                        <th style={{ textAlign: "left" }}>Email</th>
                        <th style={{ textAlign: "left" }}>Role</th>
                        <th style={{ textAlign: "left" }}>Status</th>
                        <th style={{ textAlign: "left" }}>Joined</th>
                        <th style={{ textAlign: "right" }}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {members.map((member) => {
                        const isOwner = String(member.userId) === String(community.ownerId);
                        return (
                          <tr key={member.memberId}>
                            <td>{member.username}</td>
                            <td>{member.email}</td>
                            <td>
                              {isOwner ? (
                                <span className="owner-badge-pill" style={{ display: "inline-flex", flex: "none", padding: "2px 8px" }}>
                                  Owner
                                </span>
                              ) : (
                                <select
                                  value={member.role}
                                  onChange={(e) => handleChangeRole(member.memberId, e.target.value)}
                                  disabled={actionLoading}
                                  className="filter"
                                  style={{
                                    fontSize: "0.85rem",
                                    padding: "4px 8px",
                                    background: "rgba(255,255,255,0.05)",
                                    border: "1px solid var(--border-glass)",
                                    color: "white",
                                    borderRadius: "6px"
                                  }}
                                >
                                  <option value="MEMBER">Member</option>
                                  <option value="MODERATOR">Moderator</option>
                                </select>
                              )}
                            </td>
                            <td>
                              <span className={`status-badge ${member.status?.toLowerCase() === "approved" ? "active" : "draft"}`}>
                                {member.status}
                              </span>
                            </td>
                            <td>{member.joinedAt ? new Date(member.joinedAt).toLocaleDateString() : "—"}</td>
                            <td style={{ textAlign: "right" }}>
                              {!isOwner && (
                                <button
                                  className="btn-danger"
                                  onClick={() => setMemberToRemove(member)}
                                  disabled={actionLoading}
                                  style={{
                                    background: "none",
                                    border: "none",
                                    color: "#F87171",
                                    cursor: "pointer",
                                    fontSize: "0.95rem"
                                  }}
                                >
                                  Remove
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

          </div>
        </div>
      </div>

      {/* Delete Community Confirmation Modal */}
      {showDeleteModal && (
        <div className="forgot-modal-overlay" style={{ display: "flex", justifyContent: "center", alignItems: "center", position: "fixed", top: 0, left: 0, right: 0, bottom: 0, background: "rgba(0,0,0,0.6)", zIndex: 1000 }}>
          <div className="create-comm-modal glass-panel animate-pop-in" style={{ padding: "2rem", width: "400px", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
            <h2 style={{ fontSize: "1.35rem", fontWeight: "700" }}>Delete Community</h2>
            <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem" }}>
              Are you sure you want to delete this community?
            </p>
            <div className="modal-actions" style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
              <button
                type="button"
                className="btn-secondary"
                disabled={actionLoading}
                onClick={() => setShowDeleteModal(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn-primary"
                disabled={actionLoading}
                onClick={handleDeleteCommunity}
                style={{ background: "#EF4444" }}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Remove Member Confirmation Modal */}
      {memberToRemove && (
        <div className="forgot-modal-overlay" style={{ display: "flex", justifyContent: "center", alignItems: "center", position: "fixed", top: 0, left: 0, right: 0, bottom: 0, background: "rgba(0,0,0,0.6)", zIndex: 1000 }}>
          <div className="create-comm-modal glass-panel animate-pop-in" style={{ padding: "2rem", width: "400px", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
            <h2 style={{ fontSize: "1.35rem", fontWeight: "700" }}>Remove Member</h2>
            <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem" }}>
              Are you sure you want to remove <strong>{memberToRemove.username}</strong> from the community?
            </p>
            <div className="modal-actions" style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
              <button
                type="button"
                className="btn-secondary"
                disabled={actionLoading}
                onClick={() => setMemberToRemove(null)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn-primary"
                disabled={actionLoading}
                onClick={handleRemoveMember}
                style={{ background: "#EF4444" }}
              >
                Remove
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
