import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { reportComment } from "../services/moderationService";

const MAX_DEPTH = 5;

function CommentThread({ comment, decisionId, decisionStatus, onCommentUpdated, onCommentDeleted }) {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [replies, setReplies] = useState([]);
  const [repliesLoaded, setRepliesLoaded] = useState(false);
  const [repliesVisible, setRepliesVisible] = useState(false);
  const [loadingReplies, setLoadingReplies] = useState(false);

  const [isReplying, setIsReplying] = useState(false);
  const [replyText, setReplyText] = useState("");

  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState(comment.content);
  const [isReporting, setIsReporting] = useState(false);
  const [reportReason, setReportReason] = useState("");

  const isDecisionActive = decisionStatus === "ACTIVE";
  const isAuthor = user?.id === comment.userId;
  const isModOrAdmin = user?.role === "MODERATOR" || user?.role === "ADMIN";
  const canEdit = isAuthor && comment.replyCount === 0 && !comment.deleted;
  const canDelete = (isAuthor || isModOrAdmin) && !comment.deleted;
  const canReply = isDecisionActive && comment.depth < MAX_DEPTH - 1 && !comment.deleted;

  const loadReplies = async () => {
    if (repliesLoaded) {
      setRepliesVisible(!repliesVisible);
      return;
    }
    setLoadingReplies(true);
    try {
      const res = await api.get(`/comments/${comment.id}/replies`);
      setReplies(res.data);
      setRepliesLoaded(true);
      setRepliesVisible(true);
    } catch (error) {
      addToast("Failed to load replies.", "error");
    } finally {
      setLoadingReplies(false);
    }
  };

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    if (!replyText.trim()) return;
    try {
      const res = await api.post(
        `/decisions/${decisionId}/comments/${comment.id}/replies`,
        { content: replyText }
      );
      setReplies((prev) => [...prev, res.data]);
      setRepliesLoaded(true);
      setRepliesVisible(true);
      setReplyText("");
      setIsReplying(false);
      onCommentUpdated(comment.id, { replyCount: comment.replyCount + 1 });
      addToast("Reply posted.", "success");
    } catch (error) {
      addToast(
        error.response?.data?.error || "Failed to post reply.",
        "error"
      );
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editText.trim()) return;
    try {
      const res = await api.put(`/comments/${comment.id}`, {
        content: editText,
      });
      onCommentUpdated(comment.id, { content: res.data.content });
      setIsEditing(false);
      addToast("Comment updated.", "success");
    } catch (error) {
      addToast(
        error.response?.data?.error || "Failed to update comment.",
        "error"
      );
    }
  };

  const handleReportSubmit = async (e) => {
    e.preventDefault();
    if (!reportReason.trim()) return;
    try {
      await reportComment(comment.id, reportReason);
      setReportReason("");
      setIsReporting(false);
      addToast("Comment reported. A moderator will review it.", "success");
    } catch (error) {
      addToast(
        error.response?.data?.error || "Failed to report comment.",
        "error"
      );
    }
  };

  const handleDelete = async () => {
    try {
      await api.delete(`/comments/${comment.id}`);
      onCommentDeleted(comment.id);
      addToast("Comment deleted.", "success");
    } catch (error) {
      addToast(
        error.response?.data?.error || "Failed to delete comment.",
        "error"
      );
    }
  };

  const handleChildUpdated = (childId, changes) => {
    setReplies((prev) =>
      prev.map((r) => (r.id === childId ? { ...r, ...changes } : r))
    );
  };

 
 const handleChildDeleted = (childId) => {
  setReplies((prev) => prev.filter((r) => r.id !== childId));
};

  return (
    <div className="comment-thread" style={{ marginLeft: comment.depth * 20 }}>
      <div className="comment-body">
        <span className="comment-author">
          {comment.deleted ? "" : comment.username}
        </span>

        {isEditing ? (
          <form onSubmit={handleEditSubmit} className="comment-edit-form">
            <textarea
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              required
            />
            <button type="submit">Save</button>
            <button type="button" onClick={() => setIsEditing(false)}>
              Cancel
            </button>
          </form>
        ) : (
          <p className="comment-content">{comment.content}</p>
        )}

        {!comment.deleted && !isEditing && (
          <div className="comment-actions">
            {canReply && (
              <button onClick={() => setIsReplying(!isReplying)}>
                Reply
              </button>
            )}
            {canEdit && (
              <button onClick={() => setIsEditing(true)}>Edit</button>
            )}
            {canDelete && <button onClick={handleDelete}>Delete</button>}
            {!isAuthor && (
              <button onClick={() => setIsReporting(true)}>
                Report
              </button>
            )}
          </div>
        )}

        {isReporting && (
          <form onSubmit={handleReportSubmit} className="comment-report-form">
            <textarea
              value={reportReason}
              onChange={(e) => setReportReason(e.target.value)}
              placeholder="Why is this comment inappropriate?"
              required
            />
            <button type="submit">Submit Report</button>
            <button type="button" onClick={() => setIsReporting(false)}>
              Cancel
            </button>
          </form>
        )}

        {isReplying && (
          <form onSubmit={handleReplySubmit} className="comment-reply-form">
            <textarea
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Write a reply..."
              required
            />
            <button type="submit">Post Reply</button>
            <button type="button" onClick={() => setIsReplying(false)}>
              Cancel
            </button>
          </form>
        )}

        {comment.replyCount > 0 && (
          <button
            className="show-replies-btn"
            onClick={loadReplies}
            disabled={loadingReplies}
          >
            {loadingReplies
              ? "Loading..."
              : repliesVisible
              ? "Hide replies"
              : `Show ${comment.replyCount} ${
                  comment.replyCount === 1 ? "reply" : "replies"
                }`}
          </button>
        )}
      </div>

      {repliesVisible &&
        replies.map((reply) => (
          <CommentThread
            key={reply.id}
            comment={reply}
            decisionId={decisionId}
            decisionStatus={decisionStatus}
            onCommentUpdated={handleChildUpdated}
            onCommentDeleted={handleChildDeleted}
          />
        ))}
    </div>
  );
}

export default CommentThread;