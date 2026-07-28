import { useState } from "react";
import axios from "axios";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json" }
});

export default function EditBoardPanel({ decision, onSaved, onCancel }) {
  // Per spec: Draft = everything editable. Published (ACTIVE) or Closed =
  // only Poll End Time can change, everything else is read-only.
  const isDraft = decision.status === "DRAFT";

  const [title, setTitle] = useState(decision.title || "");
  const [description, setDescription] = useState(decision.description || "");
  const [criteria, setCriteria] = useState((decision.factors || []).map((f) => f.name));
  const [criterionInput, setCriterionInput] = useState("");
  const [saving, setSaving] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [error, setError] = useState(null);

  // votingEndTime uses the raw value from GET as its default so an unedited
  // save doesn't accidentally reformat/change it (backend rejects reformatted-
  // but-identical values as "modified").
  const [votingEndTime, setVotingEndTime] = useState(
    decision.votingEndTime ? decision.votingEndTime.slice(0, 16) : ""
  );

  const [options, setOptions] = useState(
    (decision.options || []).map((opt) => ({
      id: opt.id,
      title: opt.title || "",
      description: opt.description || ""
    }))
  );

  const addCriterion = () => {
    const val = criterionInput.trim();
    if (!val) return;
    if (criteria.some((c) => c.toLowerCase() === val.toLowerCase())) return;
    setCriteria([...criteria, val]);
    setCriterionInput("");
  };

  const removeCriterion = (val) => {
    setCriteria(criteria.filter((c) => c !== val));
  };

  const handleOptionChange = (id, field, val) => {
    setOptions(options.map((opt) => (opt.id === id ? { ...opt, [field]: val } : opt)));
  };

  const handlePublish = async () => {
    setPublishing(true);
    setError(null);
    try {
      // Save any edits made while still in Draft before publishing, so
      // nothing typed gets lost if the creator forgot to hit "Save Changes" first.
      const payload = {
        title,
        description,
        tags: decision.categoryName ? [decision.categoryName] : [],
        votingType: decision.votingType,
        isPublic: decision.isPublic ?? true,
        anonymityType: decision.anonymityType || "PUBLIC",
        deadline: decision.deadline,
        votingEndTime: new Date(votingEndTime).toISOString(),
        options: options.map((opt) => ({
          title: opt.title,
          description: opt.description
        })),
        factors:
          decision.votingType !== "RATING_BASED"
            ? []
            : criteria.map((c) => ({ name: c, description: "" }))
      };
      await axios.put(`${API}/decisions/${decision.id}`, payload, headers());

      // Draft -> Active, auto-creates the Poll.
      const res = await axios.put(`${API}/decisions/${decision.id}/publish`, {}, headers());
      onSaved(res.data);
    } catch (err) {
      console.error("Failed to publish decision:", err.response?.data || err.message);
      setError(
        err.response?.data?.error || "Could not publish this decision. Check console for details."
      );
    } finally {
      setPublishing(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      let updatedDecision;

      if (isDraft) {
        // Draft: everything is editable, save the full board via the normal
        // update endpoint. Criteria here only NAMES what will be rated later —
        // no scores are collected from the creator, matching Create Decision.
        const payload = {
          title,
          description,
          tags: decision.categoryName ? [decision.categoryName] : [],
          votingType: decision.votingType,
          isPublic: decision.isPublic ?? true,
          anonymityType: decision.anonymityType || "PUBLIC",
          deadline: decision.deadline,
          votingEndTime: new Date(votingEndTime).toISOString(),
          options: options.map((opt) => ({
            title: opt.title,
            description: opt.description
          })),
          factors:
            decision.votingType !== "RATING_BASED"
              ? []
              : criteria.map((c) => ({ name: c, description: "" }))
        };
        const res = await axios.put(`${API}/decisions/${decision.id}`, payload, headers());
        updatedDecision = res.data;
      } else {
        // Published/Closed: ONLY Poll End Time can change. Use the dedicated
        // extend-end-time endpoint rather than the general update endpoint.
        // NOTE: request field name is a best guess ("votingEndTime") — not yet
        // confirmed against the UpdatePollEndTimeRequest schema in Swagger.
        // If the backend rejects this, check Schemas -> UpdatePollEndTimeRequest
        // for the real field name.
        const res = await axios.patch(
          `${API}/decisions/${decision.id}/poll/end-time`,
          { votingEndTime: new Date(votingEndTime).toISOString() },
          headers()
        );
        updatedDecision = res.data;
      }

      onSaved(updatedDecision);
    } catch (err) {
      console.error("Failed to save decision:", err.response?.data || err.message);
      setError(
        err.response?.data?.error ||
          "Could not save changes. Check console for details."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="edit-board-panel">
      <h3>Edit Decision Board &amp; Options</h3>
      {isDraft ? (
        <p className="section-subtitle">
          This decision is still a Draft — everything below can be edited before you publish.
        </p>
      ) : (
        <p className="section-subtitle">
          This decision has been published. Only the Poll End Time can still be changed.
        </p>
      )}

      {error && <div className="edit-board-error">{error}</div>}

      <h4>Board Parameters</h4>
      <div className="form-group">
        <label>Decision Title</label>
        <input
          type="text"
          value={title}
          disabled={!isDraft}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>

      <div className="form-group">
        <label>Description</label>
        <textarea
          rows="3"
          value={description}
          disabled={!isDraft}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <div className="form-group">
        <label>Poll End Time</label>
        <input
          type="datetime-local"
          value={votingEndTime}
          min={new Date().toISOString().slice(0, 16)}
          max={decision.deadline ? decision.deadline.slice(0, 16) : undefined}
          onChange={(e) => setVotingEndTime(e.target.value)}
        />
        <p className="section-subtitle">
          When voting closes. This is always editable, even after publishing.
        </p>
      </div>

      {decision.votingType === "RATING_BASED" && (
        <>
          <h4>Comparison Criteria</h4>
          <p className="section-subtitle">
            Names of what voters will rate each option on. Scores are entered by voters
            during voting, not here.
          </p>
          <div className="email-input-bar">
            <input
              type="text"
              placeholder="New Criterion (e.g. Battery, Price)"
              value={criterionInput}
              disabled={!isDraft}
              onChange={(e) => setCriterionInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  addCriterion();
                }
              }}
            />
            <button type="button" className="btn-secondary" disabled={!isDraft} onClick={addCriterion}>
              + Add Criterion
            </button>
          </div>
          <div className="email-chips-container">
            {criteria.map((c) => (
              <div key={c} className="email-chip">
                <span>{c.toUpperCase()}</span>
                {isDraft && (
                  <button type="button" onClick={() => removeCriterion(c)}>
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        </>
      )}

      <h4>Option Choices</h4>
      {options.map((opt) => (
        <div key={opt.id} className="edit-option-card">
          <div className="form-group">
            <label>Option Title</label>
            <input
              type="text"
              value={opt.title}
              disabled={!isDraft}
              onChange={(e) => handleOptionChange(opt.id, "title", e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea
              rows="2"
              value={opt.description}
              disabled={!isDraft}
              onChange={(e) => handleOptionChange(opt.id, "description", e.target.value)}
            />
          </div>
        </div>
      ))}

      <div className="edit-board-actions">
        <button className="btn-primary" disabled={saving || publishing} onClick={handleSave}>
          {saving ? "Saving..." : "Save Changes"}
        </button>
        {isDraft && (
          <button
            className="btn-primary"
            disabled={saving || publishing}
            onClick={handlePublish}
            style={{ background: "#16a34a" }}
          >
            {publishing ? "Publishing..." : "Publish Decision"}
          </button>
        )}
        <button className="btn-secondary" disabled={saving || publishing} onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}