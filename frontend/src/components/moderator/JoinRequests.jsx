import { useState, useEffect } from "react";
import { FaCheck, FaTimes, FaClock, FaUserPlus } from "react-icons/fa";
import { getJoinRequests, approveRequest, rejectRequest } from "../../services/moderationService";
import { useToast } from "../Toast";

export default function JoinRequests({ communityId, visibility }) {
  const { addToast } = useToast();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (visibility === "PRIVATE") fetchRequests();
    else setLoading(false);
  }, [communityId, visibility]);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const data = await getJoinRequests(communityId);
      setRequests(data);
    } catch (err) {
      addToast("Failed to load join requests", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (memberId) => {
    try {
      await approveRequest(communityId, memberId);
      setRequests((prev) => prev.filter((r) => r.id !== memberId));
      addToast("Request approved!", "success");
    } catch {
      addToast("Failed to approve request", "error");
    }
  };

  const handleReject = async (memberId) => {
    try {
      await rejectRequest(communityId, memberId);
      setRequests((prev) => prev.filter((r) => r.id !== memberId));
      addToast("Request rejected", "info");
    } catch {
      addToast("Failed to reject request", "error");
    }
  };

  if (visibility !== "PRIVATE") {
    return (
      <div className="mod-section">
        <h3 className="mod-section-title">
          <FaUserPlus /> Pending Join Requests
        </h3>
        <p className="mod-empty">This is a public community — no join requests needed.</p>
      </div>
    );
  }

  if (loading) return <div className="mod-loading">Loading requests...</div>;

  return (
    <div className="mod-section">
      <h3 className="mod-section-title">
        <FaUserPlus /> Pending Join Requests
        {requests.length > 0 && (
          <span className="mod-badge">{requests.length}</span>
        )}
      </h3>

      {requests.length === 0 ? (
        <p className="mod-empty">No pending join requests.</p>
      ) : (
        <div className="mod-list">
          {requests.map((req) => (
            <div key={req.id} className="mod-card">
              <div className="mod-card-info">
                <div className="mod-avatar">{req.username?.[0]?.toUpperCase() || "U"}</div>
                <div>
                  <p className="mod-name">{req.username}</p>
                  <p className="mod-email">{req.email}</p>
                  <p className="mod-meta">
                    <FaClock size={10} />
                    &nbsp;Requested:{" "}
                    {new Date(req.requestedAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <div className="mod-actions">
                <button
                  className="mod-btn mod-btn-approve"
                  onClick={() => handleApprove(req.id)}
                >
                  <FaCheck /> Approve
                </button>
                <button
                  className="mod-btn mod-btn-reject"
                  onClick={() => handleReject(req.id)}
                >
                  <FaTimes /> Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}