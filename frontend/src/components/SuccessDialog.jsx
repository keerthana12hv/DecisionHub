import { FaCheck } from "react-icons/fa";

export default function SuccessDialog({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="feedback-overlay">
      <div className="feedback-dialog glass-panel animate-pop-in">
        <div className="dialog-icon success">
          <FaCheck />
        </div>
        <h2>Feedback Submitted Successfully</h2>
        <p>Thank you for helping us improve Decision Hub.</p>
        <button className="btn-primary dialog-btn" onClick={onClose}>
          OK
        </button>
      </div>
    </div>
  );
}
