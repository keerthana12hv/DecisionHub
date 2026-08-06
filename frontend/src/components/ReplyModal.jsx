import { useState, useEffect } from "react";

export default function ReplyModal({ isOpen, onClose, onSubmit, userName }) {
  const [replyText, setReplyText] = useState("");
  const [error, setError] = useState("");

  // Reset fields when modal state changes
  useEffect(() => {
    if (isOpen) {
      setReplyText("");
      setError("");
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!replyText.trim()) {
      setError("Reply text cannot be empty.");
      return;
    }
    onSubmit(replyText.trim());
  };

  return (
    <div className="feedback-overlay">
      <form onSubmit={handleSubmit} className="reply-modal glass-panel animate-pop-in">
        <h2>Reply to {userName || "User"}</h2>

        <div className="feedback-form-group">
          <textarea
            placeholder="Write your response here..."
            value={replyText}
            onChange={(e) => {
              setReplyText(e.target.value);
              if (error && e.target.value.trim()) {
                setError("");
              }
            }}
            required
            rows={5}
          />
          {error && <span className="validation-error">{error}</span>}
        </div>

        <div className="reply-buttons">
          <button type="button" className="btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn-primary">
            Send Reply
          </button>
        </div>
      </form>
    </div>
  );
}
