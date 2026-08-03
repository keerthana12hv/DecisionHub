import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { getMyCommunities } from "../services/communityService";
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

function CreateDecision() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  // Target Audience / Community — 2 options only, per current app design
  const [visibility, setVisibility] = useState("PUBLIC"); // PUBLIC | PRIVATE

  // Communities the user belongs to — only needed/fetched when Private is
  // selected, since a Private decision must be scoped to one of them.
  const [myCommunities, setMyCommunities] = useState([]);
  const [communitiesLoading, setCommunitiesLoading] = useState(false);
  const [selectedCommunityId, setSelectedCommunityId] = useState("");

  useEffect(() => {
    if (visibility === "PRIVATE" && myCommunities.length === 0) {
      fetchMyCommunities();
    }
  }, [visibility]);

  const fetchMyCommunities = async () => {
    try {
      setCommunitiesLoading(true);
      const data = await getMyCommunities();
      setMyCommunities(data || []);
      if (data && data.length > 0) {
        setSelectedCommunityId(String(data[0].id));
      }
    } catch (err) {
      console.error("Failed to load your communities:", err);
      addToast("Could not load your communities.", "error");
    } finally {
      setCommunitiesLoading(false);
    }
  };

  // Categories now come from the backend (GET /api/categories) instead of a
  // hardcoded list, so this always matches whatever categories actually exist.
  const [categoryOptions, setCategoryOptions] = useState([]);
  const [category, setCategory] = useState("");
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setCategoriesLoading(true);
      const res = await axios.get(`${API}/categories`, headers());
      const list = res.data || [];
      setCategoryOptions(list);
      if (list.length > 0) {
        setCategory(list[0].name ?? list[0].id ?? "");
      }
    } catch (err) {
      console.error("Failed to load categories:", err);
      addToast("Could not load categories from the server.", "error");
    } finally {
      setCategoriesLoading(false);
    }
  };

  const [votingType, setVotingType] = useState("SINGLE_CHOICE");
  const [deadline, setDeadline] = useState("");         // Decision deadline (discussion closes)
  const [votingEndTime, setVotingEndTime] = useState(""); // Poll end time (voting closes)

  const [submitting, setSubmitting] = useState(false);

  // Comparison Criteria — tag-based, matches the reference app.
  // The creator ONLY names criteria here (e.g. "Speed", "Cost") — they never
  // assign scores/values. Scoring belongs exclusively to voters, during voting.
  const [criterionInput, setCriterionInput] = useState("");
  const [criteria, setCriteria] = useState([]); // array of strings, e.g. ["Speed", "Cost"]

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

  // Options — just Title + Description now. No per-option criteria values;
  // the creator defines WHAT is being compared (the criteria names above),
  // not the actual scores — those are entered by voters on the voting screen.
  const [options, setOptions] = useState([
    { id: 1, title: "", description: "" },
    { id: 2, title: "", description: "" }
  ]);

  const addOptionField = () => {
    setOptions([...options, { id: Date.now(), title: "", description: "" }]);
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

    if (visibility === "PRIVATE" && !selectedCommunityId) {
      addToast("Please choose a community for this private decision.", "error");
      return;
    }

    // Date validations, per spec: neither date can be before today, and Poll
    // End Time cannot be after the Decision Deadline.
    const now = new Date();
    const deadlineDate = new Date(deadline);
    const votingEndDate = new Date(votingEndTime);

    if (deadlineDate < now) {
      addToast("Decision Deadline cannot be before today.", "error");
      return;
    }
    if (votingEndDate < now) {
      addToast("Poll End Time cannot be before today.", "error");
      return;
    }
    if (votingEndDate > deadlineDate) {
      addToast("Poll End Time cannot be after the Decision Deadline.", "error");
      return;
    }

    setSubmitting(true);
    try {
      const createRes = await axios.post(
        `${API}/decisions`,
        {
          title,
          description: category ? `[Cat:${category}] ${description}` : description,
          tags: [category],
          votingType,
          isPublic: visibility === "PUBLIC",
          // NOTE: "communityId" field name is a best guess — the DecisionRequest
          // schema in Swagger wasn't checked for the exact field name. If the
          // backend rejects this or ignores it, confirm the real field name
          // there (Schemas → DecisionRequest) and rename this key to match.
          ...(visibility === "PRIVATE" && selectedCommunityId
            ? { communityId: Number(selectedCommunityId) }
            : {}),
          anonymityType: "PUBLIC", // not present in the UI yet — defaulting until confirmed
          deadline: deadlineDate.toISOString(),
          votingEndTime: votingEndDate.toISOString(),
          options: options.map((opt) => ({
            title: opt.title,
            description: opt.description
          })),
          // Backend rejects comparison factors for SINGLE_CHOICE / MULTIPLE_CHOICE
          // ("Comparison factors are not allowed for SINGLE_CHOICE decisions") —
          // only send them when this is actually a Rating Based decision.
          factors: votingType !== "RATING_BASED" ? [] : criteria.map((c) => ({ name: c, description: "" }))
        },
        headers()
      );

      const decisionId = createRes.data.id;

      // Per the Draft -> Edit -> Publish workflow, the decision stays DRAFT
      // here — it is NOT auto-published. The creator reviews/edits it on its
      // detail page and explicitly clicks Publish (in Edit Board) when ready.
      addToast("Decision saved as a draft. Review it, then publish when ready.", "success");
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

              {/* Choose Community — required when Private is selected.
                  Otherwise private decisions have no meaning (per spec doc). */}
              {visibility === "PRIVATE" && (
                <div className="form-group">
                  <label>Choose Community</label>
                  <select
                    value={selectedCommunityId}
                    onChange={(e) => setSelectedCommunityId(e.target.value)}
                    disabled={communitiesLoading}
                    required
                  >
                    {communitiesLoading ? (
                      <option value="">Loading your communities...</option>
                    ) : myCommunities.length === 0 ? (
                      <option value="">You are not a member of any community yet</option>
                    ) : (
                      <>
                        <option value="">Select a community...</option>
                        {myCommunities.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name}
                          </option>
                        ))}
                      </>
                    )}
                  </select>
                  {!communitiesLoading && myCommunities.length === 0 && (
                    <p className="section-subtitle">
                      Join or create a community first to make a private decision inside it.
                    </p>
                  )}
                </div>
              )}

              {/* Category — now loaded from the backend */}
              <div className="form-group">
                <label>Category</label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  disabled={categoriesLoading || categoryOptions.length === 0}
                >
                  {categoriesLoading ? (
                    <option>Loading categories...</option>
                  ) : categoryOptions.length === 0 ? (
                    <option>No categories available</option>
                  ) : (
                    categoryOptions.map((c) => (
                      <option key={c.id ?? c.name} value={c.name ?? c.id}>
                        {c.name ?? c.id}
                      </option>
                    ))
                  )}
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
                    min={new Date().toISOString().slice(0, 16)}
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
                    min={new Date().toISOString().slice(0, 16)}
                    max={deadline || undefined}
                    onChange={(e) => setVotingEndTime(e.target.value)}
                    required
                  />
                  <p className="section-subtitle">When voting closes (may be before the deadline).</p>
                </div>
              </div>

              {/* Comparison Criteria — tag based. Only relevant for RATING_BASED:
                  the backend rejects factors for SINGLE_CHOICE and MULTIPLE_CHOICE
                  decisions, so this section is hidden for those voting types.
                  The creator only names criteria here — actual scoring happens
                  on the voting screen, submitted by each voter. */}
              {votingType === "RATING_BASED" && (
                <div className="form-section-options">
                  <h3>Comparison Criteria</h3>
                  <p className="section-subtitle">
                    Define what voters will rate each option on (e.g. Speed, Cost, Scalability).
                    Voters will assign a 1–100 score for each criterion during voting.
                  </p>

                  <div className="email-input-bar">
                    <input
                      type="text"
                      placeholder="e.g. Scalability"
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
              )}

              {/* Options — just title & description. Scoring/criteria values
                  are no longer collected here; they belong to voters. */}
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