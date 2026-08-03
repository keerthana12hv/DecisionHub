import { FaCommentSlash } from "react-icons/fa";

export default function EmptyState({ message = "No feedback available." }) {
  return (
    <div className="empty-state glass-panel animate-fade-in">
      <div className="empty-state-icon">
        <FaCommentSlash />
      </div>
      <h3>No Feedback Found</h3>
      <p>{message}</p>
    </div>
  );
}
