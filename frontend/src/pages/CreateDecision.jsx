import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaArrowLeft, FaPlusCircle, FaTrash, FaTimes } from "react-icons/fa";
import "../styles/CreateDecision.css";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json" }
});

// TODO: confirm against backend — placeholder list until a /categories endpoint is confirmed
const CATEGORY_OPTIONS = [
  "Technology",
  "Career",
  "Finance",
  "Lifestyle",
  "Travel",
  "Programming",
  "Team Building"
];

// Turns "Food Variety" into a safe object key: "food_variety"
const toKey = (label) => label.trim().toLowerCase().replace(/\s+/g, "_");

function CreateDecision() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  // Target Audience / Community — 2 options only, per current app design
  const [visibility, setVisibility] = useState("PUBLIC"); // PUBLIC | PRIVATE

  const [category, setCategory] = useState(CATEGORY_OPTIONS[0]);

  const [votingType, setVotingType] = useState("SINGLE_CHOICE");
  const [deadline, setDeadline] = useState("");         // Decision deadline (discussion closes)
  const [votingEndTime, setVotingEndTime] = useState(""); // Poll end time (voting closes)

  const [submitting, setSubmitting] = useState(false);

  // Comparison Criteria — tag-based, matches the reference app
  const [criterionInput, setCriterionInput] = useState("");
  const [criteria, setCriteria] = useState([]); // array of strings, e.g. ["Food Variety", "Budget"]

  const addCriterion = () => {
    const val = criterionInput.trim();
    if (!val) return;
    if (criteria.some((c) => c.toLowerCase() === val.toLowerCase())) {
      addToast("That criterion is already added.", "warning");
      return;
    }
    setCriteria([...criteria, val]);
    setCriterionInput("");
  };

  const removeCriterion = (val) => {
    setCriteria(criteria.filter((c) => c !== val));
  };

  // Options — Title, Description, + one Criteria Specification field per criterion tag
  const [options, setOptions] = useState([
    { id: 1, title: "", description: "", criteriaValues: {} },
    { id: 2, title: "", description: "", criteriaValues: {} }
  ]);

  const addOptionField = () => {
    setOptions([
      ...options,
      { id: Date.now(), title: "", description: "", criteriaValues: {} }
    ]);
  };

  const removeOptionField = (id) => {
    if (options.length <= 2) {
      addToast("A decision requires at least two options.", "error");
      return;
    }
    setOptions(options.filter((opt) => opt.id !== id));
  };

  const handleOptionChange = (id, field, val) => {
    setOptions(options.map((opt) => (opt.id === id ? { ...opt, [field]: val } : opt)));
  };

  const handleCriteriaValueChange = (optionId, criterionLabel, val) => {
    setOptions(
      options.map((opt) =>
        opt.id === optionId
          ? {
              ...opt,
              criteriaValues: { ...opt.criteriaValues, [toKey(criterionLabel)]: val }
            }
          : opt
      )
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (options.some((opt) => !opt.title.trim())) {
      addToast("Please fill in all option titles.", "error");
      return;
    }

    if (!deadline || !votingEndTime) {
      addToast("Please set both the Decision Deadline and Poll End Time.", "error");
      return;
    }

    setSubmitting(true);
    try {
      const createRes = await axios.post(
        `${API}/decisions`,
        {
          title,
          // category is sent via `tags` — no direct `category` field confirmed on this endpoint yet.
          description,
          tags: [category],
          votingType,
          isPublic: visibility === "PUBLIC",
          anonymityType: "PUBLIC", // not present in the UI yet — defaulting until confirmed
          deadline: new Date(deadline).toISOString(),
          votingEndTime: new Date(votingEndTime).toISOString(),
          options: options.map((opt) => ({
            title: opt.title,
            description: opt.description,
            criteria: criteria.map((c) => {
              const raw = opt.criteriaValues[toKey(c)] || "";
              const numeric = parseFloat(raw);
              return {
                criterionName: c,
                score: Number.isFinite(numeric) ? numeric : 0,
                remarks: raw
              };
            })
          })),
          factors: criteria.map((c) => ({ name: c, description: "" }))
        },
        headers()
      );

      const decisionId = createRes.data.id;

      // Publish — DRAFT → ACTIVE, auto-creates Poll
      await axios.put(`${API}/decisions/${decisionId}/publish`, {}, headers());

      addToast("Decision created successfully!", "success");
      navigate(`/decisions/${decisionId}`);
    } catch (err) {
      console.error("Failed to create/publish decision:", err.response?.data || err.message);
      addToast("Failed to create decision. Check console for details.", "error");
    } finally {
      setSubmitting(false);
    }
  };

  if (user?.role === "ADMIN") return null;

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="create-decision-page">
            <div className="create-header">
              <button className="btn-secondary back-btn-top" onClick={() => navigate("/dashboard")}>
                <FaArrowLeft /> Back
              </button>
              <h1>Create New Decision</h1>
              <p>Frame your dilemma and invite the network to weigh in.</p>
            </div>

            <form onSubmit={handleSubmit} className="create-form-panel glass-panel">
              {/* Decision Title */}
              <div className="form-group">
                <label>Decision Title</label>
                <input
                  type="text"
                  placeholder="e.g. MBA vs Corporate Job"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              {/* Target Audience / Community */}
              <div className="form-group">
                <label>Target Audience / Community</label>
                <select value={visibility} onChange={(e) => setVisibility(e.target.value)}>
                  <option value="PUBLIC">Public (Open to everyone)</option>
                  <option value="PRIVATE">Private</option>
                </select>
              </div>

              {/* Category */}
              <div className="form-group">
                <label>Category</label>
                <select value={category} onChange={(e) => setCategory(e.target.value)}>
                  {CATEGORY_OPTIONS.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              {/* Description */}
              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows="4"
                  placeholder="Provide context about the decision you need to make..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
              </div>

              {/* Voting Type, Deadline, Poll End Time — per spec doc Steps 3-5 */}
              <div className="form-group-grid">
                <div className="form-group">
                  <label>Voting Type</label>
                  <select value={votingType} onChange={(e) => setVotingType(e.target.value)}>
                    <option value="SINGLE_CHOICE">Single Choice</option>
                    <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                    <option value="RATING_BASED">Rating Based</option>
                  </select>
                </div>
              </div>

              <div className="form-group-grid">
                <div className="form-group">
                  <label>Decision Deadline</label>
                  <input
                    type="datetime-local"
                    value={deadline}
                    onChange={(e) => setDeadline(e.target.value)}
                    required
                  />
                  <p className="section-subtitle">When discussion/comments close.</p>
                </div>

                <div className="form-group">
                  <label>Poll End Time</label>
                  <input
                    type="datetime-local"
                    value={votingEndTime}
                    onChange={(e) => setVotingEndTime(e.target.value)}
                    required
                  />
                  <p className="section-subtitle">When voting closes (may be before the deadline).</p>
                </div>
              </div>

              {/* Comparison Criteria — tag based */}
              <div className="form-section-options">
                <h3>Comparison Criteria</h3>
                <p className="section-subtitle">
                  Define comparison aspects (e.g. Price, Performance, Battery life). This generates
                  matching input fields inside each option card for side-by-side comparison tables.
                </p>

                <div className="email-input-bar">
                  <input
                    type="text"
                    placeholder="e.g. Warranty"
                    value={criterionInput}
                    onChange={(e) => setCriterionInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        addCriterion();
                      }
                    }}
                  />
                  <button type="button" className="btn-secondary" onClick={addCriterion}>
                    <FaPlusCircle /> Add Criterion
                  </button>
                </div>

                <div className="email-chips-container">
                  {criteria.length === 0 ? (
                    <span className="no-invites-msg">No criteria added yet.</span>
                  ) : (
                    criteria.map((c) => (
                      <div key={c} className="email-chip animate-pop-in">
                        <span>{c.toUpperCase()}</span>
                        <button type="button" onClick={() => removeCriterion(c)}>
                          <FaTimes />
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* Options */}
              <div className="form-section-options">
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <h3>Options</h3>
                  <button type="button" className="btn-secondary add-opt-btn" onClick={addOptionField}>
                    <FaPlusCircle /> Add Option
                  </button>
                </div>
                <p className="section-subtitle">You must add at least 2 options.</p>

                <div className="options-input-list">
                  {options.map((option, index) => (
                    <div className="option-row-item animate-pop-in" key={option.id} style={{ flexDirection: "column", alignItems: "stretch" }}>
                      <div style={{ display: "flex", justifyContent: "space-between" }}>
                        <span className="option-index-lbl">Option {index + 1} Title</span>
                        <button
                          type="button"
                          className="btn-delete-option"
                          onClick={() => removeOptionField(option.id)}
                          title="Remove Option"
                        >
                          <FaTrash />
                        </button>
                      </div>
                      <input
                        type="text"
                        placeholder="e.g. Option A"
                        value={option.title}
                        onChange={(e) => handleOptionChange(option.id, "title", e.target.value)}
                        required
                      />

                      <label>Description</label>
                      <textarea
                        rows="2"
                        placeholder="Brief details about this option..."
                        value={option.description}
                        onChange={(e) => handleOptionChange(option.id, "description", e.target.value)}
                      />

                      {criteria.length > 0 && (
                        <div className="form-section-options" style={{ marginTop: "0.5rem" }}>
                          <h4 style={{ color: "#22d3ee" }}>Criteria Specifications</h4>
                          <div className="form-group-grid">
                            {criteria.map((c) => (
                              <div className="form-group" key={c}>
                                <label>{c.toLowerCase()}</label>
                                <input
                                  type="text"
                                  placeholder={`Value for ${c.toLowerCase()}`}
                                  value={option.criteriaValues[toKey(c)] || ""}
                                  onChange={(e) =>
                                    handleCriteriaValueChange(option.id, c, e.target.value)
                                  }
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              <div className="create-submit-sec">
                <button type="submit" className="btn-primary btn-submit-poll" disabled={submitting}>
                  <FaPlusCircle /> {submitting ? "Deploying..." : "Deploy Decision Poll"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CreateDecision;
