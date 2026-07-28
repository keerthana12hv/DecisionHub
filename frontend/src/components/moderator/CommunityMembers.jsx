import { useState, useEffect } from "react";
import { FaUsers, FaUserMinus, FaShieldAlt } from "react-icons/fa";
import { getCommunityMembers, removeMember } from "../../services/moderationService";
import { useToast } from "../Toast";
import { useAuth } from "../../context/AuthContext";

// Confirmed from Swagger's CommunityMemberResponse schema: role is only ever
// "MODERATOR" or "MEMBER" — there is no "OWNER" value returned by the backend.
const ROLE_CONFIG = {
  MODERATOR: { label: "Moderator", icon: <FaShieldAlt />, className: "role-mod" },
  MEMBER: { label: "Member", icon: <FaUsers />, className: "role-member" },
};

export default function CommunityMembers({ communityId }) {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMembers();
  }, [communityId]);

  const fetchMembers = async () => {
    try {
      setLoading(true);
      const data = await getCommunityMembers(communityId);
      setMembers(data);
    } catch {
      addToast("Failed to load members", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (memberId, username) => {
    if (!window.confirm(`Remove ${username} from this community?`)) return;
    try {
      await removeMember(communityId, memberId);
      setMembers((prev) => prev.filter((m) => m.memberId !== memberId));
      addToast(`${username} removed from community`, "success");
    } catch {
      addToast("Failed to remove member", "error");
    }
  };

  const canRemove = (member) => {
    // Cannot remove self or another moderator
    if (member.userId === user?.id) return false;
    if (member.role === "MODERATOR") return false;
    return true;
  };

  if (loading) return <div className="mod-loading">Loading members...</div>;

  return (
    <div className="mod-section">
      <h3 className="mod-section-title">
        <FaUsers /> Community Members
        <span className="mod-badge">{members.length}</span>
      </h3>

      {members.length === 0 ? (
        <p className="mod-empty">No members found.</p>
      ) : (
        <div className="mod-list">
          {members.map((member) => {
            const roleConfig = ROLE_CONFIG[member.role] || ROLE_CONFIG.MEMBER;
            return (
              <div key={member.memberId} className="mod-card">
                <div className="mod-card-info">
                  <div className="mod-avatar">
                    {member.username?.[0]?.toUpperCase() || "U"}
                  </div>
                  <div>
                    <p className="mod-name">{member.username}</p>
                    <p className="mod-email">{member.email}</p>
                    <p className="mod-meta">
                      Joined:{" "}
                      {member.joinedAt
                        ? new Date(member.joinedAt).toLocaleDateString()
                        : "—"}
                    </p>
                  </div>
                </div>
                <div className="mod-card-right">
                  <span className={`mod-role-badge ${roleConfig.className}`}>
                    {roleConfig.icon} {roleConfig.label}
                  </span>
                  {canRemove(member) && (
                    <button
                      className="mod-btn mod-btn-reject"
                      onClick={() => handleRemove(member.memberId, member.username)}
                    >
                      <FaUserMinus /> Remove
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}