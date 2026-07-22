import { useState, useEffect } from "react";
import {
  getJoinRequests,
  approveRequest,
  rejectRequest,
  getMembers,
  removeMember
} from "../services/communityService";

export default function ModeratorPanel({ communityId }) {
  const [requests, setRequests] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAll();
  }, [communityId]);

  const fetchAll = async () => {
    try {
      setLoading(true);
      const [reqRes, memRes] = await Promise.all([
        getJoinRequests(communityId),
        getMembers(communityId)
      ]);
      setRequests(reqRes.data);
      setMembers(memRes.data);
    } catch (err) {
      console.error("Failed to load moderator data:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (memberId) => {
    try {
      await approveRequest(communityId, memberId);
      fetchAll();
    } catch (err) {
      console.error("Failed to approve request:", err);
    }
  };

  const handleReject = async (memberId) => {
    try {
      await rejectRequest(communityId, memberId);
      fetchAll();
    } catch (err) {
      console.error("Failed to reject request:", err);
    }
  };

  const handleRemoveMember = async (memberId) => {
    if (!window.confirm("Remove this member from the community?")) return;
    try {
      await removeMember(communityId, memberId);
      fetchAll();
    } catch (err) {
      console.error("Failed to remove member:", err);
    }
  };

  if (loading) return <p>Loading moderator panel...</p>;

  return (
    <div className="moderator-panel">
      <h3>Moderator Tools</h3>

      <section>
        <h4>Pending Join Requests ({requests.length})</h4>
        {requests.length === 0 ? (
          <p>No pending requests.</p>
        ) : (
          requests.map((r) => (
            <div key={r.id} className="request-row">
              <span>{r.username || r.email}</span>
              <button onClick={() => handleApprove(r.id)}>Approve</button>
              <button onClick={() => handleReject(r.id)}>Reject</button>
            </div>
          ))
        )}
      </section>

      <section>
        <h4>Members ({members.length})</h4>
        {members.map((m) => (
          <div key={m.id} className="member-row">
            <span>{m.username || m.email}</span>
            <button onClick={() => handleRemoveMember(m.id)}>Remove</button>
          </div>
        ))}
      </section>
    </div>
  );
}