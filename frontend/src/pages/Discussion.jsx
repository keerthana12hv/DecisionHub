import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { FaComments, FaReply, FaTrash, FaEdit } from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import {
  getComments,
  postComment,
  postReply,
  getReplies,
  editComment,
  deleteComment,
} from "../services/commentService";
import { reportComment, pinComment, unpinComment, modDeleteComment } from "../services/moderationService";
import { useToast } from "../components/Toast";

const MAX_DEPTH = 5;

const Discussion = () => {
  const { id } = useParams();
  const decisionId = Number(id);
  const { user } = useAuth();
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [decisionStatus, setDecisionStatus] = useState(null);
  const [decisionLocked, setDecisionLocked] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const isDecisionActive = decisionStatus === "ACTIVE";
  const discussionOpen = isDecisionActive && !decisionLocked;

  useEffect(() => {
    api
      .get(`/api/decisions/${decisionId}`)
      .then((res) => {
        setDecisionStatus(res.data.status);
        setDecisionLocked(!!res.data.locked);
      })
      .catch(() => {
        setDecisionStatus(null);
        setDecisionLocked(false);
      });

    getComments(decisionId)
      .then((res) => {
        setComments(res.data);
        setLoading(false);
      })
      .catch(() => {
        setComments([]);
        setLoading(false);
      });
  }, [decisionId]);

  const addComment = () => {
    if (!newComment.trim()) return;
    setErrorMsg("");
    postComment(decisionId, newComment)
      .then((res) => {
        setComments([...comments, res.data]);
        setNewComment("");
      })
      .catch((error) => {
        setErrorMsg(
          error.response?.data?.error || "Failed to post comment. Please try again."
        );
      });
  };

  const updateCommentInTree = (list, commentId, changes) =>
    list.map((c) => (c.id === commentId ? { ...c, ...changes } : c));

  const handleCommentUpdated = (commentId, changes) => {
    setComments((prev) => updateCommentInTree(prev, commentId, changes));
  };

  const handleCommentDeleted = (commentId) => {
    setComments((prev) =>
      updateCommentInTree(prev, commentId, { deleted: true, content: "[deleted]" })
    );
  };

  if (loading)
    return (
      <div style={{ textAlign: "center", padding: "50px", color: "#A78BFA" }}>
        Loading discussions...
      </div>
    );

  return (
    <div
      style={{
        padding: "24px",
        maxWidth: "800px",
        margin: "0 auto",
        fontFamily: "Inter, sans-serif",
      }}
    >
      <div style={{ marginBottom: "24px" }}>
        <h1
          style={{
            fontSize: "24px",
            fontWeight: "bold",
            color: "#A78BFA",
            display: "flex",
            alignItems: "center",
            gap: "10px",
          }}
        >
          <FaComments /> Discussion
        </h1>
        <p style={{ color: "#6B7280", marginTop: "4px" }}>
          Share your thoughts and opinions
        </p>
      </div>

      {errorMsg && (
        <div
          style={{
            background: "rgba(239, 68, 68, 0.1)",
            border: "1px solid rgba(239, 68, 68, 0.4)",
            color: "#F87171",
            borderRadius: "8px",
            padding: "12px 16px",
            marginBottom: "16px",
            fontSize: "13px",
          }}
        >
          {errorMsg}
        </div>
      )}

      {isDecisionActive ? (
        <div
          style={{
            background: "rgba(255,255,255,0.05)",
            borderRadius: "12px",
            padding: "20px",
            marginBottom: "24px",
            border: "1px solid rgba(167, 139, 250, 0.2)",
          }}
        >
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Share your thoughts..."
            style={textareaStyle}
          />
          <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "12px" }}>
            <button onClick={addComment} style={primaryBtnStyle}>
              Post Comment
            </button>
          </div>
        </div>
      ) : (
        <div
          style={{
            background: "rgba(255,255,255,0.05)",
            borderRadius: "12px",
            padding: "16px 20px",
            marginBottom: "24px",
            border: "1px solid rgba(167, 139, 250, 0.2)",
            color: "#6B7280",
            fontSize: "14px",
          }}
        >
          This decision is closed. Commenting is no longer available.
        </div>
      )}

      <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
        {comments.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px", color: "#6B7280" }}>
            No comments yet. Be the first to share!
          </div>
        ) : (
          comments.map((comment) => (
            <CommentNode
              key={comment.id}
              comment={comment}
              decisionId={decisionId}
              isDecisionActive={isDecisionActive}
              decisionLocked={decisionLocked}
              currentUser={user}
              onCommentUpdated={handleCommentUpdated}
              onCommentDeleted={handleCommentDeleted}
            />
          ))
        )}
      </div>
    </div>
  );
};

function CommentNode({
  comment,
  decisionId,
  isDecisionActive,
  decisionLocked,
  currentUser,
  onCommentUpdated,
  onCommentDeleted,
}) {
  const { addToast } = useToast();
  const [replies, setReplies] = useState([]);
  const [repliesLoaded, setRepliesLoaded] = useState(false);
  const [repliesVisible, setRepliesVisible] = useState(false);
  const [loadingReplies, setLoadingReplies] = useState(false);

  const [isReplying, setIsReplying] = useState(false);
  const [replyText, setReplyText] = useState("");

  const [isReporting, setIsReporting] = useState(false);
  const [reportReason, setReportReason] = useState("");

  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState(comment.content);

  const isAuthor = currentUser?.id === comment.userId;
  const isModOrAdmin =
    currentUser?.role === "MODERATOR" || currentUser?.role === "ADMIN";
  const canEdit = isAuthor && comment.replyCount === 0 && !comment.deleted;
  const canDelete = (isAuthor || isModOrAdmin) && !comment.deleted;
  const canReply =
    isDecisionActive && !decisionLocked && comment.depth < MAX_DEPTH - 1 && !comment.deleted;

  const loadReplies = () => {
    if (repliesLoaded) {
      setRepliesVisible(!repliesVisible);
      return;
    }
    setLoadingReplies(true);
    getReplies(comment.id)
      .then((res) => {
        setReplies(res.data);
        setRepliesLoaded(true);
        setRepliesVisible(true);
      })
      .finally(() => setLoadingReplies(false));
  };

  const handleReplySubmit = () => {
    if (!replyText.trim()) return;
    postReply(decisionId, comment.id, replyText).then((res) => {
      setReplies((prev) => [...prev, res.data]);
      setRepliesLoaded(true);
      setRepliesVisible(true);
      setReplyText("");
      setIsReplying(false);
      onCommentUpdated(comment.id, { replyCount: comment.replyCount + 1 });
    });
  };

  const handleEditSubmit = () => {
    if (!editText.trim()) return;
    editComment(comment.id, editText).then((res) => {
      onCommentUpdated(comment.id, { content: res.data.content });
      setIsEditing(false);
    });
  };

  const handleDelete = () => {
    deleteComment(comment.id).then(() => {
      onCommentDeleted(comment.id);
    });
  };

  const handleChildUpdated = (childId, changes) => {
    setReplies((prev) =>
      prev.map((r) => (r.id === childId ? { ...r, ...changes } : r))
    );
  };

  const handleChildDeleted = (childId) => {
    setReplies((prev) =>
      prev.map((r) =>
        r.id === childId ? { ...r, deleted: true, content: "[deleted]" } : r
      )
    );
  };

  return (
    <div
      style={{
        marginLeft: comment.depth > 0 ? "20px" : "0",
        paddingLeft: comment.depth > 0 ? "16px" : "0",
        borderLeft: comment.depth > 0 ? "3px solid rgba(167, 139, 250, 0.3)" : "none",
        marginTop: comment.depth > 0 ? "12px" : "0",
      }}
    >
      <div
        style={{
          background: "rgba(255,255,255,0.05)",
          borderRadius: "12px",
          padding: "16px",
          border: "1px solid rgba(167, 139, 250, 0.15)",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "10px" }}>
            <div style={avatarStyle}>
              {comment.deleted ? "-" : comment.username?.[0]?.toUpperCase()}
            </div>
            <p style={{ fontWeight: "600", color: "#A78BFA", fontSize: "14px" }}>
              {comment.deleted ? "" : comment.username}
            </p>
          </div>
          {canDelete && (
            <button onClick={handleDelete} style={iconBtnStyle}>
              <FaTrash size={14} />
            </button>
          )}
          {comment.pinned && (
            <span style={{ color: "#FBBF24", fontWeight: 700, marginLeft: 8 }}>Pinned</span>
          )}
        </div>

        {isEditing ? (
          <div>
            <textarea
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              style={textareaStyle}
            />
            <div style={{ display: "flex", gap: "8px", marginTop: "8px", justifyContent: "flex-end" }}>
              <button onClick={() => setIsEditing(false)} style={secondaryBtnStyle}>
                Cancel
              </button>
              <button onClick={handleEditSubmit} style={primaryBtnStyle}>
                Save
              </button>
            </div>
          </div>
        ) : (
          <p style={{ color: "#E5E7EB", fontSize: "14px", marginBottom: "10px" }}>
            {comment.content}
          </p>
        )}

        {!comment.deleted && !isEditing && (
          <div style={{ display: "flex", gap: "16px" }}>
            {canReply && (
              <button
                onClick={() => setIsReplying(!isReplying)}
                style={linkBtnStyle}
              >
                <FaReply /> Reply
              </button>
            )}
            {canEdit && (
              <button onClick={() => setIsEditing(true)} style={linkBtnStyle}>
                <FaEdit /> Edit
              </button>
            )}
            {!isAuthor && (
              <button
                onClick={() => setIsReporting(true)}
                style={linkBtnStyle}
              >
                Report
              </button>
            )}
            {isModOrAdmin && (
              <>
                {!comment.pinned ? (
                  <button
                    onClick={async () => {
                      try {
                        const res = await pinComment(comment.id);
                        onCommentUpdated(comment.id, { pinned: res.pinned });
                        addToast("Comment pinned.", "success");
                      } catch (err) {
                        addToast("Failed to pin comment.", "error");
                      }
                    }}
                    style={linkBtnStyle}
                  >
                    Pin
                  </button>
                ) : (
                  <button
                    onClick={async () => {
                      try {
                        const res = await unpinComment(comment.id);
                        onCommentUpdated(comment.id, { pinned: res.pinned });
                        addToast("Comment unpinned.", "success");
                      } catch (err) {
                        addToast("Failed to unpin comment.", "error");
                      }
                    }}
                    style={linkBtnStyle}
                  >
                    Unpin
                  </button>
                )}

                <button
                  onClick={async () => {
                    if (!confirm("Remove this comment?")) return;
                    try {
                      await modDeleteComment(comment.id);
                      onCommentDeleted(comment.id);
                      addToast("Comment removed.", "success");
                    } catch (err) {
                      addToast("Failed to remove comment.", "error");
                    }
                  }}
                  style={linkBtnStyle}
                >
                  Remove
                </button>
              </>
            )}
          </div>
        )}

        {isReporting && (
          <div style={{ marginTop: "12px" }}>
            <textarea
              value={reportReason}
              onChange={(e) => setReportReason(e.target.value)}
              placeholder="Why is this comment inappropriate?"
              style={textareaStyle}
            />
            <div style={{ display: "flex", gap: "8px", marginTop: "8px", justifyContent: "flex-end" }}>
              <button onClick={() => setIsReporting(false)} style={secondaryBtnStyle}>
                Cancel
              </button>
              <button
                onClick={async () => {
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
                }}
                style={primaryBtnStyle}
              >
                Submit Report
              </button>
            </div>
          </div>
        )}

        {isReplying && (
          <div style={{ marginTop: "12px" }}>
            <textarea
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Write a reply..."
              style={textareaStyle}
            />
            <div style={{ display: "flex", gap: "8px", marginTop: "8px", justifyContent: "flex-end" }}>
              <button onClick={() => setIsReplying(false)} style={secondaryBtnStyle}>
                Cancel
              </button>
              <button onClick={handleReplySubmit} style={primaryBtnStyle}>
                Reply
              </button>
            </div>
          </div>
        )}

        {comment.replyCount > 0 && (
          <button onClick={loadReplies} disabled={loadingReplies} style={linkBtnStyle}>
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
          <CommentNode
            key={reply.id}
            comment={reply}
            decisionId={decisionId}
            isDecisionActive={isDecisionActive}
            decisionLocked={decisionLocked}
            currentUser={currentUser}
            onCommentUpdated={handleChildUpdated}
            onCommentDeleted={handleChildDeleted}
          />
        ))}
    </div>
  );
}

const textareaStyle = {
  width: "100%",
  minHeight: "80px",
  background: "rgba(255,255,255,0.05)",
  border: "1px solid rgba(167, 139, 250, 0.3)",
  borderRadius: "8px",
  padding: "10px",
  fontSize: "13px",
  resize: "none",
  outline: "none",
  color: "white",
  fontFamily: "inherit",
  boxSizing: "border-box",
};

const primaryBtnStyle = {
  background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
  color: "white",
  border: "none",
  borderRadius: "8px",
  padding: "8px 16px",
  cursor: "pointer",
  fontWeight: "600",
  fontSize: "13px",
};

const secondaryBtnStyle = {
  background: "rgba(255,255,255,0.05)",
  border: "1px solid rgba(167,139,250,0.2)",
  borderRadius: "8px",
  padding: "8px 16px",
  cursor: "pointer",
  fontSize: "13px",
  color: "white",
};

const linkBtnStyle = {
  background: "none",
  border: "none",
  color: "#A78BFA",
  cursor: "pointer",
  fontSize: "13px",
  fontWeight: "600",
  display: "flex",
  alignItems: "center",
  gap: "6px",
};

const iconBtnStyle = {
  background: "none",
  border: "none",
  cursor: "pointer",
  color: "#6B7280",
};

const avatarStyle = {
  width: "36px",
  height: "36px",
  borderRadius: "50%",
  background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
  color: "white",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontWeight: "bold",
  fontSize: "14px",
};

export default Discussion;