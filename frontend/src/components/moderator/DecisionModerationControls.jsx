import { useState } from "react";
import { pinDecision, unpinDecision, lockDecision, unlockDecision, closeDecision } from "../../services/moderationService";

export default function DecisionModerationControls({ decision, onUpdate }) {
  const [pinned, setPinned] = useState(decision.pinned);
  const [locked, setLocked] = useState(decision.locked);
  const [status, setStatus] = useState(decision.status);
  const [loading, setLoading] = useState(false);

  const togglePin = async () => {
    setLoading(true);
    try {
      const res = pinned
        ? await unpinDecision(decision.id)
        : await pinDecision(decision.id);
      setPinned(res.pinned);
      if (onUpdate) onUpdate(res);
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
      setLocked(res.locked);
      if (onUpdate) onUpdate(res);
    } catch (err) {
      console.error("Failed to toggle lock:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = async () => {
    setLoading(true);
    try {
      const res = await closeDecision(decision.id);
      setStatus(res.status);
      if (onUpdate) onUpdate(res);
    } catch (err) {
      console.error("Failed to close decision:", err);
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
      {status === "ACTIVE" && (
        <button onClick={handleClose} disabled={loading}>
          Close Decision
        </button>
      )}
    </div>
  );
}
