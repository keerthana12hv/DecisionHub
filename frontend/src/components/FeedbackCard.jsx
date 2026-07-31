import StarRating from "./StarRating";

export default function FeedbackCard({ feedback, onReplyClick }) {
  const formattedDate = feedback.submissionDate
    ? new Date(feedback.submissionDate).toLocaleString(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      })
    : "Date Unknown";

  return (
    <div className="feedback-card glass-panel animate-fade-in">
      <div className="feedback-card-header">
        <div className="feedback-card-user-info">
          <span className="feedback-card-username">{feedback.userName || "Anonymous User"}</span>
          <span className="feedback-card-date">{formattedDate}</span>
        </div>
      </div>

      <div className="feedback-card-body">
        <div className="feedback-card-ratings">
          <div className="feedback-card-rating-row">
            <span className="feedback-card-rating-label">Ease of Use</span>
            <StarRating rating={feedback.easeOfUseRating || feedback.rating || 0} readOnly={true} />
          </div>
          <div className="feedback-card-rating-row">
            <span className="feedback-card-rating-label">Feature Satisfaction</span>
            <StarRating rating={feedback.featureSatisfactionRating || feedback.rating || 0} readOnly={true} />
          </div>
          <div className="feedback-card-rating-row">
            <span className="feedback-card-rating-label">Overall Experience</span>
            <StarRating rating={feedback.overallExperienceRating || feedback.rating || 0} readOnly={true} />
          </div>
        </div>

        <div className="feedback-section">
          <span className="feedback-section-label">Experience</span>
          <p className="feedback-text">{feedback.experience}</p>
        </div>

        {feedback.queries && (
          <div className="feedback-section">
            <span className="feedback-section-label">Queries</span>
            <div className="feedback-queries-box">
              <p className="feedback-text">{feedback.queries}</p>
            </div>
          </div>
        )}

        {feedback.reply && (
          <div className="feedback-section">
            <span className="feedback-section-label">Admin Reply</span>
            <div className="feedback-reply-box">
              <p className="feedback-text">{feedback.reply}</p>
            </div>
          </div>
        )}
      </div>

      <div className="feedback-card-footer">
        <button
          className="btn-secondary"
          onClick={() => onReplyClick(feedback)}
        >
          Reply to {feedback.userName || "Anonymous User"}
        </button>
      </div>
    </div>
  );
}
