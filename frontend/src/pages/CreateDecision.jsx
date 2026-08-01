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
<<<<<<< Updated upstream
=======
  }, [user, navigate, addToast]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [visibility, setVisibility] = useState("Public");
  const [votingEndTime, setVotingEndTime] = useState("");
  const [votingType, setVotingType] = useState("SINGLE_CHOICE");
  const [submitting, setSubmitting] = useState(false);

  const [options, setOptions] = useState([
    { id: 1, text: "" },
    { id: 2, text: "" }
  ]);

  const [factors, setFactors] = useState([{ id: 1, text: "" }]);

  const [emailInput, setEmailInput] = useState("");
  const [invitedEmails, setInvitedEmails] = useState([]);

  const addOptionField = () => {
    setOptions([...options, { id: Date.now(), text: "" }]);
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    localStorage.setItem(STORAGE_KEY, JSON.stringify([...existingDecisions, newDecision]));
    alert("Decision Created Successfully!");
    navigate("/dashboard");
=======
    if (visibility === "Private" && invitedEmails.length === 0) {
      addToast("Please invite at least one member for private decisions.", "error");
      return;
    }

    if (!votingEndTime) {
      addToast("Please set a voting end time.", "error");
      return;
    }

    const votingEndDate    = new Date(votingEndTime);
    const votingEndTimeISO = votingEndDate.toISOString();
    // Deadline is automatically 2 hours after voting closes — backend requires deadline > votingEndTime
    const deadlineISO = new Date(votingEndDate.getTime() + 2 * 60 * 60 * 1000).toISOString();

    setSubmitting(true);
    try {
      // Step 1: Create as DRAFT
      const createRes = await axios.post(
        `${API}/decisions`,
        {
          title,
          description,
          votingType,
          votingEndTime: votingEndTimeISO,
          deadline: deadlineISO,
          options: options.map((opt) => ({ title: opt.text })),
          comparisonFactors:
            votingType === "RATING_BASED"
              ? factors.map((f) => ({ name: f.text }))
              : []
        },
        headers()
      );

      const decisionId = createRes.data.id;

      // Step 2: Publish — DRAFT → ACTIVE, auto-creates Poll
      await axios.post(`${API}/decisions/${decisionId}/publish`, {}, headers());

      addToast("Decision created successfully!", "success");
      navigate(`/decision/${decisionId}`);
    } catch (err) {
      console.error("Failed to create/publish decision:", err.response?.data || err.message);
      addToast("Failed to create decision. Check console for details.", "error");
    } finally {
      setSubmitting(false);
    }
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
              <div className="form-group-grid">
                <div className="form-group">
                  <label>Visibility</label>
                  <select value={visibility} onChange={(e) => setVisibility(e.target.value)}>
                    <option value="Public">Public (Anyone can view &amp; vote)</option>
                    <option value="Private">Private (Invite-only via email)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Voting Type</label>
                  <select value={votingType} onChange={(e) => setVotingType(e.target.value)}>
                    <option value="SINGLE_CHOICE">Single Choice</option>
                    <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                    <option value="RATING_BASED">Rating Based</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label>Poll Closes At</label>
                <input
                  type="datetime-local"
                  value={votingEndTime}
                  onChange={(e) => setVotingEndTime(e.target.value)}
                  required
                />
                <span className="field-hint">
                  Voting closes at this time. The decision deadline is automatically set 2 hours after.
                </span>
              </div>

              <div className="form-section-options">
                <h3>Voting Options</h3>
                <p className="section-subtitle">Provide choices for participants. You must add at least 2 options.</p>

                <div className="options-input-list">
                  {options.map((option, index) => (
                    <div className="option-row-item animate-pop-in" key={option.id}>
                      <span className="option-index-lbl">Option {index + 1}</span>
                      <input
                        type="text"
                        placeholder={`Enter Option ${index + 1} text`}
                        value={option.text}
                        onChange={(e) => handleOptionChange(option.id, e.target.value)}
                        required
                      />
                      <button
                        type="button"
                        className="btn-delete-option"
                        onClick={() => removeOptionField(option.id)}
                        title="Remove Option"
                      >
                        <FaTrash />
                      </button>
                    </div>
                  ))}
                </div>

                <button type="button" className="btn-secondary add-opt-btn" onClick={addOptionField}>
                  <FaPlusCircle /> Add Choice Option
                </button>
              </div>

              {votingType === "RATING_BASED" && (
                <div className="form-section-options animate-fade-in">
                  <h3>Comparison Factors</h3>
                  <p className="section-subtitle">
                    Criteria voters will use to rate each option (e.g. Price, Quality).
                  </p>

                  <div className="options-input-list">
                    {factors.map((factor, index) => (
                      <div className="option-row-item animate-pop-in" key={factor.id}>
                        <span className="option-index-lbl">Factor {index + 1}</span>
                        <input
                          type="text"
                          placeholder={`Enter Factor ${index + 1} name`}
                          value={factor.text}
                          onChange={(e) => handleFactorChange(factor.id, e.target.value)}
                          required
                        />
                        <button
                          type="button"
                          className="btn-delete-option"
                          onClick={() => removeFactorField(factor.id)}
                          title="Remove Factor"
                        >
                          <FaTrash />
                        </button>
                      </div>
                    ))}
                  </div>

                  <button type="button" className="btn-secondary add-opt-btn" onClick={addFactorField}>
                    <FaPlusCircle /> Add Comparison Factor
                  </button>
                </div>
              )}

              {visibility === "Private" && (
                <div className="form-section-invites animate-fade-in">
                  <div className="invites-title-sec">
                    <FaLock />
                    <h3>Invite Authorized Members</h3>
                  </div>
                  <p className="section-subtitle">Only added email addresses will be authorized to access this decision.</p>

                  <div className="email-input-bar">
                    <input
                      type="email"
                      placeholder="voter@domain.com"
                      value={emailInput}
                      onChange={(e) => setEmailInput(e.target.value)}
                    />
                    <button type="button" className="btn-primary" onClick={handleAddEmail}>
                      <FaPlusCircle /> Add
                    </button>
                  </div>

                  <div className="email-chips-container">
                    {invitedEmails.length === 0 ? (
                      <span className="no-invites-msg">
                        <FaEnvelope /> No members invited yet. Add emails above.
                      </span>
                    ) : (
                      invitedEmails.map((email) => (
                        <div key={email} className="email-chip animate-pop-in">
                          <span>{email}</span>
                          <button type="button" onClick={() => removeEmailChip(email)}>
                            <FaTimes />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}

              <div className="create-submit-sec">
                <button type="submit" className="btn-primary btn-submit-poll" disabled={submitting}>
                  <FaPlusCircle /> {submitting ? "Deploying..." : "Deploy Decision Poll"}
                </button>
              </div>
            </form>
>>>>>>> Stashed changes
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
