import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaVoteYea, FaCommentDots, FaHeart, FaShareAlt, FaCrown, FaReply } from "react-icons/fa";
import "../styles/VotingPage.css";

const STORAGE_KEY = "decisionhub-decisions";

const VotingPage = () => {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [polls, setPolls] = useState([]);
  const [commentInputs, setCommentInputs] = useState({});
  const [replyInputs, setReplyInputs] = useState({});
  const [activeReplyBox, setActiveReplyBox] = useState(null); // id of comment
  const [justVotedPollId, setJustVotedPollId] = useState(null);

  useEffect(() => {
    const storedDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setPolls(storedDecisions);
  }, []);

  const persistPolls = (nextPolls) => {
    setPolls(nextPolls);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
  };

  const handleVote = (pollId, optionId) => {
    if (!user) {
      addToast("Please login to cast your vote.", "error");
      return;
    }

    const nextPolls = polls.map((poll) => {
      const hasVoted = poll.userVoteOptionId != null;
      if (poll.id !== pollId || hasVoted) {
        return poll;
      }
      return {
        ...poll,
        userVoteOptionId: optionId,
        options: poll.options.map((option) =>
          option.id === optionId ? { ...option, votes: option.votes + 1 } : option
        )
      };
    });

    persistPolls(nextPolls);
    setJustVotedPollId(pollId);
    addToast("Your vote has been recorded!", "success");

    // Add Activity Entry
    const activities = JSON.parse(localStorage.getItem("decisionhub-activities") || "[]");
    activities.unshift({
      id: Date.now(),
      icon: "vote",
      text: `${user.username} voted on '${polls.find(p => p.id === pollId)?.title}'`,
      time: "Just now"
    });
    localStorage.setItem("decisionhub-activities", JSON.stringify(activities.slice(0, 10)));

    // Clean up animation state after 3 seconds
    setTimeout(() => {
      setJustVotedPollId(null);
    }, 3000);
  };

  const handleCommentSubmit = (pollId, event) => {
    event.preventDefault();
    const commentText = commentInputs[pollId]?.trim();
    if (!commentText) return;

    const nextPolls = polls.map((poll) => {
      if (poll.id === pollId) {
        const nextComment = {
          id: Date.now(),
          user: user?.username || "Anonymous",
          text: commentText,
          likes: 0,
          reactions: {},
          replies: []
        };
        return {
          ...poll,
          comments: [...(poll.comments || []), nextComment]
        };
      }
      return poll;
    });

    persistPolls(nextPolls);
    setCommentInputs((prev) => ({ ...prev, [pollId]: "" }));
    addToast("Comment posted!", "success");
  };

  // Reply submit logic
  const handleReplySubmit = (pollId, commentId, event) => {
    event.preventDefault();
    const replyText = replyInputs[commentId]?.trim();
    if (!replyText) return;

    const nextPolls = polls.map((poll) => {
      if (poll.id === pollId) {
        return {
          ...poll,
          comments: poll.comments.map((comment) => {
            if (comment.id === commentId) {
              const newReply = {
                id: Date.now(),
                user: user?.username || "Anonymous",
                text: replyText
              };
              return {
                ...comment,
                replies: [...(comment.replies || []), newReply]
              };
            }
            return comment;
          })
        };
      }
      return poll;
    });

    persistPolls(nextPolls);
    setReplyInputs((prev) => ({ ...prev, [commentId]: "" }));
    setActiveReplyBox(null);
    addToast("Reply posted!", "success");
  };

  // Like a comment
  const handleLikeComment = (pollId, commentId) => {
    const nextPolls = polls.map((poll) => {
      if (poll.id === pollId) {
        return {
          ...poll,
          comments: poll.comments.map((comment) => {
            if (comment.id === commentId) {
              return { ...comment, likes: (comment.likes || 0) + 1 };
            }
            return comment;
          })
        };
      }
      return poll;
    });
    persistPolls(nextPolls);
  };

  // Emoji Reactions
  const handleReaction = (pollId, commentId, emoji) => {
    const nextPolls = polls.map((poll) => {
      if (poll.id === pollId) {
        return {
          ...poll,
          comments: poll.comments.map((comment) => {
            if (comment.id === commentId) {
              const reactions = { ...comment.reactions };
              reactions[emoji] = (reactions[emoji] || 0) + 1;
              return { ...comment, reactions };
            }
            return comment;
          })
        };
      }
      return poll;
    });
    persistPolls(nextPolls);
  };

  const handleShare = (pollId) => {
    const shareUrl = `${window.location.origin}/vote?id=${pollId}`;
    navigator.clipboard.writeText(shareUrl);
    addToast("Voting room URL copied to clipboard!", "success");
  };

  const activePolls = polls.filter((p) => p.status === "Active");

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="voting-page-container">
            <div className="voting-header">
              <h1>Collaborative Voting Room</h1>
              <p>Express your voice, view dynamic tallies, and debate choices in real-time.</p>
            </div>

            <div className="polls-feed-grid">
              {activePolls.length === 0 ? (
                <div className="glass-card empty-voting-state">
                  <FaVoteYea className="empty-icon" />
                  <h3>No Active Decisions</h3>
                  <p>All decision proposals are currently closed. Check back later or draft a new one.</p>
                </div>
              ) : (
                activePolls.map((poll) => {
                  const totalVotes = poll.options.reduce((sum, option) => sum + option.votes, 0);
                  const hasVoted = poll.userVoteOptionId != null;

                  // Find winning option
                  let winningOptionId = null;
                  if (hasVoted) {
                    let maxVotes = -1;
                    poll.options.forEach((opt) => {
                      if (opt.votes > maxVotes) {
                        maxVotes = opt.votes;
                        winningOptionId = opt.id;
                      }
                    });
                  }

                  return (
                    <div key={poll.id} className="voting-card glass-panel animate-pop-in">
                      {/* Voting Room Header */}
                      <div className="voting-card-header">
                        <div>
                          <span className="category-tag">{poll.category}</span>
                          <h2>{poll.title}</h2>
                        </div>
                        <button className="share-vote-btn" onClick={() => handleShare(poll.id)} title="Share Poll">
                          <FaShareAlt />
                        </button>
                      </div>

                      <p className="poll-description">{poll.description}</p>
                      
                      <div className="voting-options-list">
                        {poll.options.map((option) => {
                          const percentage = totalVotes === 0 ? 0 : Math.round((option.votes / totalVotes) * 100);
                          const isWinner = winningOptionId === option.id;
                          const isSelected = poll.userVoteOptionId === option.id;

                          return (
                            <div
                              key={option.id}
                              className={`voting-option-block ${hasVoted ? "disabled" : ""} ${isSelected ? "selected-choice" : ""}`}
                            >
                              {!hasVoted ? (
                                <button onClick={() => handleVote(poll.id, option.id)} className="vote-click-trigger">
                                  <span className="option-name-txt">{option.name}</span>
                                  <span className="click-vote-hint">Vote</span>
                                </button>
                              ) : (
                                <div className="voted-option-result">
                                  <div className="result-bar-labels">
                                    <span className="option-label">
                                      {option.name}
                                      {isSelected && <span className="your-vote-tag"> (Your Vote)</span>}
                                    </span>
                                    <span className="percentage-label">
                                      {percentage}% ({option.votes} votes)
                                    </span>
                                  </div>
                                  <div className="progress-bar-container">
                                    <div
                                      className={`progress-bar-fill ${isWinner ? "winner-fill" : "default-fill"}`}
                                      style={{ width: `${percentage}%` }}
                                    ></div>
                                  </div>
                                  {isWinner && (
                                    <span className="winner-badge-lbl">
                                      <FaCrown /> Leader
                                    </span>
                                  )}
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>

                      <div className="voting-card-footer">
                        <span>Total Votes: <strong>{totalVotes}</strong></span>
                        <span>Deadline: {poll.deadline}</span>
                      </div>

                      {/* Thank You Animation Overlay inside card */}
                      {justVotedPollId === poll.id && (
                        <div className="thanks-overlay-anim animate-pop-in">
                          <div className="checkmark-circle">
                            <div className="checkmark-draw"></div>
                          </div>
                          <h3>Thank You for Voting!</h3>
                          <p>Your contribution helps steer community initiatives.</p>
                        </div>
                      )}

                      {/* Comment Section Redesign */}
                      <div className="comments-section-container">
                        <div className="comments-title">
                          <FaCommentDots />
                          <h3>Debate & Discussion ({poll.comments?.length || 0})</h3>
                        </div>

                        {/* Add Comment */}
                        <form onSubmit={(e) => handleCommentSubmit(poll.id, e)} className="comment-post-form">
                          <textarea
                            rows="2"
                            value={commentInputs[poll.id] || ""}
                            onChange={(e) => setCommentInputs({ ...commentInputs, [poll.id]: e.target.value })}
                            placeholder="Add your thoughts and logic..."
                            required
                          />
                          <button type="submit" className="btn-primary comment-submit-btn">Post Comment</button>
                        </form>

                        {/* Comment Feed */}
                        <div className="comments-feed-list">
                          {(poll.comments || []).map((comment) => (
                            <div key={comment.id} className="comment-feed-item">
                              <div className="comment-avatar">
                                {comment.user.charAt(0).toUpperCase()}
                              </div>
                              <div className="comment-content-main">
                                <div className="comment-user-meta">
                                  <strong>{comment.user}</strong>
                                  <span className="comment-time-lbl">Just now</span>
                                </div>
                                <p className="comment-text-content">{comment.text}</p>

                                {/* Comment Actions (Like, Reply, Reactions) */}
                                <div className="comment-actions-row">
                                  <button className="comment-action-btn like" onClick={() => handleLikeComment(poll.id, comment.id)}>
                                    <FaHeart /> <span>{comment.likes || 0} Likes</span>
                                  </button>
                                  <button className="comment-action-btn reply" onClick={() => setActiveReplyBox(activeReplyBox === comment.id ? null : comment.id)}>
                                    <FaReply /> Reply
                                  </button>

                                  {/* Emoji triggers */}
                                  <div className="emoji-reaction-bar">
                                    {["👍", "❤️", "😮", "🎉", "🚀"].map(emoji => (
                                      <button
                                        key={emoji}
                                        className="emoji-btn"
                                        onClick={() => handleReaction(poll.id, comment.id, emoji)}
                                      >
                                        {emoji} <span className="emoji-count">{comment.reactions?.[emoji] || 0}</span>
                                      </button>
                                    ))}
                                  </div>
                                </div>

                                {/* Nested Replies List */}
                                {comment.replies && comment.replies.length > 0 && (
                                  <div className="nested-replies-container">
                                    {comment.replies.map((rep) => (
                                      <div key={rep.id} className="reply-item">
                                        <div className="reply-avatar">{rep.user.charAt(0).toUpperCase()}</div>
                                        <div>
                                          <div className="reply-meta">
                                            <strong>{rep.user}</strong>
                                          </div>
                                          <p className="reply-text">{rep.text}</p>
                                        </div>
                                      </div>
                                    ))}
                                  </div>
                                )}

                                {/* Reply Input Box */}
                                {activeReplyBox === comment.id && (
                                  <form onSubmit={(e) => handleReplySubmit(poll.id, comment.id, e)} className="reply-input-box animate-pop-in">
                                    <input
                                      type="text"
                                      placeholder={`Reply to ${comment.user}...`}
                                      value={replyInputs[comment.id] || ""}
                                      onChange={(e) => setReplyInputs({ ...replyInputs, [comment.id]: e.target.value })}
                                      required
                                    />
                                    <button type="submit" className="btn-primary">Send</button>
                                  </form>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VotingPage;