import { useState, useEffect, useCallback } from "react";
import {
  FaShieldAlt, FaLock, FaLockOpen, FaThumbtack,
  FaUserMinus, FaCheck, FaTimes, FaUsers, FaSync,
  FaExclamationTriangle,
} from "react-icons/fa";
import { useToast } from "./Toast";
import {
  getJoinRequests, approveRequest, rejectRequest,
  getMembers, removeMember,
} from "../services/communityService";
import {
  pinDecision, unpinDecision,
  lockDiscussion, unlockDiscussion,
  getPinnedComment, unpinComment,
} from "../services/moderationService";
import "../styles/ModeratorPanel.css";

// ─── ConfirmModal ─────────────────────────────────────────────────────────────

function ConfirmModal({ message, onConfirm, onCancel }) {
  return (
    <div className="disc-modal-overlay" onClick={onCancel}>
      <div className="disc-modal disc-modal--sm glass-panel" onClick={(e) => e.stopPropagation()}>
        <div className="disc-modal-header">
          <FaExclamationTriangle /> <h3>Confirm</h3>
        </div>
        <p className="disc-modal-sub">{message}</p>
        <div className="disc-modal-footer">
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

  // decision state — mirrors backend
  const [pinned,  setPinned]  = useState(isPinned ?? false);
  const [locked,  setLocked]  = useState(isLocked ?? false);
  const [actBusy, setActBusy] = useState(null); // which action is in-flight

  // pinned comment
  const [pinnedComment, setPinnedComment] = useState(null);
  const [loadingPinned, setLoadingPinned] = useState(false);

  // community management
  const [requests, setRequests] = useState([]);
  const [members,  setMembers]  = useState([]);
  const [loadingMod, setLoadingMod] = useState(false);

  const [confirm, setConfirm] = useState(null);

  // sync props when decision reloads externally
  useEffect(() => {
    setPinned(isPinned ?? false);
    setLocked(isLocked ?? false);
  }, [isPinned, isLocked]);

  // load pinned comment
  const fetchPinnedComment = useCallback(async () => {
    if (!decisionId) return;
    setLoadingPinned(true);
    try {
      const res = await getPinnedComment(decisionId);
      setPinnedComment(res.data ?? null);
    } catch {
      setPinnedComment(null);
    } finally {
      setLoadingPinned(false);
    }
  }, [decisionId]);

  useEffect(() => { fetchPinnedComment(); }, [fetchPinnedComment]);

  // load community data when communityId provided
  const fetchMemberData = useCallback(async () => {
    if (!communityId) return;
    setLoadingMod(true);
    try {
      const [reqRes, memRes] = await Promise.all([
        getJoinRequests(communityId),
        getMembers(communityId),
      ]);
      setRequests(reqRes.data ?? []);
      setMembers(memRes.data ?? []);
    } catch {
      // community may be open (no approval needed)
    } finally {
      setLoadingMod(false);
    }
  }, [communityId]);

  useEffect(() => { fetchMemberData(); }, [fetchMemberData]);

  // ── decision actions ──────────────────────────────────────────────────────

  const runAction = async (key, apiFn, optimisticFn, rollbackFn, label) => {
    setActBusy(key);
    optimisticFn();
    try {
      const res = await apiFn();
      addToast(`${label} successful.`, "success");
      onDecisionUpdate?.(res.data);
    } catch (err) {
      rollbackFn();
      addToast(err?.response?.data?.message ?? `${label} failed.`, "error");
    } finally {
      setActBusy(null);
    }
  };

  const handlePin = () =>
    runAction("pin", () => pinDecision(decisionId),
      () => setPinned(true), () => setPinned(false), "Pin");

  const handleUnpin = () =>
    runAction("unpin", () => unpinDecision(decisionId),
      () => setPinned(false), () => setPinned(true), "Unpin");

  const handleLock = () =>
    runAction("lock", () => lockDiscussion(decisionId),
      () => setLocked(true), () => setLocked(false), "Lock discussion");

  const handleUnlock = () =>
    runAction("unlock", () => unlockDiscussion(decisionId),
      () => setLocked(false), () => setLocked(true), "Unlock discussion");

  // ── unpin comment ─────────────────────────────────────────────────────────

  const handleUnpinComment = async () => {
    if (!pinnedComment) return;
    try {
      await unpinComment(pinnedComment.id);
      setPinnedComment(null);
      addToast("Comment unpinned.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Could not unpin comment.", "error");
    }
  };

  // ── community actions ─────────────────────────────────────────────────────

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

  const isBusy = (key) => actBusy === key;

  return (
    <>
      <div className="mod-panel glass-panel">

        {/* Header */}
        <div className="mod-panel-header">
          <FaShieldAlt /> <h3>Moderator Controls</h3>
        </div>

        {/* ── Decision actions ── */}
        <div className="mod-section">
          <p className="mod-section-label">Discussion Controls</p>
          <div className="mod-action-stack">

            {/* Pin / Unpin decision */}
            <button
              className={`mod-btn${pinned ? " mod-btn--active-yellow" : ""}`}
              disabled={actBusy !== null}
              onClick={pinned ? handleUnpin : handlePin}
            >
              {(isBusy("pin") || isBusy("unpin")) ? <FaSync className="spin" /> : <FaThumbtack />}
              <span>{pinned ? "Unpin Decision" : "Pin Decision"}</span>
              {pinned && <span className="mod-badge mod-badge--yellow">Pinned</span>}
            </button>

            {/* Lock / Unlock */}
            <button
              className={`mod-btn${locked ? " mod-btn--active-red" : ""}`}
              disabled={actBusy !== null}
              onClick={locked ? handleUnlock : handleLock}
            >
              {(isBusy("lock") || isBusy("unlock")) ? <FaSync className="spin" /> : (locked ? <FaLockOpen /> : <FaLock />)}
              <span>{locked ? "Unlock Discussion" : "Lock Discussion"}</span>
              {locked && <span className="mod-badge mod-badge--red">Locked</span>}
            </button>
          </div>

          {/* Status chips */}
          <div className="mod-status-row">
            <span className={`mod-chip${pinned ? " mod-chip--yellow" : ""}`}>
              <FaThumbtack /> {pinned ? "Pinned" : "Not pinned"}
            </span>
            <span className={`mod-chip${locked ? " mod-chip--red" : ""}`}>
              <FaLock /> {locked ? "Locked" : "Open"}
            </span>
          </div>
        </div>

        {/* ── Pinned comment ── */}
        <div className="mod-section">
          <div className="mod-section-row">
            <p className="mod-section-label">Pinned Comment</p>
            <button className="vp-refresh-btn" onClick={fetchPinnedComment} disabled={loadingPinned} title="Refresh">
              <FaSync className={loadingPinned ? "spin" : ""} />
            </button>
          </div>

          {loadingPinned ? (
            <p className="mod-muted"><FaSync className="spin" /> Loading…</p>
          ) : pinnedComment ? (
            <div className="mod-pinned-comment">
              <div className="mod-pinned-meta">
                <span className="mod-pinned-author">{pinnedComment.username}</span>
                <span className="mod-pinned-time">{new Date(pinnedComment.createdAt).toLocaleDateString()}</span>
              </div>
              <p className="mod-pinned-content">
                {pinnedComment.content.length > 120
                  ? pinnedComment.content.slice(0, 120) + "…"
                  : pinnedComment.content}
              </p>
              <button className="mod-btn mod-btn--sm" onClick={handleUnpinComment}>
                <FaThumbtack /> Unpin
              </button>
            </div>
          ) : (
            <p className="mod-muted">No comment is currently pinned.</p>
          )}
        </div>

        {/* ── Community member management ── */}
        {communityId && (
          <>
            <div className="mod-section">
              <div className="mod-section-row">
                <p className="mod-section-label">
                  Pending Requests
                  {requests.length > 0 && <span className="mod-count">{requests.length}</span>}
                </p>
                <button className="vp-refresh-btn" onClick={fetchMemberData} disabled={loadingMod} title="Refresh">
                  <FaSync className={loadingMod ? "spin" : ""} />
                </button>
              </div>

              {loadingMod ? (
                <p className="mod-muted"><FaSync className="spin" /> Loading…</p>
              ) : requests.length === 0 ? (
                <p className="mod-muted">No pending requests.</p>
              ) : (
                <div className="mod-list">
                  {requests.map((r) => (
                    <div key={r.id} className="mod-list-row">
                      <div className="mod-avatar">{(r.username ?? r.email ?? "?")[0].toUpperCase()}</div>
                      <span className="mod-list-name">{r.username ?? r.email}</span>
                      <div className="mod-list-btns">
                        <button className="mod-icon-btn mod-icon-btn--approve" title="Approve" onClick={() => handleApprove(r.id)}><FaCheck /></button>
                        <button className="mod-icon-btn mod-icon-btn--reject"  title="Reject"  onClick={() => handleReject(r.id)}><FaTimes /></button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="mod-section">
              <p className="mod-section-label">
                <FaUsers /> Members <span className="mod-count">{members.length}</span>
              </p>
              {loadingMod ? (
                <p className="mod-muted"><FaSync className="spin" /> Loading…</p>
              ) : members.length === 0 ? (
                <p className="mod-muted">No members yet.</p>
              ) : (
                <div className="mod-list">
                  {members.map((m) => (
                    <div key={m.id} className="mod-list-row">
                      <div className="mod-avatar">{(m.username ?? m.email ?? "?")[0].toUpperCase()}</div>
                      <div className="mod-list-info">
                        <span className="mod-list-name">{m.username ?? m.email}</span>
                        {m.role && (
                          <span className={`mod-role-tag${m.role === "MODERATOR" ? " mod-role-tag--mod" : ""}`}>
                            {m.role}
                          </span>
                        )}
                      </div>
                      <button className="mod-icon-btn mod-icon-btn--remove" title="Remove"
                        onClick={() => handleRemove(m.id, m.username ?? m.email)}>
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
