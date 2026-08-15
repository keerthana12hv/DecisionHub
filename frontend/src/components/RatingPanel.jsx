import { useState, useEffect } from "react";
import { submitScore, getRanking } from "../services/voteService";
import { useToast } from "./Toast";

export default function RatingPanel({ decision, currentUserId, pollOpen, onScoreSubmitted }) {
  const { addToast } = useToast();

  // Prefill ONLY from the CURRENT USER's own previously-submitted scores.
  // comparisonScores contains one entry per voter (each carries a userId) —
  // filtering here is what keeps other people's individual ratings private;
  // without this filter, whichever entry happened to be first would leak.
  const buildSubmittedScores = () => {
    const result = {};
    (decision.options || []).forEach((opt) => {
      (opt.comparisonScores || [])
        .filter((cs) => String(cs.userId) === String(currentUserId))
        .forEach((cs) => {
          result[`${opt.id}-${cs.factorId}`] = cs.score;
        });
    });
    return result;
  };

  // submittedScores = what's actually been saved for this user (source of
  // truth for "your rating"). scores = the in-progress slider draft, which
  // starts at 0 (not a pre-filled midpoint) until the user moves it or it
  // gets initialized from their own submitted value.
  const [submittedScores, setSubmittedScores] = useState(buildSubmittedScores);
  const [scores, setScores] = useState(buildSubmittedScores);
  const [ranking, setRanking] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!pollOpen) fetchRanking();
  }, [pollOpen]);

  const fetchRanking = async () => {
    try {
      const res = await getRanking(decision.id);
      const data = res.data;
      const list = Array.isArray(data) ? data : data?.results || data?.rankings || [];
      setRanking(list);
    } catch (err) {
      console.error("Failed to fetch ranking:", err);
    }
  };

  const handleScoreChange = (optionId, factorId, value) => {
    setScores((prev) => ({ ...prev, [`${optionId}-${factorId}`]: value }));
  };

  const isDirty = decision.options.some((opt) =>
    decision.factors.some((factor) => {
      const key = `${opt.id}-${factor.id}`;
      return (scores[key] ?? 0) !== (submittedScores[key] ?? 0);
    })
  );

  // Single button submits EVERY changed rating across all options/criteria
  // in one pass — instead of a separate API call + button per criterion.
  const handleSubmitAll = async () => {
    setSubmitting(true);
    try {
      for (const opt of decision.options) {
        for (const factor of decision.factors) {
          const key = `${opt.id}-${factor.id}`;
          const draftValue = scores[key] ?? 0;
          const savedValue = submittedScores[key] ?? 0;
          if (draftValue !== savedValue) {
            await submitScore(decision.id, opt.id, factor.id, draftValue);
          }
        }
      }
      setSubmittedScores({ ...scores });
      addToast("Ratings submitted successfully.", "success");
      if (onScoreSubmitted) onScoreSubmitted();
    } catch (err) {
      console.error("Failed to submit ratings:", err);
      addToast("Failed to submit ratings.", "error");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="rating-panel">
      <h3>Rate each option</h3>
      <p className="section-subtitle">
        Your ratings are private — only you can see the scores you submit. Move the sliders
        for each option and criterion, then click "Submit Ratings" once at the bottom to
        save everything together.
      </p>

      <table>
        <thead>
          <tr>
            <th>Option</th>
            {decision.factors.map((f) => (
              <th key={f.id}>{f.name}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {decision.options.map((opt) => (
            <tr key={opt.id}>
              <td>{opt.title}</td>
              {decision.factors.map((factor) => {
                const key = `${opt.id}-${factor.id}`;
                return (
                  <td key={factor.id}>
                    <input
                      type="range"
                      min="0"
                      max="100"
                      disabled={!pollOpen}
                      value={scores[key] ?? 0}
                      onChange={(e) =>
                        handleScoreChange(opt.id, factor.id, Number(e.target.value))
                      }
                    />
                    <span>{scores[key] ?? 0}</span>
                    {submittedScores[key] !== undefined && (
                      <div className="your-rating-note">Your rating: {submittedScores[key]}</div>
                    )}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>

      <div className="rating-submit-row" style={{ marginTop: "1rem" }}>
        <button
          className="btn-primary"
          disabled={!pollOpen || submitting || !isDirty}
          onClick={handleSubmitAll}
        >
          {submitting ? "Submitting..." : "Submit Ratings"}
        </button>
      </div>

      {!pollOpen && ranking && (
        <div className="ranking-results">
          <h4>Final Ranking</h4>
          <ol>
            {ranking.map((r) => (
              <li key={r.optionId}>
                {r.optionName} — {r.finalScore.toFixed(1)}
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
}