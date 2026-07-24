import { useState, useEffect } from "react";
import {
  getDecisions,
  lockDecision,
  unlockDecision,
  pinDecision,
  unpinDecision
} from "../../services/communityService";

export default function DecisionModeration({ communityId }) {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actingId, setActingId] = useState(null);

  useEffect(() => {
    fetchDecisions();
  }, [communityId]);

  const fetchDecisions = async () => {
    try {
      setLoading(true);
      const res = await getDecisions({ communityId });
      setDecisions(res.data);
    } catch (err) {
      console.error("Failed to load decisions:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleLock = async (decision) => {
    setActingId(decision.id);
    try {
      if (decision.locked) {
        await unlockDecision(decision.id);
      } else {
        await lockDecision(decision.id);
      }
      await fetchDecisions();
    } catch (err) {
      console.error("Failed to toggle lock:", err);
    } finally {
      setActingId(null);
    }
  };

  const handleTogglePin = async (decision) => {
    setActingId(decision.id);
    try {
      if (decision.pinned) {
        await unpinDecision(decision.id);
      } else {
        await pinDecision(decision.id);
      }
      await fetchDecisions();
    } catch (err) {
      console.error("Failed to toggle pin:", err);
    } finally {
      setActingId(null);
    }
  };

  if (loading) return <p>Loading decisions...</p>;

  return (
    <div className="decision-moderation">
      <h4>Decisions ({decisions.length})</h4>
      {decisions.length === 0 ? (
        <p>No decisions found for this community.</p>
      ) : (
        <table className="decision-mod-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Status</th>
              <th>Locked</th>
              <th>Pinned</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {decisions.map((d) => (
              <tr key={d.id}>
                <td>{d.title}</td>
                <td>{d.status}</td>
                <td>{d.locked ? "Yes" : "No"}</td>
                <td>{d.pinned ? "Yes" : "No"}</td>
                <td>
                  <button
                    disabled={actingId === d.id}
                    onClick={() => handleToggleLock(d)}
                  >
                    {d.locked ? "Unlock" : "Lock"}
                  </button>
                  <button
                    disabled={actingId === d.id}
                    onClick={() => handleTogglePin(d)}
                  >
                    {d.pinned ? "Unpin" : "Pin"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}