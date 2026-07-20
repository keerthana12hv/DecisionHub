import { FaExclamationTriangle } from "react-icons/fa";
import "../styles/DeleteModal.css";

function DeleteModal({ decisionTitle, onCancel, onDelete }) {
  return (
    <div className="delete-overlay">
      <div className="delete-modal glass-panel animate-pop-in">
        <div className="delete-warning-icon">
          <FaExclamationTriangle />
        </div>
        <h2>Delete Decision</h2>
        <p>
          Are you sure you want to permanently delete <strong>"{decisionTitle}"</strong>? This action cannot be undone and all cast votes will be lost.
        </p>

        <div className="delete-buttons">
          <button className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
          <button className="btn-primary confirm-delete-btn" onClick={onDelete}>
            Yes, Delete Poll
          </button>
        </div>
      </div>
    </div>
  );
}

export default DeleteModal;