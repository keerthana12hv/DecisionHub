import { useState } from "react";
import axios from "axios";
import { submitScore } from "../services/voteService";

const API = "http://localhost:8080/api";

const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");

const headers = () => ({
  headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json" }
});

const CATEGORY_OPTIONS = [
  "Technology",
  "Career",
  "Finance",
  "Lifestyle",
  "Travel",
  "Programming",
  "Team Building"
];

const toKey = (label) => label.trim().toLowerCase().replace(/\s+/g, "_");

export default function EditBoardPanel({ decision, onSaved, onCancel }) {
  const [title, setTitle] = useState(decision.title || "");
  const [category, setCategory] = useState(decision.categoryName || CATEGORY_OPTIONS[0]);
  // NOTE: categoryName is currently always null from the backend, even after sending
  // tags at creation — the tags→category link isn't confirmed working yet.
  const [description, setDescription] = useState(decision.description || "");
  const [criteria, setCriteria] = useState((decision.factors || []).map((f) => f.name));
  const [criterionInput, setCriterionInput] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const [options, setOptions] = useState(
    (decision.options || []).map((opt) => {
      const criteriaValues = {};
      (decision.factors || []).forEach((f) => {
        const match = (opt.comparisonScores || []).find((s) => s.factorId === f.id);
        if (match) {
          criteriaValues[toKey(f.name)] =
            match.remarks && match.remarks.trim() !== "" ? match.remarks : match.score;
        }
      });
      return {
        id: opt.id,
        title: opt.title || "",
        description: opt.description || "",
        criteriaValues
      };
    })
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

  const handleCriteriaValueChange = (optionId, criterionLabel, val) => {
    setOptions(
      options.map((opt) =>
        opt.id === optionId
          ? { ...opt, criteriaValues: { ...opt.criteriaValues, [toKey(criterionLabel)]: val } }
          : opt
      )
    );
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      // Step 1: save board-level details (title/description/tags/etc).
      // NOTE: the "criteria" field on this payload is NOT persisted by the backend —
      // confirmed via Swagger. Score values must go through submitScore instead (step 2).
      const payload = {
        title,
        description,
        tags: [category],
        votingType: decision.votingType,
        isPublic: decision.isPublic ?? true,
        anonymityType: decision.anonymityType || "PUBLIC",
        // Both deadline and votingEndTime sent back completely unchanged (raw strings
        // from GET) — the backend rejects them as "modified" if reformatted at all,
        // even when the actual value is identical.
        deadline: decision.deadline,
        votingEndTime: decision.votingEndTime,
        options: options.map((opt) => ({
          title: opt.title,
          description: opt.description
        })),
        factors: criteria.map((c) => ({ name: c, description: "" }))
      };

      const res = await axios.put(`${API}/decisions/${decision.id}`, payload, headers());
      let updatedDecision = res.data;

      // Step 2: push each option's Criteria Specification value through the
      // confirmed-working score endpoint, matching each criterion name back to
      // its factor id from the freshly-saved decision (factor ids may have
      // changed if criteria were added/removed above).
      const factorByName = {};
      (updatedDecision.factors || []).forEach((f) => {
        factorByName[f.name.trim().toLowerCase()] = f.id;
      });

      for (const opt of options) {
        for (const c of criteria) {
          const factorId = factorByName[c.trim().toLowerCase()];
          if (!factorId) continue; // criterion wasn't saved as a factor, skip
          const raw = opt.criteriaValues[toKey(c)];
          if (raw === undefined || raw === "") continue; // nothing entered, don't overwrite
          const numeric = parseFloat(raw);
          const score = Number.isFinite(numeric) ? numeric : 0;
          try {
            await submitScore(decision.id, opt.id, factorId, score);
          } catch (scoreErr) {
            console.error(
              `Failed to save score for option ${opt.id}, factor ${factorId}:`,
              scoreErr
            );
          }
        }
      }

      // Step 3: refetch so the Comparison Matrix reflects the scores just submitted.
      const freshRes = await axios.get(`${API}/decisions/${decision.id}`, headers());
      onSaved(freshRes.data);
    } catch (err) {
      console.error("Failed to save decision:", err.response?.data || err.message);
      setError(
        err.response?.data?.error ||
          "Could not save changes. This endpoint may not be confirmed yet — check console."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="edit-board-panel">
      <h3>Edit Decision Board &amp; Options</h3>
      <p className="section-subtitle">
        Modify the board parameters, comparison criteria, and option choices directly on this page.
      </p>

      {error && <div className="edit-board-error">{error}</div>}

      <h4>Board Parameters</h4>
      <div className="form-group">
        <label>Decision Title</label>
        <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} />
      </div>

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

      <div className="form-group">
        <label>Description</label>
        <textarea
          rows="3"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <h4>Comparison Criteria</h4>
      <p className="section-subtitle">
        Add, edit, or remove comparison criteria for this decision board.
      </p>
      <div className="email-input-bar">
        <input
          type="text"
          placeholder="New Criterion (e.g. Battery, Price)"
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
          + Add Criterion
        </button>
      </div>
      <div className="email-chips-container">
        {criteria.map((c) => (
          <div key={c} className="email-chip">
            <span>{c.toUpperCase()}</span>
            <button type="button" onClick={() => removeCriterion(c)}>
              ×
            </button>
          </div>
        ))}
      </div>

      <h4>Option Choices</h4>
      {options.map((opt) => (
        <div key={opt.id} className="edit-option-card">
          <div className="form-group">
            <label>Option Title</label>
            <input
              type="text"
              value={opt.title}
              onChange={(e) => handleOptionChange(opt.id, "title", e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea
              rows="2"
              value={opt.description}
              onChange={(e) => handleOptionChange(opt.id, "description", e.target.value)}
            />
          </div>

          {criteria.length > 0 && (
            <>
              <h5>Criteria Specifications</h5>
              <div className="form-group-grid">
                {criteria.map((c) => (
                  <div className="form-group" key={c}>
                    <label>{c.toLowerCase()}</label>
                    <input
                      type="text"
                      value={opt.criteriaValues[toKey(c)] ?? ""}
                      onChange={(e) =>
                        handleCriteriaValueChange(opt.id, c, e.target.value)
                      }
                    />
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      ))}

      <div className="edit-board-actions">
        <button className="btn-primary" disabled={saving} onClick={handleSave}>
          {saving ? "Saving..." : "Save All Details"}
        </button>
        <button className="btn-secondary" disabled={saving} onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}