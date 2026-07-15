import { useEffect, useState } from "react";
import "../styles/VotingPage.css";
import EditDecisionModal from "../components/EditDecisionModal";

const STORAGE_KEY = "decisionhub-decisions";
const USER_KEY = "decisionhub-current-user";

function getCurrentUser() {
  return localStorage.getItem(USER_KEY) || "Mythili";
}

const VotingPage = () => {
  const [polls, setPolls] = useState([]);
  const [commentInputs, setCommentInputs] = useState({});
  const currentUser = getCurrentUser();
  const [showEdit, setShowEdit] = useState(false);
  const [editDecision, setEditDecision] = useState(null);

  useEffect(() => {
    const storedDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setPolls(storedDecisions);
  }, []);

  const persistPolls = (nextPolls) => {
    setPolls(nextPolls);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
  };

  const handleVote = (pollId, optionId) => {
    setPolls((prevPolls) => {
      const nextPolls = prevPolls.map((poll) => {
        const hasVoted = poll.userVoteOptionId != null;

        if (poll.id !== pollId || hasVoted) {
          return poll;
        }

        return {
          ...poll,
          userVoteOptionId: optionId,
          options: poll.options.map((option) =>
            option.id === optionId ? { ...option, votes: option.votes + 1 } : option
          ),
        };
      });

      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
      return nextPolls;
    });
  };

  const handleCommentSubmit = (pollId, event) => {
    event.preventDefault();
    const commentText = commentInputs[pollId]?.trim();

    if (!commentText) return;

    setPolls((prevPolls) => {
      const nextPolls = prevPolls.map((poll) =>
        poll.id === pollId
          ? {
              ...poll,
              comments: [...(poll.comments || []), { id: Date.now(), user: "You", text: commentText }],
            }
          : poll
      );

      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextPolls));
      return nextPolls;
    });

    setCommentInputs((prev) => ({ ...prev, [pollId]: "" }));
  };

  const handleSave = (updated) => {
    setPolls((prevPolls) => {
      const next = prevPolls.map((p) => (p.id === editDecision.id ? { ...p, ...updated } : p));
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
    setShowEdit(false);
    setEditDecision(null);
    alert("Decision updated");
  };

  return (
    <div className="voting-container">
      <div className="polls-list">
        {polls.length === 0 ? (
          <div className="voting-card">
            <h1>No polls yet</h1>
            <p className="decision-description">Create a decision first and it will appear here.</p>
          </div>
        ) : (
          polls.map((poll) => {
          const totalVotes = (poll.options || []).reduce((sum, option) => sum + (option.votes || 0), 0);
          const hasVoted = poll.userVoteOptionId != null;
          const isOwner = poll.creator === currentUser;
          const isActive = poll.status === "Active";
          const isPublic = poll.visibility === "Public";
          const invited = (poll.invites || []).includes(currentUser);
          const canVote = !hasVoted && isActive && ((isPublic && !isOwner) || (!isPublic && (isOwner || invited)));

          return (
            <div key={poll.id} className="voting-card">
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <h1>{poll.title}</h1>
                {poll.creator === currentUser && (
                  <button className="edit-inline-btn" onClick={() => { setEditDecision(poll); setShowEdit(true); }}>
                    Edit
                  </button>
                )}
              </div>
              <p className="decision-description">{poll.description}</p>

              {poll.options.map((option) => {
                const percentage = totalVotes === 0 ? 0 : Math.round((option.votes / totalVotes) * 100);

                return (
                  <div key={option.id} className="option-block">
                    <div className="option-row">
                      <span>{option.name}</span>
                      {hasVoted && <span className="percentage">{percentage}%</span>}
                    </div>

                    {hasVoted && (
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${percentage}%` }}></div>
                      </div>
                    )}

                    {canVote ? (
                      <button onClick={() => handleVote(poll.id, option.id)} className="vote-btn-card">
                        Vote for {option.name}
                      </button>
                    ) : (
                      !hasVoted && <div style={{ color: "#9ca3af" }}>{isOwner ? "Can't vote your own decision" : isPublic ? "Voting unavailable" : "Private poll"}</div>
                    )}

                    {hasVoted && poll.userVoteOptionId === option.id && (
                      <span className="selected-badge">✅ You voted this</span>
                    )}
                  </div>
                );
              })}

              <p className="total-votes">Total votes: {totalVotes}</p>

              {hasVoted && <div className="thanks-box">🎉 Thank you for voting!</div>}

              <div className="comments-section">
                <h3>Comments</h3>
                <form onSubmit={(event) => handleCommentSubmit(poll.id, event)} className="comment-form">
                  <textarea
                    rows="3"
                    value={commentInputs[poll.id] || ""}
                    onChange={(event) =>
                      setCommentInputs((prev) => ({ ...prev, [poll.id]: event.target.value }))
                    }
                    placeholder="Share your thoughts..."
                  />
                  <button type="submit">Post Comment</button>
                </form>

                <div className="comment-list">
                  {(poll.comments || []).map((comment) => (
                    <div key={comment.id} className="comment-item">
                      <strong>{comment.user}</strong>
                      <p>{comment.text}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            );
          })
        )}
      </div>
      {showEdit && editDecision && (
        <EditDecisionModal
          decision={editDecision}
          onClose={() => { setShowEdit(false); setEditDecision(null); }}
          onSave={handleSave}
        />
      )}
    </div>
  );
};

export default VotingPage;