import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import axios from "axios";
import RatingPanel from "../components/RatingPanel";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { useAuth } from "../context/AuthContext";
import { FaArrowLeft, FaCalendarAlt, FaVoteYea, FaCommentAlt, FaPaperPlane } from "react-icons/fa";

import "../styles/DecisionDetail.css";

const API = "http://localhost:8080/api";

const getToken = () => {
  return (
    localStorage.getItem("token") ||
    localStorage.getItem("authToken") ||
    localStorage.getItem("jwt")
  );
};

export default function DecisionDetail() {
  const params = useParams();
  const { user } = useAuth();
  const { addToast } = useToast();

  const decisionId = params.decisionId || params.id;

  const [decision, setDecision] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  
  // Voting State
  const [selectedOption, setSelectedOption] = useState(null);
  const [votingProgress, setVotingProgress] = useState(false);
  const [hasVoted, setHasVoted] = useState(false);
  const [myVote, setMyVote] = useState(null);

  // Comments State (LocalStorage Fallback for Milestone 3 Demo)
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [replyInputs, setReplyInputs] = useState({});
  const [activeReplyId, setActiveReplyId] = useState(null);

  // =========================================================
  // FETCH DECISION & VOTE
  // =========================================================

  const fetchDecision = async () => {
    try {
      setLoading(true);
      setError("");

      const authToken = getToken();
      if (!authToken) {
        setError("You are not logged in. Please login again.");
        return;
      }

      const response = await axios.get(
        `${API}/decisions/${decisionId}`,
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
            "Content-Type": "application/json",
          },
        }
      );

      setDecision(response.data);

      // Try fetching user's existing vote
      try {
        const myVoteRes = await axios.get(
          `${API}/decisions/${decisionId}/votes/me`,
          {
            headers: {
              Authorization: `Bearer ${authToken}`,
            },
          }
        );
        if (myVoteRes.data && myVoteRes.data.optionIds && myVoteRes.data.optionIds.length > 0) {
          setMyVote(myVoteRes.data);
          setHasVoted(true);
          setSelectedOption(myVoteRes.data.optionIds[0]);
        }
      } catch (voteErr) {
        console.log("No existing vote found or error fetching vote");
      }

    } catch (err) {
      console.error("FETCH DECISION ERROR:", err);
      setError(err.response?.data?.message || "Failed to load decision.");
    } finally {
      setLoading(false);
    }
  };

  // Load comments from localstorage keyed by decisionId
  const loadComments = () => {
    const allComments = JSON.parse(localStorage.getItem(`decisionhub-comments-${decisionId}`) || "[]");
    setComments(allComments);
  };

  useEffect(() => {
    if (decisionId) {
      fetchDecision();
      loadComments();
    }
  }, [decisionId]);

  // =========================================================
  // SUBMIT VOTE
  // =========================================================

  const handleVoteSubmit = async () => {
    if (!selectedOption) {
      addToast("Please select an option to vote.", "error");
      return;
    }

    setVotingProgress(true);
    try {
      const authToken = getToken();
      await axios.put(
        `${API}/decisions/${decisionId}/votes`,
        {
          optionIds: [selectedOption]
        },
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
            "Content-Type": "application/json"
          }
        }
      );

      addToast("Your vote has been successfully cast!", "success");
      setHasVoted(true);
      
      // Refresh decision options stats
      fetchDecision();
    } catch (err) {
      console.error("VOTE ERROR:", err);
      addToast(err.response?.data?.message || "Failed to cast vote.", "error");
    } finally {
      setVotingProgress(false);
    }
  };

  const handlePublishDecision = async () => {
    try {
      const authToken = getToken();
      await axios.put(
        `${API}/decisions/${decisionId}/publish`,
        {},
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
            "Content-Type": "application/json"
          }
        }
      );
      addToast("Decision published successfully! It is now ACTIVE and open for voting.", "success");
      fetchDecision();
    } catch (err) {
      console.error("PUBLISH ERROR:", err);
      addToast(err.response?.data?.message || "Failed to publish decision.", "error");
    }
  };

  // =========================================================
  // COMMENTS INTERACTIONS
  // =========================================================

  const handleAddComment = (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    const nextComment = {
      id: Date.now(),
      author: user?.username || "Anonymous User",
      text: newComment.trim(),
      time: "Just now",
      replies: []
    };

    const nextComments = [nextComment, ...comments];
    setComments(nextComments);
    localStorage.setItem(`decisionhub-comments-${decisionId}`, JSON.stringify(nextComments));
    setNewComment("");
    addToast("Comment posted successfully!", "success");
  };

  const handleAddReply = (commentId, e) => {
    e.preventDefault();
    const replyText = replyInputs[commentId]?.trim();
    if (!replyText) return;

    const nextReply = {
      id: Date.now(),
      author: user?.username || "Anonymous User",
      text: replyText,
      time: "Just now"
    };

    const nextComments = comments.map(comment => {
      if (comment.id === commentId) {
        return {
          ...comment,
          replies: [...(comment.replies || []), nextReply]
        };
      }
      return comment;
    });

    setComments(nextComments);
    localStorage.setItem(`decisionhub-comments-${decisionId}`, JSON.stringify(nextComments));
    setReplyInputs(prev => ({ ...prev, [commentId]: "" }));
    setActiveReplyId(null);
    addToast("Reply posted successfully!", "success");
  };

  // =========================================================
  // RENDER BLOCKS
  // =========================================================

  if (loading) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content decision-detail-page">
            <h2 style={{ color: "#fff" }}>Loading decision details...</h2>
          </div>
        </div>
      </div>
    );
  }

  if (error || !decision) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="dashboard-content decision-detail-page">
            <h2 style={{ color: "#ef4444" }}>Error Loading Decision</h2>
            <p style={{ color: "#9ca3af" }}>{error || "Decision not found."}</p>
            <Link to="/decisions" className="btn-vote" style={{ display: "inline-block", marginTop: "15px", textDecoration: "none" }}>
              Back to Decisions
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const pollOpen = decision.status === "ACTIVE" || decision.status === "Active";
  const isDraft = decision.status === "DRAFT" || decision.status === "Draft";

  // Calculate vote percentages
  const totalVotes = decision.options?.reduce((acc, opt) => acc + (opt.votes?.length || 0), 0) || 0;

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content decision-detail-page">
          
          <Link to="/decisions" className="back-link">
            <FaArrowLeft /> Back to Decisions
          </Link>

          {/* MAIN CARD */}
          <div className="decision-detail-card">
            <div className="decision-detail-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "15px" }}>
              <div>
                <h1>{decision.title}</h1>
                <p className="description">{decision.description}</p>
              </div>
              {isDraft && (decision.creator?.username === user?.username || decision.creator?.id === user?.id) && (
                <button
                  className="btn-vote"
                  onClick={handlePublishDecision}
                  style={{ background: "#10b981", borderColor: "#10b981", padding: "10px 20px", borderRadius: "8px", fontWeight: "bold" }}
                >
                  🚀 Publish Decision
                </button>
              )}
            </div>

            {/* METADATA BADGES */}
            <div className="decision-meta-badges">
              <span className={`meta-pill ${pollOpen ? "active" : isDraft ? "draft" : "closed"}`}>
                {pollOpen ? "● Active Poll" : isDraft ? "● Draft" : "● Closed"}
              </span>
              <span className="meta-pill voting-type">
                🗳️ {decision.votingType?.replace("_", " ")}
              </span>
              {decision.votingEndTime && (
                <span className="meta-pill">
                  <FaCalendarAlt /> Ends: {new Date(decision.votingEndTime).toLocaleDateString()}
                </span>
              )}
            </div>

            {/* RATING BASED SYSTEM */}
            {decision.votingType === "RATING_BASED" && (
              <RatingPanel decision={decision} pollOpen={pollOpen} />
            )}

            {/* SINGLE CHOICE SYSTEM */}
            {decision.votingType === "SINGLE_CHOICE" && (
              <div className="voting-section">
                <h2>{pollOpen && !hasVoted ? "Cast Your Vote" : "Voting Results"}</h2>

                {pollOpen && !hasVoted ? (
                  /* VOTE INPUT VIEW */
                  <>
                    <div className="options-container">
                      {decision.options?.map((option) => (
                        <div
                          key={option.id}
                          className={`option-card ${selectedOption === option.id ? "selected" : ""}`}
                          onClick={() => setSelectedOption(option.id)}
                        >
                          <div className="option-radio">
                            <div className="option-radio-dot"></div>
                          </div>
                          <div className="option-details">
                            <span className="option-title">{option.title || option.optionName}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                    <div className="vote-submit-bar">
                      <button
                        className="btn-vote"
                        onClick={handleVoteSubmit}
                        disabled={selectedOption === null || votingProgress}
                      >
                        {votingProgress ? "Submitting..." : "Submit Vote"}
                      </button>
                    </div>
                  </>
                ) : (
                  /* VOTE RESULTS VIEW */
                  <div className="results-display">
                    {decision.options?.map((option) => {
                      const votesCount = option.votes?.length || 0;
                      const percent = totalVotes > 0 ? (votesCount / totalVotes) * 100 : 0;
                      return (
                        <div key={option.id} className="result-bar-wrapper">
                          <div className="result-bar-info">
                            <span className="result-bar-title">
                              {option.title || option.optionName} {selectedOption === option.id && "⭐ (Your Vote)"}
                            </span>
                            <span className="result-bar-stats">
                              {votesCount} votes ({percent.toFixed(0)}%)
                            </span>
                          </div>
                          <div className="progress-track">
                            <div className="progress-fill" style={{ width: `${percent}%` }}></div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}

            {/* MULTIPLE CHOICE SYSTEM */}
            {decision.votingType === "MULTIPLE_CHOICE" && (
              <div className="voting-section">
                <h2>Voting Results</h2>
                <div className="results-display">
                  {decision.options?.map((option) => {
                    const votesCount = option.votes?.length || 0;
                    const percent = totalVotes > 0 ? (votesCount / totalVotes) * 100 : 0;
                    return (
                      <div key={option.id} className="result-bar-wrapper">
                        <div className="result-bar-info">
                          <span className="result-bar-title">{option.title || option.optionName}</span>
                          <span className="result-bar-stats">
                            {votesCount} votes ({percent.toFixed(0)}%)
                          </span>
                        </div>
                        <div className="progress-track">
                          <div className="progress-fill" style={{ width: `${percent}%` }}></div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

          </div>

          {/* COMMENTS & DISCUSSIONS */}
          <div className="discussion-section">
            <h2><FaCommentAlt /> Discussions</h2>

            {/* Comment Form */}
            <form onSubmit={handleAddComment} className="comment-input-form">
              <textarea
                placeholder="Share your thoughts or feedback about this decision..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                required
              />
              <button type="submit" className="btn-vote" style={{ display: "inline-flex", alignItems: "center", gap: "8px" }}>
                <FaPaperPlane /> Post Comment
              </button>
            </form>

            {/* Comments list */}
            <div className="comments-list">
              {comments.length > 0 ? (
                comments.map((comment) => (
                  <div key={comment.id} className="comment-bubble">
                    <div className="comment-header">
                      <span className="comment-author">@{comment.author}</span>
                      <span className="comment-time">{comment.time}</span>
                    </div>
                    <p className="comment-text">{comment.text}</p>
                    
                    <div className="comment-actions">
                      <button 
                        onClick={() => setActiveReplyId(activeReplyId === comment.id ? null : comment.id)}
                        className="btn-action"
                      >
                        Reply
                      </button>
                    </div>

                    {/* Replies */}
                    {comment.replies && comment.replies.length > 0 && (
                      <div style={{ marginLeft: "30px", marginTop: "15px", paddingLeft: "15px", borderLeft: "2px solid rgba(255,255,255,0.06)" }}>
                        {comment.replies.map(reply => (
                          <div key={reply.id} className="comment-bubble" style={{ marginTop: "10px", padding: "12px 15px", background: "rgba(255,255,255,0.01)" }}>
                            <div className="comment-header" style={{ marginBottom: "5px" }}>
                              <span className="comment-author" style={{ fontSize: "13px" }}>@{reply.author}</span>
                              <span className="comment-time" style={{ fontSize: "11px" }}>{reply.time}</span>
                            </div>
                            <p className="comment-text" style={{ fontSize: "13px", margin: 0 }}>{reply.text}</p>
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Reply Input Box */}
                    {activeReplyId === comment.id && (
                      <form 
                        onSubmit={(e) => handleAddReply(comment.id, e)} 
                        className="comment-input-form" 
                        style={{ marginLeft: "30px", marginTop: "15px" }}
                      >
                        <textarea
                          placeholder="Write a reply..."
                          value={replyInputs[comment.id] || ""}
                          onChange={(e) => setReplyInputs(prev => ({ ...prev, [comment.id]: e.target.value }))}
                          required
                          style={{ minHeight: "60px", fontSize: "13px" }}
                        />
                        <button type="submit" className="btn-vote" style={{ padding: "8px 16px", fontSize: "13px" }}>
                          Reply
                        </button>
                      </form>
                    )}

                  </div>
                ))
              ) : (
                <p style={{ color: "#9ca3af", textAlign: "center", padding: "20px" }}>
                  No comments yet. Start the conversation!
                </p>
              )}
            </div>

          </div>

        </div>
      </div>
    </div>
  );
}