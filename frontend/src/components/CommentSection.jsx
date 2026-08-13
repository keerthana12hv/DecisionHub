import { useState, useEffect } from "react";
import { useToast } from "../components/Toast";
import api from "../services/api";
import CommentThread from "./CommentThread";

function CommentSection({ decisionId, decisionStatus }) {
  const { addToast } = useToast();

  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newComment, setNewComment] = useState("");

  const isDecisionActive = decisionStatus === "ACTIVE";

  useEffect(() => {
    fetchComments();
  }, [decisionId]);

  const fetchComments = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/api/decisions/${decisionId}/comments`);
      setComments(res.data);
    } catch (error) {
      addToast("Failed to load comments.", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      const res = await api.post(`/api/decisions/${decisionId}/comments`, {
        content: newComment,
      });
      setComments((prev) => [...prev, res.data]);
      setNewComment("");
      addToast("Comment posted.", "success");
    } catch (error) {
      addToast(
        error.response?.data?.error || "Failed to post comment.",
        "error"
      );
    }
  };

  const handleCommentUpdated = (commentId, changes) => {
    setComments((prev) =>
      prev.map((c) => (c.id === commentId ? { ...c, ...changes } : c))
    );
  };

 const handleCommentDeleted = (commentId) => {
  setComments((prev) => prev.filter((c) => c.id !== commentId));
};

  return (
    <div className="comment-section">
      <h2>Discussion</h2>

      {isDecisionActive ? (
        <form onSubmit={handleAddComment} className="add-comment-form">
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Add a comment..."
            required
          />
          <button type="submit">Post Comment</button>
        </form>
      ) : (
        <p className="discussion-closed-notice">
          This decision is closed. Commenting is no longer available.
        </p>
      )}

      {loading ? (
        <p>Loading comments...</p>
      ) : comments.length === 0 ? (
        <p>No comments yet. Be the first to start the discussion.</p>
      ) : (
        comments.map((comment) => (
          <CommentThread
            key={comment.id}
            comment={comment}
            decisionId={decisionId}
            decisionStatus={decisionStatus}
            onCommentUpdated={handleCommentUpdated}
            onCommentDeleted={handleCommentDeleted}
          />
        ))
      )}
    </div>
  );
}

export default CommentSection;