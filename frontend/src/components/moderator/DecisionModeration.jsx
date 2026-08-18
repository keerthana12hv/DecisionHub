import { useState, useEffect } from "react";
import {
  FaLock, FaLockOpen, FaThumbtack, FaGavel, FaSpinner,
} from "react-icons/fa";
import {
  lockDecision,
  unlockDecision,
  pinDecision,
  unpinDecision,
} from "../../services/moderationService";
import { useToast } from "../Toast";
import api from "../../services/api.js"


  const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
export default function DecisionModeration({ communityId }) {
  const { addToast } = useToast();
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actioning, setActioning] = useState(null);

  useEffect(() => {
    fetchDecisions();
  }, [communityId]);

  const fetchDecisions = async () => {
    try {
      setLoading(true);
      const res = await api.get(
        `/decisions?communityId=${communityId}`,
        headers()
      );
      setDecisions(res.data);
    } catch {
      addToast("Failed to load decisions", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (decisionId, action) => {
    setActioning(decisionId + action);
    try {
      if (action === "lock") {
        await lockDecision(decisionId);
        updateDecision(decisionId, { discussionLocked: true });
        addToast("Discussion locked", "success");
      } else if (action === "unlock") {
        await unlockDecision(decisionId);
        updateDecision(decisionId, { discussionLocked: false });
        addToast("Discussion unlocked", "success");
      } else if (action === "pin") {
        await pinDecision(decisionId);
        updateDecision(decisionId, { pinned: true });
        addToast("Decision pinned", "success");
      } else if (action === "unpin") {
        await unpinDecision(decisionId);
        updateDecision(decisionId, { pinned: false });
        addToast("Decision unpinned", "success");
      }
    } catch {
      addToast("Action failed. Please try again.", "error");
    } finally {
      setActioning(null);
    }
  };

  const updateDecision = (id, updates) => {
    setDecisions((prev) =>
      prev.map((d) => (d.id === id ? { ...d, ...updates } : d))
    );
  };

  const isActioning = (id, action) => actioning === id + action;

  if (loading) return <div className="mod-loading">Loading decisions...</div>;

  return (
    <div className="mod-section">
      <h3 className="mod-section-title">
        <FaGavel /> Decision Moderation
      </h3>

      {decisions.length === 0 ? (
        <p className="mod-empty">No decisions in this community yet.</p>
      ) : (
        <div className="mod-list">
          {decisions.map((decision) => (
            <div key={decision.id} className="mod-card mod-card-decision">
              <div className="mod-card-info">
                <div>
                  <p className="mod-name">
                    {decision.pinned && (
                      <span className="mod-pin-icon" title="Pinned">
                        <FaThumbtack />
                      </span>
                    )}
                    {decision.title}
                  </p>
                  <p className="mod-meta">
                    Status: <strong>{decision.status}</strong> &nbsp;|&nbsp;
                    Discussion:{" "}
                    <strong>
                      {decision.discussionLocked ? "🔒 Locked" : "🔓 Open"}
                    </strong>
                  </p>
                </div>
              </div>

              <div className="mod-decision-actions">
                {/* Lock / Unlock Discussion */}
                {decision.discussionLocked ? (
                  <button
                    className="mod-btn mod-btn-approve"
                    disabled={isActioning(decision.id, "unlock")}
                    onClick={() => handleAction(decision.id, "unlock")}
                  >
                    {isActioning(decision.id, "unlock") ? (
                      <FaSpinner className="spin" />
                    ) : (
                      <FaLockOpen />
                    )}
                    &nbsp;Unlock
                  </button>
                ) : (
                  <button
                    className="mod-btn mod-btn-reject"
                    disabled={isActioning(decision.id, "lock")}
                    onClick={() => handleAction(decision.id, "lock")}
                  >
                    {isActioning(decision.id, "lock") ? (
                      <FaSpinner className="spin" />
                    ) : (
                      <FaLock />
                    )}
                    &nbsp;Lock
                  </button>
                )}

                {/* Pin / Unpin Decision */}
                {decision.pinned ? (
                  <button
                    className="mod-btn mod-btn-ghost"
                    disabled={isActioning(decision.id, "unpin")}
                    onClick={() => handleAction(decision.id, "unpin")}
                  >
                    <FaThumbtack /> Unpin
                  </button>
                ) : (
                  <button
                    className="mod-btn mod-btn-pin"
                    disabled={isActioning(decision.id, "pin")}
                    onClick={() => handleAction(decision.id, "pin")}
                  >
                    <FaThumbtack /> Pin
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}