import "./CommunityMembers.css";
import React, { useEffect, useState, useCallback } from "react";
import { getMembers, removeMember } from "../../services/communityService";
// This assumes CommunityMembers.jsx lives in src/components/moderator/
// (same folder as JoinRequestsPanel.jsx). If you place it elsewhere, adjust accordingly:
//   - in src/components/        -> "../services/communityService"
//   - in src/pages/             -> "../services/communityService"

export default function CommunityMembers({ communityId }) {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [removingId, setRemovingId] = useState(null); // tracks which row is mid-delete

  const fetchMembers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getMembers(communityId);
      setMembers(res.data);
    } catch (err) {
      console.error("Failed to fetch members:", err);
      setError("Could not load community members. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [communityId]);

  useEffect(() => {
    if (communityId) {
      fetchMembers();
    }
  }, [communityId, fetchMembers]);

  const handleRemove = async (memberId, username) => {
    const confirmed = window.confirm(
      `Remove ${username} from this community? This action cannot be undone.`
    );
    if (!confirmed) return;

    setRemovingId(memberId);
    setError(null);
    try {
      await removeMember(communityId, memberId);
      // Refresh list after successful removal
      await fetchMembers();
    } catch (err) {
      console.error("Failed to remove member:", err);
      setError(
        err.response?.data?.message ||
          "Could not remove member. Please try again."
      );
    } finally {
      setRemovingId(null);
    }
  };

  if (loading) {
    return <div className="members-loading">Loading members...</div>;
  }

  return (
    <div className="community-members">
      <h2>Community Members</h2>

      {error && <div className="members-error">{error}</div>}

      {members.length === 0 ? (
        <p className="members-empty">No members found in this community.</p>
      ) : (
        <table className="members-table">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Role</th>
              <th>Joined Date</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {members
              .filter((member) => member.status !== "PENDING") // pending requests belong on the Requests tab, not here
              .map((member) => {
                const isModerator = member.role === "MODERATOR";
                const isRemoving = removingId === member.memberId;

                return (
                  <tr key={member.memberId}>
                    <td>{member.username}</td>
                    <td>{member.email}</td>
                    <td>
                      <span className={`role-badge role-${member.role?.toLowerCase()}`}>
                        {member.role}
                      </span>
                    </td>
                    <td>
                      {member.joinedAt
                        ? new Date(member.joinedAt).toLocaleDateString()
                        : "-"}
                    </td>
                    <td>
                      {isModerator ? (
                        <span className="no-action">—</span>
                      ) : (
                        <button
                          className="remove-btn"
                          disabled={isRemoving}
                          onClick={() => handleRemove(member.memberId, member.username)}
                        >
                          {isRemoving ? "Removing..." : "Remove"}
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
  );
}
