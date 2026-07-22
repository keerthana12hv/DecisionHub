import { useState, useEffect } from "react";
import { getRules, createRule, updateRule, deleteRule } from "../../services/communityService";

export default function CommunityRules({ communityId }) {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    fetchRules();
  }, [communityId]);

  const fetchRules = async () => {
    try {
      setLoading(true);
      const res = await getRules(communityId);
      setRules(res.data);
    } catch (err) {
      console.error("Failed to load rules:", err);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setTitle("");
    setDescription("");
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !description.trim()) return;

    try {
      if (editingId) {
        await updateRule(editingId, { title, description });
      } else {
        await createRule(communityId, { title, description });
      }
      resetForm();
      fetchRules();
    } catch (err) {
      console.error("Failed to save rule:", err);
    }
  };

  const handleEdit = (rule) => {
    setEditingId(rule.id);
    setTitle(rule.title);
    setDescription(rule.description);
  };

  const handleDelete = async (ruleId) => {
    if (!window.confirm("Delete this rule?")) return;
    try {
      await deleteRule(ruleId);
      fetchRules();
    } catch (err) {
      console.error("Failed to delete rule:", err);
    }
  };

  if (loading) return <p>Loading rules...</p>;

  return (
    <div className="community-rules">
      <h3>Community Rules</h3>

      <form onSubmit={handleSubmit} className="rule-form">
        <input
          type="text"
          placeholder="Rule title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          maxLength={100}
        />
        <textarea
          placeholder="Rule description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          maxLength={1000}
        />
        <div>
          <button type="submit">{editingId ? "Update Rule" : "Add Rule"}</button>
          {editingId && (
            <button type="button" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {rules.length === 0 ? (
        <p>No rules set for this community yet.</p>
      ) : (
        rules.map((rule) => (
          <div key={rule.id} className="rule-row">
            <strong>{rule.title}</strong>
            <p>{rule.description}</p>
            <button onClick={() => handleEdit(rule)}>Edit</button>
            <button onClick={() => handleDelete(rule.id)}>Delete</button>
          </div>
        ))
      )}
    </div>
  );
}