import { useState, useEffect } from "react";
import {
  FaListUl, FaPlus, FaEdit, FaTrash, FaCheck, FaTimes,
} from "react-icons/fa";
import {
  getCommunityRules,
  addCommunityRule,
  updateCommunityRule,
  deleteCommunityRule,
} from "../../services/moderationService";
import { useToast } from "../Toast";

export default function CommunityRules({ communityId }) {
  const { addToast } = useToast();
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newRule, setNewRule] = useState({ title: "", description: "" });
  const [editingId, setEditingId] = useState(null);
  const [editData, setEditData] = useState({ title: "", description: "" });

  useEffect(() => {
    fetchRules();
  }, [communityId]);

  const fetchRules = async () => {
    try {
      setLoading(true);
      const data = await getCommunityRules(communityId);
      setRules(data);
    } catch {
      addToast("Failed to load rules", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = async () => {
    if (!newRule.title.trim()) {
      addToast("Rule title is required", "error");
      return;
    }
    try {
      const created = await addCommunityRule(communityId, newRule);
      setRules((prev) => [...prev, created]);
      setNewRule({ title: "", description: "" });
      setShowAddForm(false);
      addToast("Rule added!", "success");
    } catch {
      addToast("Failed to add rule", "error");
    }
  };

  const startEdit = (rule) => {
    setEditingId(rule.id);
    setEditData({ title: rule.title, description: rule.description || "" });
  };

  const handleUpdate = async (ruleId) => {
    if (!editData.title.trim()) {
      addToast("Rule title is required", "error");
      return;
    }
    try {
      const updated = await updateCommunityRule(communityId, ruleId, editData);
      setRules((prev) => prev.map((r) => (r.id === ruleId ? updated : r)));
      setEditingId(null);
      addToast("Rule updated!", "success");
    } catch {
      addToast("Failed to update rule", "error");
    }
  };

  const handleDelete = async (ruleId) => {
    if (!window.confirm("Delete this rule?")) return;
    try {
      await deleteCommunityRule(communityId, ruleId);
      setRules((prev) => prev.filter((r) => r.id !== ruleId));
      addToast("Rule deleted", "info");
    } catch {
      addToast("Failed to delete rule", "error");
    }
  };

  if (loading) return <div className="mod-loading">Loading rules...</div>;

  return (
    <div className="mod-section">
      <div className="mod-section-header">
        <h3 className="mod-section-title">
          <FaListUl /> Community Rules
          <span className="mod-badge">{rules.length}</span>
        </h3>
        <button
          className="mod-btn mod-btn-add"
          onClick={() => setShowAddForm(!showAddForm)}
        >
          <FaPlus /> Add Rule
        </button>
      </div>

      {/* Add New Rule Form */}
      {showAddForm && (
        <div className="mod-rule-form">
          <input
            className="mod-input"
            placeholder="Rule title *"
            value={newRule.title}
            onChange={(e) => setNewRule({ ...newRule, title: e.target.value })}
          />
          <textarea
            className="mod-textarea"
            placeholder="Rule description (optional)"
            value={newRule.description}
            onChange={(e) =>
              setNewRule({ ...newRule, description: e.target.value })
            }
            rows={2}
          />
          <div className="mod-form-actions">
            <button className="mod-btn mod-btn-approve" onClick={handleAdd}>
              <FaCheck /> Save Rule
            </button>
            <button
              className="mod-btn mod-btn-ghost"
              onClick={() => {
                setShowAddForm(false);
                setNewRule({ title: "", description: "" });
              }}
            >
              <FaTimes /> Cancel
            </button>
          </div>
        </div>
      )}

      {/* Rules List */}
      {rules.length === 0 ? (
        <p className="mod-empty">No rules yet. Add the first rule!</p>
      ) : (
        <div className="mod-rules-list">
          {rules.map((rule, idx) => (
            <div key={rule.id} className="mod-rule-card">
              {editingId === rule.id ? (
                <div className="mod-rule-form">
                  <input
                    className="mod-input"
                    value={editData.title}
                    onChange={(e) =>
                      setEditData({ ...editData, title: e.target.value })
                    }
                  />
                  <textarea
                    className="mod-textarea"
                    value={editData.description}
                    onChange={(e) =>
                      setEditData({ ...editData, description: e.target.value })
                    }
                    rows={2}
                  />
                  <div className="mod-form-actions">
                    <button
                      className="mod-btn mod-btn-approve"
                      onClick={() => handleUpdate(rule.id)}
                    >
                      <FaCheck /> Save
                    </button>
                    <button
                      className="mod-btn mod-btn-ghost"
                      onClick={() => setEditingId(null)}
                    >
                      <FaTimes /> Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="mod-rule-content">
                    <span className="mod-rule-num">{idx + 1}</span>
                    <div>
                      <p className="mod-rule-title">{rule.title}</p>
                      {rule.description && (
                        <p className="mod-rule-desc">{rule.description}</p>
                      )}
                    </div>
                  </div>
                  <div className="mod-rule-btns">
                    <button
                      className="mod-icon-btn mod-icon-edit"
                      onClick={() => startEdit(rule)}
                      title="Edit rule"
                    >
                      <FaEdit />
                    </button>
                    <button
                      className="mod-icon-btn mod-icon-delete"
                      onClick={() => handleDelete(rule.id)}
                      title="Delete rule"
                    >
                      <FaTrash />
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}