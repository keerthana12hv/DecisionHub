import { useState, useEffect } from "react";
import { submitScore, getRanking } from "../services/voteService";

export default function RatingPanel({ decision, pollOpen, onScoreSubmitted }) {
  // Prefill from any scores already submitted (comparisonScores, keyed by factorId)
  const initialScores = {};
  (decision.options || []).forEach((opt) => {
    (opt.comparisonScores || []).forEach((cs) => {
      initialScores[`${opt.id}-${cs.factorId}`] = cs.score;
    });
  });

  const [scores, setScores] = useState(initialScores);
  const [ranking, setRanking] = useState(null);

  useEffect(() => {
    if (!pollOpen) fetchRanking();
  }, [pollOpen]);

  const fetchRanking = async () => {
    try {
      const res = await getRanking(decision.id);
      console.log("Ranking API response shape:", res.data); // TEMP — check this in console to confirm real field names
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

  const handleSubmit = async (optionId, factorId) => {
    const value = scores[`${optionId}-${factorId}`];
    if (value == null) return;
    try {
      await submitScore(decision.id, optionId, factorId, value);
      // Let the parent page know a score was saved so it can refetch the
      // decision and refresh the Comparison Matrix without a manual reload.
      if (onScoreSubmitted) onScoreSubmitted();
    } catch (err) {
      console.error("Failed to submit score:", err);
    }
  };

  return (
    <div className="rating-panel">
      <h3>Rate each option</h3>

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
              {decision.factors.map((factor) => (
                <td key={factor.id}>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    disabled={!pollOpen}
                    value={scores[`${opt.id}-${factor.id}`] ?? 50}
                    onChange={(e) =>
                      handleScoreChange(opt.id, factor.id, Number(e.target.value))
                    }
                    onMouseUp={() => handleSubmit(opt.id, factor.id)}
                  />
                  <span>{scores[`${opt.id}-${factor.id}`] ?? 50}</span>
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

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