import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaArrowLeft, FaPlusCircle } from "react-icons/fa";
import InviteModal from "../components/InviteModal";
import "../styles/CreateDecision.css";

const STORAGE_KEY = "decisionhub-decisions";
const USER_KEY = "decisionhub-current-user";

function getCurrentUser() {
  return localStorage.getItem(USER_KEY) || "Mythili";
}

function CreateDecision() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "",
    visibility: "Public",
    deadline: "",
    board: "",
    options: ["", ""],
  });
  const [showInvite, setShowInvite] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleOptionChange = (index, value) => {
    const updatedOptions = [...form.options];
    updatedOptions[index] = value;
    setForm((prev) => ({ ...prev, options: updatedOptions }));
  };

  const addOption = () => {
    setForm((prev) => ({ ...prev, options: [...prev.options, ""] }));
  };

  const removeOption = (index) => {
    if (form.options.length > 2) {
      const updatedOptions = form.options.filter((_, i) => i !== index);
      setForm((prev) => ({ ...prev, options: updatedOptions }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const filledOptions = form.options.map((option) => option.trim()).filter(Boolean);

    if (filledOptions.length < 2) {
      alert("Please add at least two decision options.");
      return;
    }

    const existingDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    const newDecision = {
      id: Date.now(),
      title: form.title.trim(),
      description: form.description.trim(),
      category: form.category,
      visibility: form.visibility,
      deadline: form.deadline,
      board: form.board,
      status: "Active",
      votes: 0,
      creator: getCurrentUser(),
      userVoteOptionId: null,
      invites: [],
      options: filledOptions.map((name, index) => ({
        id: index + 1,
        name,
        votes: 0,
      })),
      comments: [],
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify([...existingDecisions, newDecision]));
    alert("Decision Created Successfully!");
    navigate("/dashboard");
  };

  return (
    <div className="create-container">
      <div className="create-card">
        <h1>DecisionHub</h1>
        <h2>Create New Decision</h2>
        <p>Create a new decision and invite your community to vote.</p>

        <form onSubmit={handleSubmit}>
          <label>Decision Title</label>
          <input
            type="text"
            name="title"
            placeholder="Enter Decision Title"
            value={form.title}
            onChange={handleChange}
            required
          />

          <label>Description</label>
          <textarea
            rows="5"
            name="description"
            placeholder="Describe your decision..."
            value={form.description}
            onChange={handleChange}
            required
          />

          <label>Category</label>
          <select name="category" value={form.category} onChange={handleChange} required>
            <option value="">Select Category</option>
            <option value="Education">Education</option>
            <option value="Career">Career</option>
            <option value="Technology">Technology</option>
            <option value="Business">Business</option>
            <option value="Travel">Travel</option>
            <option value="Others">Others</option>
          </select>

          <label>Visibility</label>
          <select name="visibility" value={form.visibility} onChange={handleChange}>
            <option value="Public">Public</option>
            <option value="Private">Private</option>
          </select>

          <label>Board / Community</label>
          <input
            type="text"
            name="board"
            placeholder="Board or community name (optional)"
            value={form.board}
            onChange={handleChange}
          />

          <div style={{ marginTop: 8 }}>
            <button type="button" className="add-option-btn" onClick={() => setShowInvite(true)}>
              Invite Members
            </button>
          </div>

          <label>Voting Deadline</label>
          <input type="date" name="deadline" value={form.deadline} onChange={handleChange} required />

          <label>Decision Options</label>
          <div className="options-list">
            {form.options.map((option, index) => (
              <div key={index} className="option-row">
                <input
                  type="text"
                  placeholder={`Option ${index + 1}`}
                  value={option}
                  onChange={(e) => handleOptionChange(index, e.target.value)}
                />
                {form.options.length > 2 && (
                  <button type="button" className="remove-option-btn" onClick={() => removeOption(index)}>
                    Remove
                  </button>
                )}
              </div>
            ))}
          </div>

          <button type="button" className="add-option-btn" onClick={addOption}>
            + Add Option
          </button>

          <div className="button-group">
            <button type="button" className="back-btn" onClick={() => navigate("/dashboard")}>
              <FaArrowLeft /> Back
            </button>

            <button type="submit" className="create-btn">
              <FaPlusCircle /> Create Decision
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreateDecision;
