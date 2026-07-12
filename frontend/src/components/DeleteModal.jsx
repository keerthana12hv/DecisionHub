import "../styles/DeleteModal.css";

function DeleteModal({ decisionTitle, onCancel, onDelete }) {
  return (
    <div className="delete-overlay">

      <div className="delete-modal">

        <h2>Delete Decision</h2>

        <p>
          Are you sure you want to delete
          <strong> "{decisionTitle}" </strong>?
        </p>

        <div className="delete-buttons">

          <button
            className="cancel-btn"
            onClick={onCancel}
          >
            Cancel
          </button>

          <button
            className="confirm-delete-btn"
            onClick={onDelete}
          >
            Delete
          </button>

        </div>

      </div>

    </div>
  );
}

export default DeleteModal;