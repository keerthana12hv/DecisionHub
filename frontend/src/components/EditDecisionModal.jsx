import { useState } from "react";
import { FaSave, FaTimes } from "react-icons/fa";
import "../styles/EditDecisionModal.css";

function EditDecisionModal({ decision, onClose, onSave }) {
  const [title, setTitle] = useState(decision.title);
  const [description, setDescription] = useState(decision.description || "");
  const [category, setCategory] = useState(decision.category || "");
  const [deadline, setDeadline] = useState(decision.deadline || "");

  const handleSave = () => {
    if (!title.trim() || !category.trim()) return;
    onSave({
      ...decision,
      title,
      description,
      category,
      deadline
    });
  };

  return (
    <div className="edit-overlay">
      <div className="edit-modal glass-panel animate-pop-in">
        <div className="modal-header">
          <h2>Edit Decision</h2>
          <button className="close-x-btn" onClick={onClose}><FaTimes /></button>
        </div>

        <div className="modal-body">
          <div className="form-group">
            <label>Decision Title</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-group-row">
            <div className="form-group">
              <label>Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)} required>
                <option value="">Select Category</option>
                <option>Education</option>
                <option>Career</option>
                <option>Technology</option>
                <option>Business</option>
                <option>Travel</option>
                <option>Others</option>
              </select>
            </div>
            <div className="form-group">
              <label>Deadline</label>
              <input
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              rows="4"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
        </div>

        <div className="edit-buttons">
          <button className="btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button className="btn-primary" onClick={handleSave}>
            <FaSave /> Save Changes
          </button>
        </div>
      </div>
    </div>
  );
}

export default EditDecisionModal;