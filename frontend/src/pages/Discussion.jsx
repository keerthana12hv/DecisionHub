import { useState, useEffect, useCallback, useRef } from "react";
import {
  FaComments, FaReply, FaTrash, FaFlag, FaLock,
  FaThumbtack, FaEdit, FaSync, FaExclamationTriangle,
  FaTimes, FaChevronDown, FaChevronUp,
} from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import {
  getComments,
  postComment,
  postReply,
  updateComment,
  deleteComment,
  getReplies,
} from "../services/commentService";
import { reportContent } from "../services/moderationService";
import "../styles/Discussion.css";

// ─── helpers ──────────────────────────────────────────────────────────────────

const avatar = (name = "?") => (name?.[0] ?? "?").toUpperCase();

const timeAgo = (iso) => {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1)  return "just now";
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
};

// ─── ReportModal ──────────────────────────────────────────────────────────────

function ReportModal({ onClose, onSubmit }) {
  const reasons = [
    "Spam or advertising",
    "Harassment or hate speech",
    "Misinformation",
    "Off-topic content",
    "Other",
  ];
  const [selected, setSelected] = useState("");
  const [custom, setCustom]     = useState("");
  const [busy, setBusy]         = useState(false);

  const canSubmit = selected && (selected !== "Other" || custom.trim());

  const handleSubmit = async () => {
    if (!canSubmit || busy) return;
    setBusy(true);
    await onSubmit(selected === "Other" ? custom.trim() : selected);
    setBusy(false);
  };

  return (
    <div className="disc-modal-overlay" onClick={onClose}>
      <div className="disc-modal glass-panel" onClick={(e) => e.stopPropagation()}>
        <div className="disc-modal-header">
          <FaFlag /> <h3>Report Comment</h3>
          <button className="disc-modal-close" onClick={onClose}><FaTimes /></button>
        </div>
        <p className="disc-modal-sub">Select a reason for reporting this comment.</p>
        <div className="disc-reason-list">
          {reasons.map((r) => (
            <label key={r} className={`disc-reason-item${selected === r ? " active" : ""}`}>
              <input type="radio" name="reason" checked={selected === r}
                onChange={() => setSelected(r)} />
              {r}
            </label>
          ))}
        </div>
        {selected === "Other" && (
          <textarea
            className="disc-custom-reason"
            placeholder="Describe the issue…"
            value={custom}
            onChange={(e) => setCustom(e.target.value)}
          />
        )}
        <div className="disc-modal-footer">
          <button className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn-danger" disabled={!canSubmit || busy} onClick={handleSubmit}>
            {busy ? "Submitting…" : "Submit Report"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── CommentCard ──────────────────────────────────────────────────────────────

function CommentCard({ comment, decisionId, currentUserId, isMod, isLocked, onDeleted, onPinToggle }) {
  const { addToast }                    = useToast();
  const [replies, setReplies]           = useState([]);
  const [repliesLoaded, setRepliesLoaded] = useState(false);
  const [showReplies, setShowReplies]   = useState(false);
  const [replyText, setReplyText]       = useState("");
  const [showReplyBox, setShowReplyBox] = useState(false);
  const [editing, setEditing]           = useState(false);
  const [editText, setEditText]         = useState(comment.content);
  const [reporting, setReporting]       = useState(false);
  const [busy, setBusy]                 = useState(false);
  const replyRef                        = useRef(null);

  const isOwn    = String(comment.userId) === String(currentUserId);
  const isDeleted = comment.deleted;

  // ── load replies ─────────────────────────────────────────────────────────
  const loadReplies = useCallback(async () => {
    try {
      const res = await getReplies(comment.id);
      setReplies(res.data ?? []);
      setRepliesLoaded(true);
    } catch {
      addToast("Could not load replies.", "error");
    }
  }, [comment.id, addToast]);

  const toggleReplies = async () => {
    if (!repliesLoaded) await loadReplies();
    setShowReplies((p) => !p);
  };

  // ── reply ─────────────────────────────────────────────────────────────────
  const handleReply = async () => {
    if (!replyText.trim() || busy) return;
    setBusy(true);
    try {
      const res = await postReply(decisionId, comment.id, replyText);
      setReplies((p) => [...p, res.data]);
      setRepliesLoaded(true);
      setShowReplies(true);
      setReplyText("");
      setShowReplyBox(false);
      addToast("Reply posted.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to post reply.", "error");
    } finally {
      setBusy(false);
    }
  };

  // ── edit ──────────────────────────────────────────────────────────────────
  const handleEdit = async () => {
    if (!editText.trim() || busy) return;
    setBusy(true);
    try {
      await updateComment(comment.id, editText);
      comment.content = editText; // mutate local ref so UI updates without refetch
      setEditing(false);
      addToast("Comment updated.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to update comment.", "error");
    } finally {
      setBusy(false);
    }
  };

  // ── delete ────────────────────────────────────────────────────────────────
  const handleDelete = async () => {
    if (!window.confirm("Delete this comment?")) return;
    setBusy(true);
    try {
      // Owners use regular DELETE; mods use moderation DELETE (same result)
      await deleteComment(comment.id);
      onDeleted(comment.id);
      addToast("Comment deleted.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to delete comment.", "error");
    } finally {
      setBusy(false);
    }
  };

  // ── report ────────────────────────────────────────────────────────────────
  const handleReport = async (reason) => {
    await reportContent("COMMENT", comment.id, reason);
    setReporting(false);
    addToast("Report submitted. Thank you.", "success");
  };

  // ── deleted placeholder ───────────────────────────────────────────────────
  if (isDeleted) {
    return (
      <div className="comment-card comment-card--deleted">
        <span className="deleted-label">[This comment was deleted]</span>
        {comment.replyCount > 0 && (
          <button className="replies-toggle" onClick={toggleReplies}>
            {showReplies ? <FaChevronUp /> : <FaChevronDown />}
            {comment.replyCount} {comment.replyCount === 1 ? "reply" : "replies"}
          </button>
        )}
        {showReplies && replies.map((r) => (
          <CommentCard key={r.id} comment={r} decisionId={decisionId}
            currentUserId={currentUserId} isMod={isMod} isLocked={isLocked}
            onDeleted={onDeleted} onPinToggle={onPinToggle} />
        ))}
      </div>
    );
  }

  return (
    <>
      <div className={[
        "comment-card",
        comment.pinned ? "comment-card--pinned" : "",
      ].filter(Boolean).join(" ")}>

        {/* Pinned banner */}
        {comment.pinned && (
          <div className="comment-pinned-banner">
            <FaThumbtack /> Pinned by moderator
          </div>
        )}

        {/* Header row */}
        <div className="comment-header">
          <div className="comment-avatar">{avatar(comment.username)}</div>
          <div className="comment-meta">
            <span className="comment-author">{comment.username}</span>
            <span className="comment-time">{timeAgo(comment.createdAt)}</span>
          </div>

          {/* Action buttons */}
          <div className="comment-actions">
            {/* Reply */}
            {!isLocked && (
              <button className="cmt-btn" title="Reply"
                onClick={() => { setShowReplyBox((p) => !p); setTimeout(() => replyRef.current?.focus(), 50); }}>
                <FaReply />
              </button>
            )}

            {/* Edit — own comment only, not locked */}
            {isOwn && !isLocked && (
              <button className="cmt-btn" title="Edit" onClick={() => setEditing((p) => !p)}>
                <FaEdit />
              </button>
            )}

            {/* Report — not own */}
            {!isOwn && (
              <button className="cmt-btn cmt-btn--warn" title="Report" onClick={() => setReporting(true)}>
                <FaFlag />
              </button>
            )}

            {/* Pin / Unpin — mod only */}
            {isMod && (
              <button
                className={`cmt-btn${comment.pinned ? " cmt-btn--active" : ""}`}
                title={comment.pinned ? "Unpin" : "Pin"}
                onClick={() => onPinToggle(comment)}
              >
                <FaThumbtack />
              </button>
            )}

            {/* Delete — own or mod */}
            {(isOwn || isMod) && (
              <button className="cmt-btn cmt-btn--danger" title="Delete" disabled={busy} onClick={handleDelete}>
                <FaTrash />
              </button>
            )}
          </div>
        </div>

        {/* Body — edit mode or plain text */}
        {editing ? (
          <div className="comment-edit-box">
            <textarea
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              rows={3}
            />
            <div className="edit-actions">
              <button className="btn-ghost btn-sm" onClick={() => setEditing(false)}>Cancel</button>
              <button className="btn-primary btn-sm" disabled={!editText.trim() || busy} onClick={handleEdit}>
                {busy ? "Saving…" : "Save"}
              </button>
            </div>
          </div>
        ) : (
          <p className="comment-body">{comment.content}</p>
        )}

        {/* Reply compose */}
        {showReplyBox && !isLocked && (
          <div className="reply-compose">
            <textarea
              ref={replyRef}
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder={`Replying to ${comment.username}…`}
              rows={2}
            />
            <div className="reply-compose-footer">
              <button className="btn-ghost btn-sm" onClick={() => setShowReplyBox(false)}>Cancel</button>
              <button className="btn-primary btn-sm" disabled={!replyText.trim() || busy} onClick={handleReply}>
                {busy ? "Posting…" : "Post Reply"}
              </button>
            </div>
          </div>
        )}

        {/* Replies toggle */}
        {comment.replyCount > 0 && (
          <button className="replies-toggle" onClick={toggleReplies}>
            {showReplies ? <FaChevronUp /> : <FaChevronDown />}
            {comment.replyCount} {comment.replyCount === 1 ? "reply" : "replies"}
          </button>
        )}

        {/* Replies list */}
        {showReplies && (
          <div className="replies-list">
            {replies.map((r) => (
              <CommentCard key={r.id} comment={r} decisionId={decisionId}
                currentUserId={currentUserId} isMod={isMod} isLocked={isLocked}
                onDeleted={onDeleted} onPinToggle={onPinToggle} />
            ))}
          </div>
        )}
      </div>

      {reporting && (
        <ReportModal onClose={() => setReporting(false)} onSubmit={handleReport} />
      )}
    </>
  );
}

// ─── Discussion (main export) ─────────────────────────────────────────────────

export default function Discussion({ decisionId, isLocked = false, isPinned = false }) {
  const { user }     = useAuth();
  const { addToast } = useToast();

  const [comments,    setComments]    = useState([]);
  const [newComment,  setNewComment]  = useState("");
  const [posting,     setPosting]     = useState(false);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState(null);

  const isMod         = user?.role === "ADMIN" || user?.role === "MODERATOR";
  const currentUserId = user?.id ?? localStorage.getItem("userId");

  // ── fetch comments ────────────────────────────────────────────────────────
  const fetchComments = useCallback(async () => {
    if (!decisionId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await getComments(decisionId);
      // Pinned comments float to top
      const sorted = [...(res.data ?? [])].sort((a, b) => {
        if (a.pinned && !b.pinned) return -1;
        if (!a.pinned && b.pinned) return 1;
        return 0;
      });
      setComments(sorted);
    } catch (err) {
      setError(err?.response?.data?.message ?? "Could not load comments.");
    } finally {
      setLoading(false);
    }
  }, [decisionId]);

  useEffect(() => { fetchComments(); }, [fetchComments]);

  // ── post comment ──────────────────────────────────────────────────────────
  const handlePost = async () => {
    if (!newComment.trim() || posting || isLocked) return;
    setPosting(true);
    try {
      const res = await postComment(decisionId, newComment);
      setComments((prev) => [...prev, res.data]);
      setNewComment("");
      addToast("Comment posted.", "success");
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Failed to post comment.", "error");
    } finally {
      setPosting(false);
    }
  };

  // ── delete callback (from CommentCard) ────────────────────────────────────
  const handleDeleted = (commentId) => {
    setComments((prev) => prev.filter((c) => c.id !== commentId));
  };

  // ── pin toggle (mod only) ─────────────────────────────────────────────────
  const handlePinToggle = async (comment) => {
    try {
      const { pinComment, unpinComment } = await import("../services/moderationService");
      if (comment.pinned) {
        await unpinComment(comment.id);
        addToast("Comment unpinned.", "success");
      } else {
        await pinComment(comment.id);
        addToast("Comment pinned.", "success");
      }
      fetchComments(); // re-fetch so only-one-pinned rule is respected
    } catch (err) {
      addToast(err?.response?.data?.message ?? "Could not update pin.", "error");
    }
  };

  return (
    <div className="discussion-panel">

      {/* ── Header ── */}
      <div className="discussion-header">
        <div className="disc-header-left">
          <FaComments />
          <h3>Discussion</h3>
          <span className="disc-count">{comments.length}</span>
          {isPinned && (
            <span className="disc-pinned-badge"><FaThumbtack /> Pinned</span>
          )}
        </div>
        <button className="vp-refresh-btn" onClick={fetchComments} disabled={loading} title="Refresh">
          <FaSync className={loading ? "spin" : ""} />
        </button>
      </div>

      {/* ── Lock banner ── */}
      {isLocked && (
        <div className="disc-lock-banner">
          <FaLock />
          <div>
            <strong>Discussion locked</strong>
            <p>A moderator has locked this discussion. New comments are disabled.</p>
          </div>
        </div>
      )}

      {/* ── Compose ── */}
      {!isLocked ? (
        <div className="disc-compose glass-panel">
          <div className="disc-compose-avatar">{avatar(user?.username)}</div>
          <div className="disc-compose-body">
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Share your thoughts on this decision…"
              rows={3}
              onKeyDown={(e) => { if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) handlePost(); }}
            />
            <div className="disc-compose-footer">
              <span className="disc-compose-hint">Ctrl + Enter to post</span>
              <button className="btn-primary btn-sm"
                disabled={!newComment.trim() || posting} onClick={handlePost}>
                {posting ? "Posting…" : "Post Comment"}
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="disc-locked-msg">
          <FaLock /> Comments are disabled while this discussion is locked.
        </div>
      )}

      {/* ── Body ── */}
      {loading ? (
        <div className="disc-state">
          <FaSync className="spin" /> Loading comments…
        </div>
      ) : error ? (
        <div className="disc-state disc-state--error">
          <FaExclamationTriangle /> {error}
          <button className="btn-link" onClick={fetchComments}>Retry</button>
        </div>
      ) : comments.length === 0 ? (
        <div className="disc-state disc-state--empty">
          <FaComments />
          <p>No comments yet. Be the first to share your thoughts.</p>
        </div>
      ) : (
        <div className="comments-list">
          {comments.map((c) => (
            <CommentCard
              key={c.id}
              comment={c}
              decisionId={decisionId}
              currentUserId={currentUserId}
              isMod={isMod}
              isLocked={isLocked}
              onDeleted={handleDeleted}
              onPinToggle={handlePinToggle}
            />
          ))}
        </div>
      )}
    </div>
  );
}
