import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { getCommunities } from "../services/communityService";
import { FaArrowLeft, FaPlusCircle, FaTrash, FaEnvelope, FaTimes, FaLock } from "react-icons/fa";
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

  // Any authenticated user can create a decision.
  // Admins should not create regular decision boards (per spec), so we block that role instead.
  useEffect(() => {
    if (user && user.role === "ADMIN") {
      addToast("Admins do not create regular decision boards.", "error");
      navigate("/dashboard");
    }
  }, [user, navigate, addToast]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [visibility, setVisibility] = useState("PUBLIC"); // PUBLIC | PRIVATE | COMMUNITY
  const [deadline, setDeadline] = useState("");         // Decision deadline (discussion closes)
  const [votingEndTime, setVotingEndTime] = useState(""); // Poll end time (voting closes) — matches backend field name
  const [votingType, setVotingType] = useState("SINGLE_CHOICE");
  const [submitting, setSubmitting] = useState(false);

  // Community visibility support
  const [myCommunities, setMyCommunities] = useState([]);
  const [communityId, setCommunityId] = useState("");
  const [communitiesLoading, setCommunitiesLoading] = useState(false);

  const [options, setOptions] = useState([
    { id: 1, title: "", description: "" },
    { id: 2, title: "", description: "" }
  ]);

  const [factors, setFactors] = useState([{ id: 1, name: "", description: "" }]);

  const [emailInput, setEmailInput] = useState("");
  const [invitedEmails, setInvitedEmails] = useState([]);

  // Load communities the user is actually a member of, only when Community visibility is chosen
  useEffect(() => {
    if (visibility !== "COMMUNITY") return;
    let cancelled = false;

    const loadMyCommunities = async () => {
      setCommunitiesLoading(true);
      try {
        const data = await getCommunities();
        if (!cancelled) {
          setMyCommunities(data.filter((c) => c.isMember));
        }
      } catch (err) {
        console.error("Failed to load communities:", err);
        addToast("Failed to load your communities.", "error");
      } finally {
        if (!cancelled) setCommunitiesLoading(false);
      }
    };

    loadMyCommunities();
    return () => {
      cancelled = true;
    };
  }, [visibility, addToast]);

  // Category is derived from the selected community — never asked directly (per workflow spec)
  const selectedCommunity = myCommunities.find((c) => String(c.id) === String(communityId));
  const derivedCategoryName = selectedCommunity?.categoryName || "";

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

  const addFactorField = () => {
    setFactors([...factors, { id: Date.now(), name: "", description: "" }]);
  };

  const removeFactorField = (id) => {
    if (factors.length <= 1) {
      addToast("At least one comparison factor is required.", "error");
      return;
    }
    setFactors(factors.filter((f) => f.id !== id));
  };

  const handleFactorChange = (id, field, val) => {
    setFactors(factors.map((f) => (f.id === id ? { ...f, [field]: val } : f)));
  };

  const handleAddEmail = (e) => {
    e.preventDefault();
    const email = emailInput.trim();
    if (!email) return;

    const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    if (!isEmail) {
      addToast("Invalid email address.", "error");
      return;
    }
    if (invitedEmails.includes(email)) {
      addToast("Email already added.", "warning");
      return;
    }
    setInvitedEmails([...invitedEmails, email]);
    setEmailInput("");
  };

  const removeEmailChip = (email) => {
    setInvitedEmails(invitedEmails.filter((e) => e !== email));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (options.some((opt) => !opt.title.trim())) {
      addToast("Please fill in all option titles.", "error");
      return;
    }

    if (votingType === "RATING_BASED" && factors.some((f) => !f.name.trim())) {
      addToast("Please fill in all comparison factor names.", "error");
      return;
    }

    if (visibility === "PRIVATE" && invitedEmails.length === 0) {
      addToast("Please invite at least one member for private decisions.", "error");
      return;
    }
    // NOTE: invitedEmails currently has no confirmed backend field to send to —
    // Swagger's POST /api/decisions schema has no invite mechanism yet. Blocked
    // pending backend's answer; not included in the submit payload below.

    if (visibility === "COMMUNITY" && !communityId) {
      addToast("Please select a community.", "error");
      return;
    }

    setSubmitting(true);
    try {
      // Step 1: Create as DRAFT
      const createRes = await axios.post(
        `${API}/decisions`,
        {
          title,
          description,
          votingType,
          // Schema uses isPublic (boolean) + communityId, not a visibility string.
          // PRIVATE (invite-only) has no backend field yet — sent as non-public,
          // non-community until backend confirms how invites are handled.
          isPublic: visibility === "PUBLIC",
          communityId: visibility === "COMMUNITY" ? communityId : null,
          deadline: deadline ? new Date(deadline).toISOString() : null,
          votingEndTime: votingEndTime ? new Date(votingEndTime).toISOString() : null,
          options: options.map((opt) => ({
            title: opt.title,
            description: opt.description
          })),
          factors:
            votingType === "RATING_BASED"
              ? factors.map((f) => ({ name: f.name, description: f.description }))
              : []
        },
        headers()
      );

      const decisionId = createRes.data.id;

      // Step 2: Publish — DRAFT → ACTIVE, auto-creates Poll
      await axios.put(`${API}/decisions/${decisionId}/publish`, {}, headers());

      addToast("Decision created successfully!", "success");
      navigate(`/decision/${decisionId}`);
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
              <h1>Create Decision Poll</h1>
              <p>Formulate a question, set visibility/deadlines, and add voting choices.</p>
            </div>

            <form onSubmit={handleSubmit} className="create-form-panel glass-panel">
              <div className="form-group">
                <label>Decision Title</label>
                <input
                  type="text"
                  placeholder="e.g. Choose our cloud provider"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Description & Scope</label>
                <textarea
                  rows="4"
                  placeholder="Provide context, constraints, and background details for voters..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
              </div>

              <div className="form-group-grid">
                <div className="form-group">
                  <label>Visibility</label>
                  <select
                    value={visibility}
                    onChange={(e) => {
                      setVisibility(e.target.value);
                      setCommunityId("");
                    }}
                  >
                    <option value="PUBLIC">Public (Anyone can view & vote)</option>
                    <option value="PRIVATE">Private (Invite-only via email)</option>
                    <option value="COMMUNITY">Community (Members only)</option>
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

              {visibility === "COMMUNITY" && (
                <div className="form-group animate-fade-in">
                  <label>Select Community</label>
                  <select
                    value={communityId}
                    onChange={(e) => setCommunityId(e.target.value)}
                    required
                  >
                    <option value="">
                      {communitiesLoading ? "Loading your communities..." : "Select Community"}
                    </option>
                    {myCommunities.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  {!communitiesLoading && myCommunities.length === 0 && (
                    <p className="section-subtitle">
                      You haven't joined any communities yet.
                    </p>
                  )}
                  {derivedCategoryName && (
                    <p className="section-subtitle">Category: {derivedCategoryName}</p>
                  )}
                </div>
              )}

              <div className="form-group-grid">
                <div className="form-group">
                  <label>Decision Deadline</label>
                  <input
                    type="date"
                    value={deadline}
                    onChange={(e) => setDeadline(e.target.value)}
                    required
                  />
                  <p className="section-subtitle">When discussion/comments close.</p>
                </div>

                <div className="form-group">
                  <label>Poll End Time</label>
                  <input
                    type="date"
                    value={votingEndTime}
                    onChange={(e) => setVotingEndTime(e.target.value)}
                    required
                  />
                  <p className="section-subtitle">When voting closes (may be before the deadline).</p>
                </div>
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
                        placeholder={`Enter Option ${index + 1} title`}
                        value={option.title}
                        onChange={(e) => handleOptionChange(option.id, "title", e.target.value)}
                        required
                      />
                      <input
                        type="text"
                        placeholder="Description (optional)"
                        value={option.description}
                        onChange={(e) => handleOptionChange(option.id, "description", e.target.value)}
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
                          value={factor.name}
                          onChange={(e) => handleFactorChange(factor.id, "name", e.target.value)}
                          required
                        />
                        <input
                          type="text"
                          placeholder="Description (optional)"
                          value={factor.description}
                          onChange={(e) => handleFactorChange(factor.id, "description", e.target.value)}
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

              {visibility === "PRIVATE" && (
                <div className="form-section-invites animate-fade-in">
                  <div className="invites-title-sec">
                    <FaLock />
                    <h3>Invite Authorized Members</h3>
                  </div>
                  <p className="section-subtitle">Only added email addresses will be authorized to access this decision.</p>
                  <p className="section-subtitle" style={{ color: "#b45309" }}>
                    ⚠ Pending backend confirmation — invites are not yet sent with the decision.
                  </p>

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
          </div>
        </div>
      </div>
    </div>
  );
}

export default CreateDecision;