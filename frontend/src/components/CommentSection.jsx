import { useEffect, useState } from "react";
import { getComments, postComment, postReply, deleteComment } from "../services/commentService";

export default function CommentsSection({ decisionId }) {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [replyText, setReplyText] = useState({});
  const [replyBoxOpen, setReplyBoxOpen] = useState(null);
  const [loading, setLoading] = useState(true);

  const currentUserId = localStorage.getItem("userId");

  useEffect(() => {
    fetchComments();
  }, [decisionId]);

  const fetchComments = async () => {
    try {
      const res = await getComments(decisionId);
      setComments(res.data);
    } catch (err) {
      console.error("Failed to fetch comments", err);
    } finally {
      setLoading(false);
    }
  };

  const handlePostComment = async () => {
    if (!newComment.trim()) return;
    await postComment(decisionId, newComment);
    setNewComment("");
    fetchComments();
  };

  const handleReply = async (commentId) => {
    const text = replyText[commentId];
    if (!text?.trim()) return;
    await postReply(commentId, text);
    setReplyText({ ...replyText, [commentId]: "" });
    setReplyBoxOpen(null);
    fetchComments();
  };

  const handleDelete = async (commentId) => {
    if (!window.confirm("Delete this comment?")) return;
    await deleteComment(commentId);
    fetchComments();
  };

  if (loading) return <p>Loading comments...</p>;

  return (
    <div className="comments-section">
      <h3>Discussion</h3>

      <div className="comment-input">
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Add a comment..."
        />
        <button onClick={handlePostComment}>Post</button>
      </div>

      {comments.map((c) => (
        <div key={c.id} className="comment-card">
          <div className="comment-header">
            <strong>{c.userName}</strong>
            <span className="timestamp">{new Date(c.createdAt).toLocaleString()}</span>
          </div>
          <p>{c.content}</p>

          <div className="comment-actions">
            <button onClick={() => setReplyBoxOpen(replyBoxOpen === c.id ? null : c.id)}>
              Reply
            </button>
            {c.userId === currentUserId && (
              <button onClick={() => handleDelete(c.id)}>Delete</button>
            )}
          </div>

          {replyBoxOpen === c.id && (
            <div className="reply-input">
              <input
                type="text"
                value={replyText[c.id] || ""}
                onChange={(e) => setReplyText({ ...replyText, [c.id]: e.target.value })}
                placeholder="Write a reply..."
              />
              <button onClick={() => handleReply(c.id)}>Send</button>
            </div>
          )}

          {c.replies?.map((r) => (
            <div key={r.id} className="reply-card">
              <strong>{r.userName}</strong>: <span>{r.content}</span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}