import { useState } from "react";
import "../styles/EditDecisionModal.css";

function EditDecisionModal({
  decision,
  onClose,
  onSave
}) {

  const [title, setTitle] = useState(decision.title);
  const [description, setDescription] = useState(
    decision.description || ""
  );

  return (

    <div className="edit-overlay">

      <div className="edit-modal">

        <h2>Edit Decision</h2>

        <label>Decision Title</label>

        <input
          value={title}
          onChange={(e)=>setTitle(e.target.value)}
        />

        <label>Description</label>

        <textarea
          rows="5"
          value={description}
          onChange={(e)=>setDescription(e.target.value)}
        />

        <div className="edit-buttons">

          <button
            className="cancel-btn"
            onClick={onClose}
          >
            Cancel
          </button>

          <button
            className="save-btn"
            onClick={()=>
              onSave({
                title,
                description
              })
            }
          >
            Save
          </button>

        </div>

      </div>

    </div>

  );

}

export default EditDecisionModal;