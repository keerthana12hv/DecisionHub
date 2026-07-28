import { useState } from "react";
import { pinDecision, unpinDecision, lockDecision, unlockDecision } from "../../services/moderationService";

export default function DecisionModerationControls({ decision, onUpdate }) {
  const [pinned, setPinned] = useState(decision.pinned);
  const [locked, setLocked] = useState(decision.locked);
  const [loading, setLoading] = useState(false);

  const togglePin = async () => {
    setLoading(true);
    try {
      const res = pinned
        ? await unpinDecision(decision.id)
        : await pinDecision(decision.id);
      setPinned(res.data.pinned);
      if (onUpdate) onUpdate(res.data);
    } catch (err) {
      console.error("Failed to toggle pin:", err);
    } finally {
      setLoading(false);
    }
  };

  const toggleLock = async () => {
    setLoading(true);
    try {
      const res = locked
        ? await unlockDecision(decision.id)
        : await lockDecision(decision.id);
      setLocked(res.data.locked);
      if (onUpdate) onUpdate(res.data);
    } catch (err) {
      console.error("Failed to toggle lock:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="decision-moderation-controls">
      <button onClick={togglePin} disabled={loading}>
        {pinned ? "Unpin" : "Pin"}
      </button>
      <button onClick={toggleLock} disabled={loading}>
        {locked ? "Unlock" : "Lock"}
      </button>
    </div>
  );
}