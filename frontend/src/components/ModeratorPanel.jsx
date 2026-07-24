import DecisionModeration from "./moderator/DecisionModeration";
import CommunityMembers from "./moderator/CommunityMembers";
import CommunityRules from "./moderator/CommunityRules";
import { useState, useEffect } from "react";
import {
  getJoinRequests,
  approveRequest,
  rejectRequest
} from "../services/communityService";

export default function ModeratorPanel({ communityId }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAll();
  }, [communityId]);

  const fetchAll = async () => {
    try {
      setLoading(true);
      const reqRes = await getJoinRequests(communityId);
      setRequests(reqRes.data);
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
            <div key={r.memberId} className="request-row">
              <span>{r.username || r.email}</span>
              <button onClick={() => handleApprove(r.memberId)}>Approve</button>
              <button onClick={() => handleReject(r.memberId)}>Reject</button>
            </div>
          ))
        )}
      </section>

      <section>
        <CommunityMembers communityId={communityId} />
      </section>

      <section>
        <CommunityRules communityId={communityId} />
      </section>


      <section>
        <CommunityRules communityId={communityId} />
      </section>
    </div>
  );
}