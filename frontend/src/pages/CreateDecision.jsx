import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { FaArrowLeft, FaPlusCircle, FaTrash, FaEnvelope, FaTimes, FaLock } from "react-icons/fa";
import "../styles/CreateDecision.css";

function CreateDecision() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();
  
  const STORAGE_KEY = "decisionhub-decisions";

  // Check if role is admin on mount
  useEffect(() => {
    if (user && user.role !== "ADMIN") {
      addToast("Access Denied: Only Admins can create decisions.", "error");
      navigate("/dashboard");
    }
  }, [user, navigate, addToast]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [visibility, setVisibility] = useState("Public");
  const [deadline, setDeadline] = useState("");
  
  // Dynamic Options (starts with 2 options)
  const [options, setOptions] = useState([
    { id: 1, text: "" },
    { id: 2, text: "" }
  ]);

  // Private Member Invites
  const [emailInput, setEmailInput] = useState("");
  const [invitedEmails, setInvitedEmails] = useState([]);

  const addOptionField = () => {
    setOptions([...options, { id: Date.now(), text: "" }]);
  };

  const removeOptionField = (id) => {
    if (options.length <= 2) {
      addToast("A decision requires at least two options.", "error");
      return;
    }
    setOptions(options.filter(opt => opt.id !== id));
  };

  const handleOptionChange = (id, val) => {
    setOptions(options.map(opt => opt.id === id ? { ...opt, text: val } : opt));
  };

  // Invited Emails Chip input logic
  const handleAddEmail = (e) => {
    e.preventDefault();
    const email = emailInput.trim();
    if (!email) return;
    
    // Simple Email Regex check
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
    setInvitedEmails(invitedEmails.filter(e => e !== email));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (options.some(opt => !opt.text.trim())) {
      addToast("Please fill in all options.", "error");
      return;
    }

    if (visibility === "Private" && invitedEmails.length === 0) {
      addToast("Please invite at least one member for private decisions.", "error");
      return;
    }

    const existingDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");

    const newDecision = {
      id: Date.now(),
      title,
      description,
      category,
      visibility,
      status: "Active",
      deadline,
      userVoteOptionId: null,
      options: options.map((opt, idx) => ({
        id: idx + 1,
        name: opt.text,
        votes: 0
      })),
      invitedEmails: visibility === "Private" ? invitedEmails : [],
      comments: []
    };

    existingDecisions.push(newDecision);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(existingDecisions));

    // Log Activity
    const existingActivities = JSON.parse(localStorage.getItem("decisionhub-activities") || "[]");
    existingActivities.unshift({
      id: Date.now(),
      icon: "create",
      text: `You created '${title}'`,
      time: "Just now"
    });
    localStorage.setItem("decisionhub-activities", JSON.stringify(existingActivities.slice(0, 10)));

    // Log Notification
    const existingNotifs = JSON.parse(localStorage.getItem("decisionhub-notifications") || "[]");
    existingNotifs.unshift({
      id: Date.now(),
      text: `Admin created a new decision: '${title}'`,
      unread: true,
      time: "Just now"
    });
    localStorage.setItem("decisionhub-notifications", JSON.stringify(existingNotifs));

    addToast("Decision created successfully!", "success");
    navigate("/decisions");
  };

  if (user?.role !== "ADMIN") return null;

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
              <p>Formulate a question, set categories/deadlines, and add voting choices.</p>
            </div>

            <form onSubmit={handleSubmit} className="create-form-panel glass-panel">
              <div className="form-group-grid">
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
                  <label>Category</label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    required
                  >
                    <option value="">Select Category</option>
                    <option>Education</option>
                    <option>Career</option>
                    <option>Technology</option>
                    <option>Business</option>
                    <option>Travel</option>
                    <option>Others</option>
                  </select>
                </div>
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
                    onChange={(e) => setVisibility(e.target.value)}
                  >
                    <option value="Public">Public (Anyone can view & vote)</option>
                    <option value="Private">Private (Invite-only via email)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Voting Deadline</label>
                  <input
                    type="date"
                    value={deadline}
                    onChange={(e) => setDeadline(e.target.value)}
                    required
                  />
                </div>
              </div>

              {/* Dynamic Options Section */}
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

                <button
                  type="button"
                  className="btn-secondary add-opt-btn"
                  onClick={addOptionField}
                >
                  <FaPlusCircle /> Add Choice Option
                </button>
              </div>

              {/* Conditional Private Invites Section */}
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
                <button type="submit" className="btn-primary btn-submit-poll">
                  <FaPlusCircle /> Deploy Decision Poll
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