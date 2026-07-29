import { useState, useEffect } from "react";
import { submitScore, getMyScores, getRanking } from "../services/voteService";

export default function RatingPanel({ decision, pollOpen, onScoreSubmitted }) {
  // Ratings are private per user: sliders always start at 0 until we load
  // (or the user sets) THIS user's own score. We deliberately do NOT read
  // decision.options[].comparisonScores here — that field can include every
  // participant's scores, and showing another user's rating to this user
  // would leak private votes.
  const [scores, setScores] = useState({});
  const [savedScores, setSavedScores] = useState({});
  const [ranking, setRanking] = useState(null);
  const [savingKey, setSavingKey] = useState(null);

  useEffect(() => {
    fetchMyScores();
    if (!pollOpen) fetchRanking();
  }, [decision.id, pollOpen]);

  const fetchMyScores = async () => {
    try {
      const res = await getMyScores(decision.id);
      const mine = {};
      (res.data || []).forEach((s) => {
        mine[`${s.optionId}-${s.factorId}`] = s.score;
      });
      setScores(mine);
      setSavedScores(mine);
    } catch (err) {
      console.error("Failed to fetch your scores:", err);
    }
  };

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

  // Explicit Save per rating — the slider no longer auto-submits on release,
  // so the user's in-progress drag never gets sent until they confirm.
  const handleSubmit = async (optionId, factorId) => {
    const key = `${optionId}-${factorId}`;
    const value = scores[key] ?? 0;
    setSavingKey(key);
    try {
      await submitScore(decision.id, optionId, factorId, value);
      setSavedScores((prev) => ({ ...prev, [key]: value }));
      // Let the parent page know a score was saved so it can refetch the
      // decision and refresh the Comparison Matrix without a manual reload.
      if (onScoreSubmitted) onScoreSubmitted();
    } catch (err) {
      console.error("Failed to submit score:", err);
    } finally {
      setSavingKey(null);
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
              {decision.factors.map((factor) => {
                const key = `${opt.id}-${factor.id}`;
                const value = scores[key] ?? 0;
                const isUnsaved = value !== (savedScores[key] ?? 0);
                return (
                  <td key={factor.id}>
                    <input
                      type="range"
                      min="0"
                      max="100"
                      disabled={!pollOpen}
                      value={value}
                      onChange={(e) =>
                        handleScoreChange(opt.id, factor.id, Number(e.target.value))
                      }
                    />
                    <span>{value}</span>
                    {pollOpen && (
                      <button
                        className="btn-save-score"
                        disabled={!isUnsaved || savingKey === key}
                        onClick={() => handleSubmit(opt.id, factor.id)}
                      >
                        {savingKey === key ? "Saving..." : "Save"}
                      </button>
                    )}
                  </td>
                );
              })}
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