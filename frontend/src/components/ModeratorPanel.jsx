import { useState, useEffect } from "react";
import {
  FaShieldAlt, FaLock, FaLockOpen, FaThumbtack,
  FaUserMinus, FaCheck, FaTimes, FaUsers,
  FaSync, FaExclamationTriangle,
} from "react-icons/fa";
import { useToast } from "./Toast";
import {
  getJoinRequests,
  approveRequest,
  rejectRequest,
  getMembers,
  removeMember,
} from "../services/communityService";
import {
  pinDecision,
  unpinDecision,
  lockDiscussion,
  unlockDiscussion,
} from "../services/moderationService";

// ─── ConfirmModal ─────────────────────────────────────────────────────────────

function ConfirmModal({ message, onConfirm, onCancel }) {
  return (
    <div className="mod-modal-overlay" onClick={onCancel}>
      <div className="mod-modal glass-panel mod-modal--sm" onClick={(e) => e.stopPropagation()}>
        <div className="mod-modal__header">
          <FaExclamationTriangle />
          <h3>Confirm</h3>
        </div>
        <p className="mod-modal__sub">{message}</p>
        <div className="mod-modal__footer">
          <button className="btn-ghost" onClick={onCancel}>Cancel</button>
          <button className="btn-danger" onClick={onConfirm}>Confirm</button>
        </div>
      </div>
    </div>
  );
}

// ─── ModeratorPanel ───────────────────────────────────────────────────────────

export default function ModeratorPanel({ decisionId, communityId, isPinned, isLocked, onDecisionUpdate }) {
  const { addToast } = useToast();

  // Decision moderation state (mirrors what backend returns)
  const [pinned, setPinned]   = useState(isPinned ?? false);
  const [locked, setLocked]   = useState(isLocked ?? false);
  const [actionLoading, setActionLoading] = useState(null);

  // Community member management
  const [requests, setRequests] = useState([]);
  const [members,  setMembers]  = useState([]);
  const [loadingMod, setLoadingMod] = useState(false);
  const [confirm,  setConfirm]  = useState(null); // { message, onConfirm }

  // Sync props → local state when decision reloads
  useEffect(() => {
    setPinned(isPinned ?? false);
    setLocked(isLocked ?? false);
  }, [isPinned, isLocked]);

  // Load community member data only when a communityId is provided
  useEffect(() => {
    if (communityId) fetchMemberData();
  }, [communityId]);

  const fetchMemberData = async () => {
    setLoadingMod(true);
    try {
      const [reqRes, memRes] = await Promise.all([
        getJoinRequests(communityId),
        getMembers(communityId),
      ]);
      setRequests(reqRes.data ?? []);
      setMembers(memRes.data ?? []);
    } catch {
      // community may not require member management
    } finally {
      setLoadingMod(false);
    }
  };

  // ── Decision actions ──────────────────────────────────────────────────────

  const handleAction = async (action, label) => {
    setActionLoading(action);
    try {
      let res;
      if (action === "pin")    { res = await pinDecision(decisionId);    setPinned(true); }
      if (action === "unpin")  { res = await unpinDecision(decisionId);  setPinned(false); }
      if (action === "lock")   { res = await lockDiscussion(decisionId); setLocked(true); }
      if (action === "unlock") { res = await unlockDiscussion(decisionId); setLocked(false); }
      addToast(`${label} successful.`, "success");
      onDecisionUpdate?.(res?.data);
    } catch (err) {
      addToast(err?.response?.data?.message ?? `${label} failed.`, "error");
      // Revert optimistic state
      if (action === "pin")    setPinned(false);
      if (action === "unpin")  setPinned(true);
      if (action === "lock")   setLocked(false);
      if (action === "unlock") setLocked(true);
    } finally {
      setActionLoading(null);
    }
  };

  // ── Community member actions ──────────────────────────────────────────────

  const handleApprove = async (memberId) => {
    try {
      await approveRequest(communityId, memberId);
      setRequests((p) => p.filter((r) => r.id !== memberId));
      addToast("Request approved.", "success");
    } catch {
      addToast("Could not approve request.", "error");
    }
  };

  const handleReject = async (memberId) => {
    try {
      await rejectRequest(communityId, memberId);
      setRequests((p) => p.filter((r) => r.id !== memberId));
      addToast("Request rejected.", "success");
    } catch {
      addToast("Could not reject request.", "error");
    }
  };

  const handleRemove = (memberId, name) => {
    setConfirm({
      message: `Remove "${name}" from this community?`,
      onConfirm: async () => {
        setConfirm(null);
        try {
          await removeMember(communityId, memberId);
          setMembers((p) => p.filter((m) => m.id !== memberId));
          addToast("Member removed.", "success");
        } catch {
          addToast("Could not remove member.", "error");
        }
      },
    });
  };

  return (
    <>
      <div className="moderator-panel glass-panel">

        {/* ── Panel header ── */}
        <div className="mod-panel-header">
          <FaShieldAlt />
          <h3>Moderator Controls</h3>
        </div>

        {/* ── Discussion controls ── */}
        <div className="mod-section">
          <h4 className="mod-section-title">Discussion Controls</h4>
          <div className="mod-action-grid">

            {/* Pin / Unpin */}
            <button
              className={`mod-action-btn${pinned ? " mod-action-btn--active" : ""}`}
              disabled={actionLoading !== null}
              onClick={() => handleAction(pinned ? "unpin" : "pin", pinned ? "Unpin" : "Pin")}
            >
              {actionLoading === "pin" || actionLoading === "unpin" ? (
                <FaSync className="spin" />
              ) : (
                <FaThumbtack />
              )}
              <span>{pinned ? "Unpin Decision" : "Pin Decision"}</span>
              {pinned && <span className="mod-active-badge">Active</span>}
            </button>

            {/* Lock / Unlock */}
            <button
              className={`mod-action-btn${locked ? " mod-action-btn--danger" : ""}`}
              disabled={actionLoading !== null}
              onClick={() => handleAction(locked ? "unlock" : "lock", locked ? "Unlock" : "Lock")}
            >
              {actionLoading === "lock" || actionLoading === "unlock" ? (
                <FaSync className="spin" />
              ) : locked ? (
                <FaLockOpen />
              ) : (
                <FaLock />
              )}
              <span>{locked ? "Unlock Discussion" : "Lock Discussion"}</span>
              {locked && <span className="mod-active-badge mod-active-badge--danger">Locked</span>}
            </button>
          </div>
        </div>

        {/* ── Status strip ── */}
        <div className="mod-status-strip">
          <div className={`mod-status-chip${pinned ? " mod-status-chip--on" : ""}`}>
            <FaThumbtack /> {pinned ? "Pinned" : "Not pinned"}
          </div>
          <div className={`mod-status-chip${locked ? " mod-status-chip--danger" : ""}`}>
            <FaLock /> {locked ? "Locked" : "Open"}
          </div>
        </div>

        {/* ── Community member management (only when communityId provided) ── */}
        {communityId && (
          <>
            <div className="mod-section">
              <div className="mod-section-title-row">
                <h4 className="mod-section-title">
                  Pending Requests
                  {requests.length > 0 && (
                    <span className="mod-count-badge">{requests.length}</span>
                  )}
                </h4>
                <button
                  className="vp-refresh-btn"
                  onClick={fetchMemberData}
                  disabled={loadingMod}
                  title="Refresh"
                >
                  <FaSync className={loadingMod ? "spin" : ""} />
                </button>
              </div>

              {loadingMod ? (
                <p className="mod-loading"><FaSync className="spin" /> Loading…</p>
              ) : requests.length === 0 ? (
                <p className="mod-empty">No pending requests.</p>
              ) : (
                <div className="mod-list">
                  {requests.map((r) => (
                    <div key={r.id} className="mod-list-row">
                      <div className="mod-list-avatar">
                        {(r.username ?? r.email ?? "?")[0].toUpperCase()}
                      </div>
                      <span className="mod-list-name">{r.username ?? r.email}</span>
                      <div className="mod-list-actions">
                        <button
                          className="mod-icon-btn mod-icon-btn--approve"
                          onClick={() => handleApprove(r.id)}
                          title="Approve"
                        >
                          <FaCheck />
                        </button>
                        <button
                          className="mod-icon-btn mod-icon-btn--reject"
                          onClick={() => handleReject(r.id)}
                          title="Reject"
                        >
                          <FaTimes />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="mod-section">
              <h4 className="mod-section-title">
                <FaUsers /> Members
                <span className="mod-count-badge">{members.length}</span>
              </h4>
              {loadingMod ? (
                <p className="mod-loading"><FaSync className="spin" /> Loading…</p>
              ) : members.length === 0 ? (
                <p className="mod-empty">No members yet.</p>
              ) : (
                <div className="mod-list">
                  {members.map((m) => (
                    <div key={m.id} className="mod-list-row">
                      <div className="mod-list-avatar">
                        {(m.username ?? m.email ?? "?")[0].toUpperCase()}
                      </div>
                      <div className="mod-list-info">
                        <span className="mod-list-name">{m.username ?? m.email}</span>
                        {m.role && (
                          <span className={`mod-role-tag${m.role === "MODERATOR" ? " mod-role-tag--mod" : ""}`}>
                            {m.role}
                          </span>
                        )}
                      </div>
                      <button
                        className="mod-icon-btn mod-icon-btn--remove"
                        onClick={() => handleRemove(m.id, m.username ?? m.email)}
                        title="Remove member"
                      >
                        <FaUserMinus />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </div>

      {confirm && (
        <ConfirmModal
          message={confirm.message}
          onConfirm={confirm.onConfirm}
          onCancel={() => setConfirm(null)}
        />
      )}
    </>
  );
}
