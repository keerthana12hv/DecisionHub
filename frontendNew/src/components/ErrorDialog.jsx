import { FaExclamationTriangle } from "react-icons/fa";

export default function ErrorDialog({ isOpen, onClose, message }) {
  if (!isOpen) return null;

  return (
    <div className="feedback-overlay">
      <div className="feedback-dialog glass-panel animate-pop-in">
        <div className="dialog-icon error">
          <FaExclamationTriangle />
        </div>
        <h2>Submission Failed</h2>
        <p>{message || "Failed to submit feedback. Please try again."}</p>
        <button className="btn-primary dialog-btn" onClick={onClose}>
          OK
        </button>
      </div>
    </div>
  );
}
