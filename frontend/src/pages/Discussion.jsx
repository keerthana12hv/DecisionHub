import { useState, useEffect } from "react";
import { FaComments, FaReply, FaThumbsUp, FaTrash } from "react-icons/fa";
import {
  getComments,
  postComment,
  postReply,
  deleteComment,
} from "../services/commentService";

const Discussion = ({ decisionId = 1 }) => {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [replyText, setReplyText] = useState("");
  const [replyingTo, setReplyingTo] = useState(null);
  const [loading, setLoading] = useState(true);

  // Fetch comments from backend
  useEffect(() => {
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

  // Add comment
  const addComment = () => {
    if (!newComment.trim()) return;
    postComment(decisionId, newComment)
      .then((res) => {
        setComments([...comments, res.data]);
        setNewComment("");
      })
      .catch(() => {
        setComments([...comments, {
          id: Date.now(),
          username: "Jyoti",
          content: newComment,
          createdAt: "Just now",
          replies: [],
        }]);
        setNewComment("");
      });
  };

  // Add reply
  const addReply = (commentId) => {
    if (!replyText.trim()) return;
    postReply(commentId, replyText)
      .then((res) => {
        setComments(comments.map((c) => {
          if (c.id === commentId) {
            return { ...c, replies: [...(c.replies || []), res.data] };
          }
          return c;
        }));
      })
      .catch(() => {
        setComments(comments.map((c) => {
          if (c.id === commentId) {
            return {
              ...c,
              replies: [...(c.replies || []), {
                id: Date.now(),
                username: "Jyoti",
                content: replyText,
                createdAt: "Just now",
              }],
            };
          }
          return c;
        }));
      });
    setReplyText("");
    setReplyingTo(null);
  };

  // Delete comment
  const handleDelete = (id) => {
    deleteComment(id)
      .then(() => setComments(comments.filter((c) => c.id !== id)))
      .catch(() => setComments(comments.filter((c) => c.id !== id)));
  };

  if (loading) return (
    <div style={{ textAlign: "center", padding: "50px", color: "#A78BFA" }}>
      Loading discussions...
    </div>
  );

  return (
    <div style={{
      padding: "24px",
      maxWidth: "800px",
      margin: "0 auto",
      fontFamily: "Inter, sans-serif"
    }}>
      {/* Header */}
      <div style={{ marginBottom: "24px" }}>
        <h1 style={{
          fontSize: "24px",
          fontWeight: "bold",
          color: "#A78BFA",
          display: "flex",
          alignItems: "center",
          gap: "10px"
        }}>
          <FaComments /> Discussion
        </h1>
        <p style={{ color: "#6B7280", marginTop: "4px" }}>
          Share your thoughts and opinions
        </p>
      </div>

      {/* Add Comment Box */}
      <div style={{
        background: "rgba(255,255,255,0.05)",
        borderRadius: "12px",
        padding: "20px",
        marginBottom: "24px",
        border: "1px solid rgba(167, 139, 250, 0.2)"
      }}>
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Share your thoughts..."
          style={{
            width: "100%",
            minHeight: "100px",
            background: "rgba(255,255,255,0.05)",
            border: "1px solid rgba(167, 139, 250, 0.3)",
            borderRadius: "8px",
            padding: "12px",
            fontSize: "14px",
            resize: "none",
            outline: "none",
            color: "white",
            fontFamily: "inherit",
            boxSizing: "border-box"
          }}
        />
        <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "12px" }}>
          <button
            onClick={addComment}
            style={{
              background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
              color: "white",
              border: "none",
              borderRadius: "8px",
              padding: "10px 24px",
              cursor: "pointer",
              fontWeight: "600",
              fontSize: "14px"
            }}
          >
            Post Comment
          </button>
        </div>
      </div>

      {/* Comments List */}
      <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
        {comments.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px", color: "#6B7280" }}>
            No comments yet. Be the first to share!
          </div>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} style={{
              background: "rgba(255,255,255,0.05)",
              borderRadius: "12px",
              padding: "20px",
              border: "1px solid rgba(167, 139, 250, 0.15)"
            }}>
              {/* Comment Header */}
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "12px" }}>
                  <div style={{
                    width: "40px",
                    height: "40px",
                    borderRadius: "50%",
                    background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
                    color: "white",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "bold",
                    fontSize: "16px"
                  }}>
                    {comment.username?.[0]?.toUpperCase()}
                  </div>
                  <div>
                    <p style={{ fontWeight: "600", color: "#A78BFA", fontSize: "14px" }}>
                      {comment.username}
                    </p>
                    <p style={{ color: "#6B7280", fontSize: "12px" }}>
                      {comment.createdAt}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(comment.id)}
                  style={{ background: "none", border: "none", cursor: "pointer", color: "#6B7280" }}
                >
                  <FaTrash size={14} />
                </button>
              </div>

              {/* Comment Text */}
              <p style={{ color: "#E5E7EB", fontSize: "14px", marginBottom: "12px" }}>
                {comment.content}
              </p>

              {/* Actions */}
              <div style={{ display: "flex", gap: "16px" }}>
                <button style={{
                  background: "none", border: "none", color: "#6B7280",
                  cursor: "pointer", fontSize: "13px",
                  display: "flex", alignItems: "center", gap: "6px"
                }}>
                  <FaThumbsUp /> {comment.likes || 0}
                </button>
                <button
                  onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}
                  style={{
                    background: "none", border: "none", color: "#A78BFA",
                    cursor: "pointer", fontSize: "13px", fontWeight: "600",
                    display: "flex", alignItems: "center", gap: "6px"
                  }}
                >
                  <FaReply /> Reply
                </button>
              </div>

              {/* Reply Box */}
              {replyingTo === comment.id && (
                <div style={{ marginTop: "12px", paddingLeft: "20px", borderLeft: "3px solid #7C3AED" }}>
                  <textarea
                    value={replyText}
                    onChange={(e) => setReplyText(e.target.value)}
                    placeholder="Write a reply..."
                    style={{
                      width: "100%", minHeight: "70px",
                      background: "rgba(255,255,255,0.05)",
                      border: "1px solid rgba(167, 139, 250, 0.3)",
                      borderRadius: "8px", padding: "10px",
                      fontSize: "13px", resize: "none", outline: "none",
                      color: "white", fontFamily: "inherit", boxSizing: "border-box"
                    }}
                  />
                  <div style={{ display: "flex", gap: "8px", marginTop: "8px", justifyContent: "flex-end" }}>
                    <button
                      onClick={() => setReplyingTo(null)}
                      style={{
                        background: "rgba(255,255,255,0.05)",
                        border: "1px solid rgba(167,139,250,0.2)",
                        borderRadius: "8px", padding: "8px 16px",
                        cursor: "pointer", fontSize: "13px", color: "white"
                      }}
                    >
                      Cancel
                    </button>
                    <button
                      onClick={() => addReply(comment.id)}
                      style={{
                        background: "linear-gradient(135deg, #7C3AED, #A78BFA)",
                        color: "white", border: "none", borderRadius: "8px",
                        padding: "8px 16px", cursor: "pointer",
                        fontSize: "13px", fontWeight: "600"
                      }}
                    >
                      Reply
                    </button>
                  </div>
                </div>
              )}

              {/* Replies */}
              {comment.replies?.length > 0 && (
                <div style={{ marginTop: "16px", paddingLeft: "20px", borderLeft: "3px solid rgba(167, 139, 250, 0.3)" }}>
                  {comment.replies.map((reply) => (
                    <div key={reply.id} style={{ marginBottom: "12px" }}>
                      <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "6px" }}>
                        <div style={{
                          width: "32px", height: "32px", borderRadius: "50%",
                          background: "linear-gradient(135deg, #EC4899, #A78BFA)",
                          color: "white", display: "flex", alignItems: "center",
                          justifyContent: "center", fontWeight: "bold", fontSize: "13px"
                        }}>
                          {reply.username?.[0]?.toUpperCase()}
                        </div>
                        <div>
                          <p style={{ fontWeight: "600", color: "#A78BFA", fontSize: "13px" }}>
                            {reply.username}
                          </p>
                          <p style={{ color: "#6B7280", fontSize: "11px" }}>
                            {reply.createdAt}
                          </p>
                        </div>
                      </div>
                      <p style={{ color: "#E5E7EB", fontSize: "13px", paddingLeft: "42px" }}>
                        {reply.content}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Discussion;