import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import api from "../services/api";
import { FaChevronLeft, FaComments, FaReply, FaTrash, FaThumbtack } from "react-icons/fa";

export default function AdminDecisionDiscussion() {
  const { id: decisionId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [decision, setDecision] = useState(null);
  const [comments, setComments] = useState([]);
  const [pinnedComment, setPinnedComment] = useState(null);
  const [reportedComments, setReportedComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  // Form state for new top-level comment
  const [newCommentText, setNewCommentText] = useState("");

  useEffect(() => {
    fetchDecisionAndComments();
  }, [decisionId]);

  const fetchDecisionAndComments = async () => {
    try {
      setLoading(true);
      const [decisionRes, commentsRes, pinnedRes] = await Promise.all([
        api.get(`/api/decisions/${decisionId}`),
        api.get(`/api/decisions/${decisionId}/comments`),
        api.get(`/api/moderation/decisions/${decisionId}/comments/pinned`).catch(() => null)
      ]);

      setDecision(decisionRes.data);
      setComments(commentsRes.data || []);
      setPinnedComment(pinnedRes && pinnedRes.status === 200 ? pinnedRes.data : null);

      // Load reported comments and filter by current decision
      await fetchReports(decisionRes.data);
    } catch (err) {
      console.error("Failed to load decision discussion:", err);
      addToast("Failed to load discussion details", "error");
    } finally {
      setLoading(false);
    }
  };

  const fetchReports = async (decisionData) => {
    try {
      const reportsRes = await api.get("/api/moderation/reports");
      const reports = reportsRes.data || [];
      
      const filtered = [];
      await Promise.all(
        reports.map(async (r) => {
          try {
            const commentRes = await api.get(`/api/comments/${r.commentId}`);
            if (String(commentRes.data?.decisionId) === String(decisionId)) {
              filtered.push({
                ...r,
                commentContent: commentRes.data?.content || "[deleted]",
                commentDeleted: commentRes.data?.deleted || false,
                decisionTitle: decisionData?.title || "—"
              });
            }
          } catch (e) {
            console.error("Error fetching comment details for report:", e);
          }
        })
      );
      setReportedComments(filtered);
    } catch (err) {
      console.error("Failed to fetch reports:", err);
    }
  };

  const handlePostTopComment = async (e) => {
    e.preventDefault();
    if (!newCommentText.trim()) return;

    try {
      setActionLoading(true);
      const res = await api.post(`/api/decisions/${decisionId}/comments`, {
        content: newCommentText
      });
      addToast("Comment posted successfully", "success");
      setNewCommentText("");
      // Add to list
      setComments([...comments, res.data]);
    } catch (err) {
      console.error("Failed to post comment:", err);
      addToast("Failed to post comment", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleCommentUpdated = (commentId, updatedData) => {
    setComments((prev) =>
      prev.map((c) => (c.id === commentId ? { ...c, ...updatedData } : c))
    );
    // Sync pinned comment
    if (pinnedComment && pinnedComment.id === commentId) {
      setPinnedComment({ ...pinnedComment, ...updatedData });
    }
  };

  const handleCommentDeleted = (commentId) => {
    const changes = { deleted: true, content: "[deleted]" };
    setComments((prev) =>
      prev.map((c) => (c.id === commentId ? { ...c, ...changes } : c))
    );
    if (pinnedComment && pinnedComment.id === commentId) {
      setPinnedComment(null);
    }
    // Refresh reports list to reflect soft-delete
    if (decision) {
      fetchReports(decision);
    }
  };

  const handleUnpinPinnedComment = async () => {
    if (!pinnedComment) return;
    try {
      setActionLoading(true);
      await api.put(`/api/moderation/comments/${pinnedComment.id}/unpin`, {});
      addToast("Comment unpinned", "success");
      handleCommentUpdated(pinnedComment.id, { pinned: false });
      setPinnedComment(null);
    } catch (err) {
      console.error("Failed to unpin comment:", err);
      addToast("Failed to unpin comment", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDismissReport = async (reportId) => {
    try {
      setActionLoading(true);
      await api.delete(`/api/moderation/reports/${reportId}`);
      addToast("Report dismissed", "success");
      if (decision) {
        fetchReports(decision);
      }
    } catch (err) {
      console.error("Failed to dismiss report:", err);
      addToast("Failed to dismiss report", "error");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Loading decision discussion...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!decision) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content animate-fade-in">
            <p>Decision not found.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="discussion-page" style={{ maxWidth: "800px", margin: "0 auto" }}>
            
            {/* Header back navigation */}
            <div style={{ marginBottom: "2.5rem" }}>
              <button
                onClick={() => navigate(`/admin/communities/${decision.communityId}/decisions`)}
                style={{
                  background: "none",
                  border: "none",
                  color: "var(--accent-purple, #A78BFA)",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "6px",
                  marginBottom: "1rem",
                  fontSize: "0.95rem",
                  fontWeight: "600"
                }}
              >
                <FaChevronLeft /> Back to Decisions
              </button>
              <h1 style={{ display: "flex", alignItems: "center", gap: "10px", color: "var(--accent-purple, #A78BFA)" }}>
                <FaComments /> Discussion Moderation
              </h1>
              <p style={{ color: "var(--text-secondary)", marginTop: "4px" }}>
                Moderating discussion for decision: <strong>{decision.title}</strong>
              </p>
            </div>

            {/* Pinned Comment Banner */}
            {pinnedComment && (
              <div
                className="pinned-comment-section glass-panel"
                style={{
                  padding: "1.25rem",
                  border: "1px solid #FBBF24",
                  borderRadius: "12px",
                  background: "rgba(251, 191, 36, 0.05)",
                  marginBottom: "2rem"
                }}
              >
                <h3 style={{ color: "#FBBF24", fontSize: "1rem", fontWeight: "700", display: "flex", alignItems: "center", gap: "6px", margin: "0 0 0.5rem" }}>
                  📌 Pinned Comment
                </h3>
                <p style={{ color: "white", fontSize: "0.95rem", margin: "0 0 0.75rem" }}>
                  {pinnedComment.content}
                </p>
                <button
                  onClick={handleUnpinPinnedComment}
                  disabled={actionLoading}
                  className="btn-secondary"
                  style={{ fontSize: "0.8rem", padding: "4px 8px" }}
                >
                  Unpin
                </button>
              </div>
            )}

            {/* Post comment section */}
            <div
              className="glass-panel"
              style={{
                background: "rgba(255,255,255,0.03)",
                borderRadius: "12px",
                padding: "20px",
                marginBottom: "2rem",
                border: "1px solid var(--border-glass)"
              }}
            >
              <form onSubmit={handlePostTopComment}>
                <textarea
                  value={newCommentText}
                  onChange={(e) => setNewCommentText(e.target.value)}
                  placeholder="Post an official administrator statement..."
                  disabled={actionLoading}
                  style={{
                    width: "100%",
                    minHeight: "80px",
                    background: "rgba(255,255,255,0.05)",
                    border: "1px solid var(--border-glass)",
                    borderRadius: "8px",
                    padding: "10px",
                    fontSize: "0.9rem",
                    resize: "none",
                    outline: "none",
                    color: "white",
                    fontFamily: "inherit",
                    boxSizing: "border-box"
                  }}
                />
                <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "12px" }}>
                  <button type="submit" className="btn-primary" disabled={actionLoading || !newCommentText.trim()}>
                    Post Comment
                  </button>
                </div>
              </form>
            </div>

            {/* Comments list thread */}
            <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "1.5rem", color: "var(--accent-purple, #A78BFA)" }}>
              Discussion Thread
            </h2>

            <div style={{ display: "flex", flexDirection: "column", gap: "16px", marginBottom: "4rem" }}>
              {comments.length === 0 ? (
                <div style={{ textAlign: "center", padding: "40px", color: "var(--text-muted)", background: "rgba(255,255,255,0.01)", borderRadius: "12px", border: "1px dashed var(--border-glass)" }}>
                  No comments yet.
                </div>
              ) : (
                comments.map((comment) => (
                  <AdminCommentNode
                    key={comment.id}
                    comment={comment}
                    decisionId={Number(decisionId)}
                    onCommentUpdated={handleCommentUpdated}
                    onCommentDeleted={handleCommentDeleted}
                    onPinComment={(pinnedCommentData) => setPinnedComment(pinnedCommentData)}
                  />
                ))
              )}
            </div>

            {/* Reported Comments Section */}
            <div style={{ borderTop: "1px solid var(--border-glass)", paddingTop: "3rem", marginBottom: "2rem" }}>
              <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "1.5rem", color: "#F87171" }}>
                Reported Comments
              </h2>

              <div className="decision-table-wrapper glass-panel">
                {reportedComments.length === 0 ? (
                  <p style={{ padding: "20px" }}>No reported comments found</p>
                ) : (
                  <table className="decision-table-element">
                    <thead>
                      <tr>
                        <th style={{ textAlign: "left" }}>Comment</th>
                        <th style={{ textAlign: "left" }}>Reported By</th>
                        <th style={{ textAlign: "left" }}>Reason</th>
                        <th style={{ textAlign: "left" }}>Decision</th>
                        <th style={{ textAlign: "left" }}>Date</th>
                        <th style={{ textAlign: "right" }}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reportedComments.map((report) => (
                        <tr key={report.id}>
                          <td style={{ maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                            {report.commentDeleted ? (
                              <span style={{ fontStyle: "italic", color: "var(--text-muted)" }}>[deleted]</span>
                            ) : (
                              report.commentContent
                            )}
                          </td>
                          <td>{report.reporterUsername}</td>
                          <td>{report.reason}</td>
                          <td>{report.decisionTitle}</td>
                          <td>{report.createdAt ? new Date(report.createdAt).toLocaleDateString() : "—"}</td>
                          <td style={{ textAlign: "right" }}>
                            <div style={{ display: "flex", justifyContent: "flex-end", gap: "8px" }}>
                              {!report.commentDeleted && (
                                <button
                                  className="btn-danger"
                                  onClick={async () => {
                                    if (!confirm("Delete comment? This comment will be removed from the discussion.")) return;
                                    try {
                                      setActionLoading(true);
                                      await api.delete(`/api/moderation/comments/${report.commentId}`);
                                      addToast("Comment removed", "success");
                                      handleCommentDeleted(report.commentId);
                                    } catch (err) {
                                      console.error("Failed to delete comment:", err);
                                      addToast("Failed to delete comment", "error");
                                    } finally {
                                      setActionLoading(false);
                                    }
                                  }}
                                  disabled={actionLoading}
                                  style={{ background: "none", border: "none", color: "#F87171", cursor: "pointer", fontSize: "0.85rem" }}
                                >
                                  Delete
                                </button>
                              )}
                              <button
                                className="btn-secondary"
                                onClick={() => handleDismissReport(report.id)}
                                disabled={actionLoading}
                                style={{ padding: "2px 8px", fontSize: "0.80rem" }}
                              >
                                Dismiss
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}

function AdminCommentNode({
  comment,
  decisionId,
  onCommentUpdated,
  onCommentDeleted,
  onPinComment
}) {
  const { addToast } = useToast();
  const [replies, setReplies] = useState([]);
  const [repliesLoaded, setRepliesLoaded] = useState(false);
  const [repliesVisible, setRepliesVisible] = useState(false);
  const [loadingReplies, setLoadingReplies] = useState(false);

  const [isReplying, setIsReplying] = useState(false);
  const [replyText, setReplyText] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  const MAX_DEPTH = 5;

  const loadReplies = () => {
    if (repliesLoaded) {
      setRepliesVisible(!repliesVisible);
      return;
    }
    setLoadingReplies(true);
    api.get(`/api/comments/${comment.id}/replies`)
      .then((res) => {
        setReplies(res.data || []);
        setRepliesLoaded(true);
        setRepliesVisible(true);
      })
      .catch((err) => {
        console.error("Failed to load replies:", err);
        addToast("Failed to load replies", "error");
      })
      .finally(() => setLoadingReplies(false));
  };

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    if (!replyText.trim()) return;

    try {
      setActionLoading(true);
      const res = await api.post(`/api/decisions/${decisionId}/comments/${comment.id}/replies`, {
        content: replyText
      });
      addToast("Reply posted", "success");
      setReplies((prev) => [...prev, res.data]);
      setRepliesLoaded(true);
      setRepliesVisible(true);
      setReplyText("");
      setIsReplying(false);
      onCommentUpdated(comment.id, { replyCount: comment.replyCount + 1 });
    } catch (err) {
      console.error("Failed to submit reply:", err);
      addToast("Failed to submit reply", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteComment = async () => {
    if (!confirm("Delete comment? This comment will be removed from the discussion.")) return;
    try {
      setActionLoading(true);
      await api.delete(`/api/moderation/comments/${comment.id}`);
      addToast("Comment deleted successfully", "success");
      onCommentDeleted(comment.id);
    } catch (err) {
      console.error("Failed to delete comment:", err);
      addToast("Failed to delete comment", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handlePinCommentToggle = async () => {
    try {
      setActionLoading(true);
      if (comment.pinned) {
        await api.put(`/api/moderation/comments/${comment.id}/unpin`, {});
        addToast("Comment unpinned", "success");
        onCommentUpdated(comment.id, { pinned: false });
        onPinComment(null);
      } else {
        const res = await api.put(`/api/moderation/comments/${comment.id}/pin`, {});
        addToast("Comment pinned", "success");
        // Update previous pinned comment in siblings tree if necessary, or let the parent update
        onPinComment(res.data);
        onCommentUpdated(comment.id, { pinned: true });
      }
    } catch (err) {
      console.error("Failed to toggle pin state:", err);
      addToast("Failed to pin comment", "error");
    } finally {
      setActionLoading(false);
    }
  };

  const handleChildUpdated = (childId, updatedData) => {
    setReplies((prev) =>
      prev.map((r) => (r.id === childId ? { ...r, ...updatedData } : r))
    );
  };

  const handleChildDeleted = (childId) => {
    const changes = { deleted: true, content: "[deleted]" };
    setReplies((prev) =>
      prev.map((r) => (r.id === childId ? { ...r, ...changes } : r))
    );
  };

  const canReply = comment.depth < MAX_DEPTH - 1 && !comment.deleted;

  return (
    <div
      style={{
        marginLeft: comment.depth > 0 ? "20px" : "0",
        paddingLeft: comment.depth > 0 ? "16px" : "0",
        borderLeft: comment.depth > 0 ? "3px solid rgba(167, 139, 250, 0.3)" : "none",
        marginTop: comment.depth > 0 ? "12px" : "0"
      }}
    >
      <div
        style={{
          background: "rgba(255,255,255,0.04)",
          borderRadius: "12px",
          padding: "16px",
          border: "1px solid var(--border-glass)"
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "8px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <div
              style={{
                width: "32px",
                height: "32px",
                borderRadius: "50%",
                background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
                color: "white",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontWeight: "bold",
                fontSize: "0.85rem"
              }}
            >
              {comment.deleted ? "-" : comment.username?.[0]?.toUpperCase() || "?"}
            </div>
            <div>
              <span style={{ fontWeight: "700", color: "var(--accent-purple, #A78BFA)", fontSize: "0.9rem" }}>
                {comment.deleted ? "" : comment.username}
              </span>
              <span style={{ display: "block", fontSize: "0.75rem", color: "var(--text-muted)" }}>
                {comment.createdAt ? new Date(comment.createdAt).toLocaleString() : ""}
              </span>
            </div>
          </div>

          <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
            {comment.pinned && (
              <span style={{ color: "#FBBF24", fontWeight: 700, fontSize: "0.8rem", display: "flex", alignItems: "center", gap: "4px" }}>
                <FaThumbtack /> Pinned
              </span>
            )}
            {!comment.deleted && (
              <button
                onClick={handleDeleteComment}
                disabled={actionLoading}
                style={{
                  background: "none",
                  border: "none",
                  cursor: "pointer",
                  color: "#F87171",
                  padding: "4px"
                }}
                title="Delete comment"
              >
                <FaTrash size={12} />
              </button>
            )}
          </div>
        </div>

        <p style={{ color: "white", fontSize: "0.95rem", margin: "0.5rem 0 1rem", wordBreak: "break-word" }}>
          {comment.deleted ? (
            <span style={{ fontStyle: "italic", color: "var(--text-muted)" }}>[deleted]</span>
          ) : (
            comment.content
          )}
        </p>

        {!comment.deleted && (
          <div style={{ display: "flex", gap: "16px", alignItems: "center" }}>
            {canReply && (
              <button
                onClick={() => setIsReplying(!isReplying)}
                disabled={actionLoading}
                style={{
                  background: "none",
                  border: "none",
                  color: "var(--accent-purple, #A78BFA)",
                  cursor: "pointer",
                  fontSize: "0.85rem",
                  fontWeight: "600",
                  display: "flex",
                  alignItems: "center",
                  gap: "4px"
                }}
              >
                <FaReply /> Reply
              </button>
            )}
          </div>
        )}

        {isReplying && (
          <form onSubmit={handleReplySubmit} style={{ marginTop: "12px" }}>
            <textarea
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Write a reply..."
              disabled={actionLoading}
              style={{
                width: "100%",
                minHeight: "60px",
                background: "rgba(255,255,255,0.05)",
                border: "1px solid var(--border-glass)",
                borderRadius: "8px",
                padding: "8px",
                fontSize: "0.85rem",
                resize: "none",
                outline: "none",
                color: "white",
                fontFamily: "inherit",
                boxSizing: "border-box"
              }}
            />
            <div style={{ display: "flex", gap: "8px", marginTop: "8px", justifyContent: "flex-end" }}>
              <button type="button" className="btn-secondary" onClick={() => setIsReplying(false)} disabled={actionLoading} style={{ padding: "4px 8px", fontSize: "0.8rem" }}>
                Cancel
              </button>
              <button type="submit" className="btn-primary" disabled={actionLoading || !replyText.trim()} style={{ padding: "4px 8px", fontSize: "0.8rem" }}>
                Submit
              </button>
            </div>
          </form>
        )}

        {comment.replyCount > 0 && (
          <button
            onClick={loadReplies}
            disabled={loadingReplies}
            style={{
              background: "none",
              border: "none",
              color: "var(--accent-purple, #A78BFA)",
              cursor: "pointer",
              fontSize: "0.85rem",
              fontWeight: "600",
              marginTop: "12px",
              padding: 0
            }}
          >
            {loadingReplies
              ? "Loading..."
              : repliesVisible
              ? "Hide replies"
              : `Show ${comment.replyCount} ${comment.replyCount === 1 ? "reply" : "replies"}`}
          </button>
        )}
      </div>

      {repliesVisible &&
        replies.map((reply) => (
          <AdminCommentNode
            key={reply.id}
            comment={reply}
            decisionId={decisionId}
            onCommentUpdated={handleChildUpdated}
            onCommentDeleted={handleChildDeleted}
            onPinComment={onPinComment}
          />
        ))}
    </div>
  );
}
