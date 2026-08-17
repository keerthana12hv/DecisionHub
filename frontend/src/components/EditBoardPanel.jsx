import { useState } from "react";
import { useToast } from "./Toast";
import api from "../services/api";


const parseLocalDateTimeForInput = (dateTimeStr) => {
  if (!dateTimeStr) return "";
  if (dateTimeStr.includes("Z") || dateTimeStr.includes("+")) {
    const d = new Date(dateTimeStr);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
  return dateTimeStr.slice(0, 16);
};

const formatLocalDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return null;
  if (dateTimeStr.length === 16) {
    return `${dateTimeStr}:00`;
  }
  return dateTimeStr;
};

const getLocalISOTime = () => {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

export default function EditBoardPanel({ decision, onSaved, onCancel }) {
  const { addToast } = useToast();
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

  const [votingEndTime, setVotingEndTime] = useState(
    decision.votingEndTime ? parseLocalDateTimeForInput(decision.votingEndTime) : ""
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

  const saveDraftEdits = async () => {
    // 1. Put decision details
    const payload = {
      title,
      description: decision.categoryName ? `[Cat:${decision.categoryName}] ${description}` : description,
      tags: decision.categoryName ? [decision.categoryName] : [],
      votingType: decision.votingType,
      isPublic: decision.isPublic ?? true,
      anonymityType: decision.anonymityType || "PUBLIC",
      deadline: formatLocalDateTime(decision.deadline),
      votingEndTime: formatLocalDateTime(votingEndTime)
    };
    await api.put(`/decisions/${decision.id}`, payload);
    // 2. Put changed options
    const changedOptions = options.filter((opt) => {
      const original = (decision.options || []).find((o) => o.id === opt.id);
      const originalTitle = original?.title || "";
      const originalDesc = original?.description || "";
      return originalTitle !== opt.title || originalDesc !== opt.description;
    });
    for (const opt of changedOptions) {
      await api.put(
          `/decisions/${decision.id}/options/${opt.id}`,
          {title: opt.title, description: opt.description}
      );
    }

    // 3. Delete/Create factors
    if (decision.votingType === "RATING_BASED") {
      const deletedFactors = (decision.factors || []).filter((f) => !criteria.includes(f.name));
      const addedFactorNames = criteria.filter(
          (c) => !(decision.factors || []).some((f) => f.name === c)
      );

      for (const factor of deletedFactors) {
        await api.delete(`/decisions/${decision.id}/factors/${factor.id}`
        );
        for (const name of addedFactorNames) {
          await api.post(
              `/decisions/${decision.id}/factors`,
              {name, description: ""}
          );
        }
      }
    }
    ;

    const handlePublish = async () => {
      setPublishing(true);
      setError(null);
      try {
        // Save any edits made while still in Draft before publishing
        await saveDraftEdits();

        // Draft -> Active, auto-creates the Poll.
        await api.put(`/decisions/${decision.id}/publish`);

        // Re-fetch the decision to ensure we return fresh data from backend
        const res = await api.get(`/decisions/${decision.id}`);
        let updatedDecision = res.data;
        if (updatedDecision && updatedDecision.description) {
          const match = updatedDecision.description.match(/^\[Cat:([^\]]+)\]\s*(.*)/s);
          if (match) {
            updatedDecision.categoryName = match[1];
            updatedDecision.description = match[2];
          }
        }
        if (updatedDecision && updatedDecision.status !== "DRAFT") {
          try {
            const pollRes = await api.get(`/decisions/${decision.id}/poll`);
            if (pollRes.data) {
              updatedDecision.poll = pollRes.data;
              if (pollRes.data.endTime) {
                updatedDecision.votingEndTime = pollRes.data.endTime;
              }
            }
          } catch (pollErr) {
            console.error("Failed to fetch poll details post-publish:", pollErr);
          }
        }
        onSaved(updatedDecision);
      } catch (err) {
        console.error("Failed to publish decision:", err.response?.data || err.message);
        let errMsg = "Could not publish this decision. Check console for details.";
        if (err.response?.data) {
          if (typeof err.response.data === "object") {
            errMsg = err.response.data.error || err.response.data.message || errMsg;
          } else if (typeof err.response.data === "string") {
            errMsg = err.response.data;
          }
        } else if (err.message) {
          errMsg = err.message;
        }
        setError(errMsg);
        addToast(errMsg, "error");
      } finally {
        setPublishing(false);
      }
    };

    const handleSave = async () => {
      setSaving(true);
      setError(null);
      try {
        if (isDraft) {
          await saveDraftEdits();
        } else {
          // Published/Closed: ONLY Poll End Time can change. Use the dedicated extend-end-time endpoint
          await api.patch(
              `/decisions/${decision.id}/poll/end-time`,
              {endTime: formatLocalDateTime(votingEndTime)}
          );
        }

        // Re-fetch the decision to ensure we return fresh data from backend
        const res = await api.get(`/decisions/${decision.id}`);
        let updatedDecision = res.data;
        if (updatedDecision && updatedDecision.description) {
          const match = updatedDecision.description.match(/^\[Cat:([^\]]+)\]\s*(.*)/s);
          if (match) {
            updatedDecision.categoryName = match[1];
            updatedDecision.description = match[2];
          }
        }
        if (updatedDecision && updatedDecision.status !== "DRAFT") {
          try {
            const pollRes = await api.get(`/decisions/${decision.id}/poll`);
            if (pollRes.data) {
              updatedDecision.poll = pollRes.data;
              if (pollRes.data.endTime) {
                updatedDecision.votingEndTime = pollRes.data.endTime;
              }
            }
          } catch (pollErr) {
            console.error("Failed to fetch poll details post-save:", pollErr);
          }
        }
        onSaved(updatedDecision);
      } catch (err) {
        console.error("Failed to save decision:", err.response?.data || err.message);
        let errMsg = "Could not save changes. Check console for details.";
        if (err.response?.data) {
          if (typeof err.response.data === "object") {
            errMsg = err.response.data.error || err.response.data.message || errMsg;
          } else if (typeof err.response.data === "string") {
            errMsg = err.response.data;
          }
        } else if (err.message) {
          errMsg = err.message;
        }
        setError(errMsg);
        addToast(errMsg, "error");
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
                min={getLocalISOTime()}
                max={decision.deadline ? parseLocalDateTimeForInput(decision.deadline) : undefined}
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
                    style={{background: "#16a34a"}}
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
}