import { useState, useEffect, useRef } from "react";
import {
  FaComments, FaReply, FaTrash, FaFlag, FaLock,
  FaThumbtack, FaEyeSlash, FaEye, FaShieldAlt,
  FaExclamationTriangle, FaTimes, FaSync,
} from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { getComments, postComment, postReply, deleteComment, pinComment, unpinComment, hideComment } from "../services/commentService";
import { reportContent } from "../services/moderationService";
import "../styles/Discussion.css";

// ─── helpers ──────────────────────────────────────────────────────────────────

const avatar = (name = "?") => (name[0] ?? "?").toUpperCase();

const timeAgo = (iso) => {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const mins  = Math.floor(diff / 60000);
  if (mins < 1)  return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
};

// ─── ReportModal ──────────────────────────────────────────────────────────────

function ReportModal({ target, onClose, onSubmit }) {
  const reasons = [
    "Spam or advertising",
    "Harassment or hate speech",
    "Misinformation",
    "Off-topic content",
    "Other",
  ];
  const [selected, setSelected] = useState("");
  const [custom, setCustom]     = useState("");
  const [loading, setLoading]   = useState(false);

  const handleSubmit = async () => {
    const reason = selected === "Other" ? custom.trim() : selected;
    if (!reason) return;
    setLoading(true);
    await onSubmit(reason);
    setLoading(false);
  };

  return (
    <div className="mod-modal-overlay" onClick={onClose}>
      <div className="mod-modal glass-panel" onClick={(e) => e.stopPropagation()}>
        <div className="mod-modal__header">
          <FaFlag />
          <h3>Report Comment</h3>
          <button className="mod-modal__close" onClick={onClose}><FaTimes /></button>
        </div>
        <p className="mod-modal__sub">
          Select a reason for reporting{target ? ` "${target}"` : ""}.
        </p>
        <div className="mod-reason-list">
          {reasons.map((r) => (
            <label key={r} className={`mod-reason-item${selected === r ? " selected" : ""}`}>
              <input
                type="radio"
                name="reason"
                value={r}
                checked={selected === r}
                onChange={() => setSelected(r)}
              />
              {r}
            </label>
          ))}
        </div>
        {selected === "Other" && (
          <textarea
            className="mod-custom-reason"
            placeholder="Describe the issue…"
            value={custom}
            onChange={(e) => setCustom(e.target.value)}
          />
        )}
        <div className="mod-modal__footer">
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button
            className="btn-danger"
            disabled={!selected || loading || (selected === "Other" && !custom.trim())}
            onClick={handleSubmit}
          >
            {loading ? "Submitting…" : "Submit Report"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── CommentCard ──────────────────────────────────────────────────────────────

function CommentCard({
  comment, currentUserId, isMod, isLocked,
  onDelete, onPin, onHide, onReport,
}) {
  const [showReply, setShowReply]   = useState(false);
  const [replyText, setReplyText]   = useState("");
  const [replying, setReplying]     = useState(false);
  const [reporting, setReporting]   = useState(false);
  const [localHidden, setLocalHidden] = useState(comment.hidden ?? false);
  const [localPinned, setLocalPinned] = useState(comment.pinned ?? false);
  const [showHidden, setShowHidden] = useState(false);
  const textareaRef = useRef(null);

  const isOwn = String(comment.userId) === String(currentUserId);
  const canDelete = isOwn || isMod;

  const handleReplySubmit = async () => {
    if (!replyText.trim() || replying) return;
    setReplying(true);
    await postReply(comment.id, replyText).catch(() => {});
    setReplyText("");
    setShowReply(false);
    setReplying(false);
  };

  const handlePin = async () => {
    const next = !localPinned;
    setLocalPinned(next);
    await onPin(comment.id, next);
  };

  const handleHide = async () => {
    const next = !localHidden;
    setLocalHidden(next);
    await onHide(comment.id, next);
  };

  // Hidden comments: show placeholder with toggle for mods
  if (localHidden && !isMod && !isOwn) {
    return (
      <div className="comment-card comment-card--hidden">
        <FaEyeSlash />
        <span>This comment was hidden by a moderator.</span>
      </div>
    );
  }

  return (
    <>
      <div className={[
        "comment-card",
        localPinned  ? "comment-card--pinned"  : "",
        localHidden  ? "comment-card--hidden-mod" : "",
      ].filter(Boolean).join(" ")}>

        {/* Pinned banner */}
        {localPinned && (
          <div className="comment-pinned-banner">
            <FaThumbtack /> Pinned by moderator
          </div>
        )}

        {/* Hidden badge (mod only) */}
        {localHidden && isMod && (
          <div className="comment-hidden-banner">
            <FaEyeSlash /> Hidden from users
          </div>
        )}

        {/* Comment header */}
        <div className="comment-header">
          <div className="comment-avatar">
            {avatar(comment.userName ?? comment.username)}
          </div>
          <div className="comment-meta">
            <span className="comment-author">{comment.userName ?? comment.username}</span>
            <span className="comment-time">{timeAgo(comment.createdAt)}</span>
          </div>

          {/* Actions */}
          <div className="comment-actions-row">
            {/* Reply — only when discussion isn't locked */}
            {!isLocked && (
              <button
                className="comment-action-btn"
                onClick={() => { setShowReply(!showReply); setTimeout(() => textareaRef.current?.focus(), 50); }}
                title="Reply"
              >
                <FaReply /> <span>Reply</span>
              </button>
            )}

            {/* Report — any non-owner */}
            {!isOwn && (
              <button
                className="comment-action-btn comment-action-btn--report"
                onClick={() => setReporting(true)}
                title="Report comment"
              >
                <FaFlag />
              </button>
            )}

            {/* Moderator-only actions */}
            {isMod && (
              <>
                <button
                  className={`comment-action-btn${localPinned ? " active-mod-btn" : ""}`}
                  onClick={handlePin}
                  title={localPinned ? "Unpin" : "Pin comment"}
                >
                  <FaThumbtack />
                </button>
                <button
                  className={`comment-action-btn${localHidden ? " active-mod-btn" : ""}`}
                  onClick={handleHide}
                  title={localHidden ? "Unhide" : "Hide comment"}
                >
                  {localHidden ? <FaEye /> : <FaEyeSlash />}
                </button>
              </>
            )}

            {/* Delete — own or mod */}
            {canDelete && (
              <button
                className="comment-action-btn comment-action-btn--delete"
                onClick={() => onDelete(comment.id)}
                title="Delete comment"
              >
                <FaTrash />
              </button>
            )}
          </div>
        </div>

        {/* Comment body */}
        <p className="comment-body">{comment.content}</p>

        {/* Reply box */}
        {showReply && !isLocked && (
          <div className="reply-compose">
            <textarea
              ref={textareaRef}
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder={`Replying to ${comment.userName ?? comment.username}…`}
              rows={3}
            />
            <div className="reply-compose__footer">
              <button className="btn-ghost" onClick={() => setShowReply(false)}>Cancel</button>
              <button
                className="btn-primary btn-sm"
                disabled={!replyText.trim() || replying}
                onClick={handleReplySubmit}
              >
                {replying ? "Posting…" : "Post Reply"}
              </button>
            </div>
          </div>
        )}

        {/* Replies */}
        {comment.replies?.length > 0 && (
          <div className="replies-list">
            {comment.replies.map((r) => (
              <div key={r.id} className="reply-card">
                <div className="reply-avatar">{avatar(r.userName ?? r.username)}</div>
                <div className="reply-body">
                  <div className="reply-meta">
                    <span className="comment-author">{r.userName ?? r.username}</span>
                    <span className="comment-time">{timeAgo(r.createdAt)}</span>
                  </div>
                  <p>{r.content}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Report modal */}
      {reporting && (
        <ReportModal
          target={comment.content?.slice(0, 40)}
          onClose={() => setReporting(false)}
          onSubmit={async (reason) => {
            await onReport("COMMENT", comment.id, reason);
            setReporting(false);
          }}
        />
      )}
    </>
  );
}

// ─── Discussion (main export) ─────────────────────────────────────────────────

export default function Discussion({ decisionId, isLocked = false, isPinned = false }) {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [comments,   setComments]   = useState([]);
  const [newComment, setNewComment] = useState("");
  const [posting,    setPosting]    = useState(false);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState(null);

  const isMod = user?.role === "ADMIN" || user?.role === "MODERATOR";
  const currentUserId = user?.id ?? localStorage.getItem("userId");

  // ── load comments ──────────────────────────────────────────────────────────
  const fetchComments = async () => {
    if (!decisionId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await getComments(decisionId);
      setComments(res.data ?? []);
    } catch {
      setError("Could not load comments.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchComments(); }, [decisionId]);

  // ── post comment ───────────────────────────────────────────────────────────
  const handlePost = async () => {
    if (!newComment.trim() || posting || isLocked) return;
    setPosting(true);
    try {
      await postComment(decisionId, newComment);
      setNewComment("");
      await fetchComments();
      addToast("Comment posted.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to post comment.", "error");
    } finally {
      setPosting(false);
    }
  };

  // ── delete ─────────────────────────────────────────────────────────────────
  const handleDelete = async (commentId) => {
    if (!window.confirm("Delete this comment?")) return;
    try {
      await deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
      addToast("Comment deleted.", "success");
    } catch {
      addToast("Failed to delete comment.", "error");
    }
  };

  // ── pin ────────────────────────────────────────────────────────────────────
  const handlePin = async (commentId, pin) => {
    try {
      pin ? await pinComment(commentId) : await unpinComment(commentId);
      addToast(pin ? "Comment pinned." : "Comment unpinned.", "success");
    } catch {
      addToast("Could not update pin status.", "error");
    }
  };

  // ── hide ───────────────────────────────────────────────────────────────────
  const handleHide = async (commentId, hide) => {
    try {
      await hideComment(commentId);
      addToast(hide ? "Comment hidden." : "Comment visible again.", "success");
    } catch {
      addToast("Could not update visibility.", "error");
    }
  };

  // ── report ─────────────────────────────────────────────────────────────────
  const handleReport = async (type, targetId, reason) => {
    await reportContent(type, targetId, reason);
    addToast("Report submitted. Thank you.", "success");
  };

  // ── sort: pinned first ─────────────────────────────────────────────────────
  const sorted = [...comments].sort((a, b) => {
    if (a.pinned && !b.pinned) return -1;
    if (!a.pinned && b.pinned) return 1;
    return 0;
  });

  return (
    <div className="discussion-panel">

      {/* ── Section header ── */}
      <div className="discussion-header">
        <div className="discussion-header__left">
          <FaComments />
          <h3>Discussion</h3>
          <span className="discussion-count">{comments.length}</span>
          {isPinned && (
            <span className="discussion-pinned-badge">
              <FaThumbtack /> Pinned
            </span>
          )}
        </div>
        <button
          className="vp-refresh-btn"
          onClick={fetchComments}
          disabled={loading}
          title="Refresh comments"
        >
          <FaSync className={loading ? "spin" : ""} />
        </button>
      </div>

      {/* ── Lock banner ── */}
      {isLocked && (
        <div className="discussion-locked-banner">
          <FaLock />
          <div>
            <strong>Discussion locked</strong>
            <p>A moderator has locked this discussion. New comments are not allowed.</p>
          </div>
        </div>
      )}

      {/* ── Compose ── */}
      {!isLocked ? (
        <div className="comment-compose glass-panel">
          <div className="compose-avatar">{avatar(user?.username)}</div>
          <div className="compose-body">
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Share your thoughts on this decision…"
              rows={3}
              onKeyDown={(e) => {
                if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) handlePost();
              }}
            />
            <div className="compose-footer">
              <span className="compose-hint">Ctrl + Enter to post</span>
              <button
                className="btn-primary btn-sm"
                disabled={!newComment.trim() || posting}
                onClick={handlePost}
              >
                {posting ? "Posting…" : "Post Comment"}
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="compose-locked-msg">
          <FaLock /> Comments are disabled while this discussion is locked.
        </div>
      )}

      {/* ── Comments list ── */}
      {loading ? (
        <div className="discussion-loading">
          <FaSync className="spin" /> Loading comments…
        </div>
      ) : error ? (
        <div className="discussion-error">
          <FaExclamationTriangle /> {error}
          <button className="btn-link" onClick={fetchComments}>Retry</button>
        </div>
      ) : sorted.length === 0 ? (
        <div className="discussion-empty">
          <FaComments />
          <p>No comments yet. Be the first to share your thoughts.</p>
        </div>
      ) : (
        <div className="comments-list">
          {sorted.map((c) => (
            <CommentCard
              key={c.id}
              comment={c}
              currentUserId={currentUserId}
              isMod={isMod}
              isLocked={isLocked}
              onDelete={handleDelete}
              onPin={handlePin}
              onHide={handleHide}
              onReport={handleReport}
            />
          ))}
        </div>
      )}
    </div>
  );
}
