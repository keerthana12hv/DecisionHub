import { useState, useEffect } from "react";
import { getRanking } from "../services/voteService";

const DONUT_COLORS = ["#a5a0ff", "#4ade80", "#facc15", "#f472b6", "#60a5fa", "#fb923c"];

// Confirmed from the real API response: { decisionId, decisionTitle, options: [...], status }
const getOptionLabel = (r) => r.optionTitle;
const getOptionKey = (r) => r.optionId;
const getScore = (r) => r.score ?? 0;

function RatingResults({ decisionId, pollOpen, refreshTick }) {
  const [ranking, setRanking] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRanking();
  }, [decisionId]);

  // Immediately pick up newly submitted ratings — fires whenever a rating is
  // saved anywhere in the page (including by this user, just now).
  useEffect(() => {
    if (refreshTick) fetchRanking(true);
  }, [refreshTick]);

  // While the poll is still open, keep results current for anyone parked on
  // this tab as other users vote — mirrors the 5s live-refresh used elsewhere
  // on the decision page, rather than only updating when the tab re-opens.
  useEffect(() => {
    if (!pollOpen) return;
    const intervalId = setInterval(() => fetchRanking(true), 5000);
    return () => clearInterval(intervalId);
  }, [decisionId, pollOpen]);

  // silent=true avoids re-showing the loading state for background refreshes.
  const fetchRanking = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const res = await getRanking(decisionId);
      const data = res.data;
      const list = data?.options || [];
      setRanking(list);
    } catch (err) {
      console.error("Failed to fetch ranking:", err);
      if (!silent) setRanking([]);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  if (loading) return <p className="poll-results-empty">Loading results...</p>;
  if (!ranking || ranking.length === 0) {
    return (
      <div className="poll-results-empty">
        <p>No ratings have been submitted on this decision yet.</p>
      </div>
    );
  }

  const totalScore = ranking.reduce((sum, r) => sum + getScore(r), 0);
  const sorted = [...ranking].sort((a, b) => (a.rank ?? 999) - (b.rank ?? 999));
  const leading = sorted[0];
  const leadingPercent = totalScore > 0 ? Math.round((getScore(leading) / totalScore) * 100) : 0;

  let cumulativePercent = 0;
  const gradientStops = sorted
    .filter((r) => getScore(r) > 0)
    .map((r, i) => {
      const percent = totalScore > 0 ? (getScore(r) / totalScore) * 100 : 0;
      const start = cumulativePercent;
      cumulativePercent += percent;
      const color = DONUT_COLORS[i % DONUT_COLORS.length];
      return `${color} ${start}% ${cumulativePercent}%`;
    });
  const donutStyle = { background: `conic-gradient(${gradientStops.join(", ")})` };

  return (
    <div className="poll-results-panel">
      <div className="leading-option-banner">
        <span className="trophy-icon">🏆</span>
        <div>
          <strong>Leading Option: {getOptionLabel(leading)}</strong>
          <p>
            Leading with a score of {getScore(leading).toFixed(1)} ({leadingPercent}% of total
            weighted score).
          </p>
        </div>
      </div>

      <div className="poll-results-grid">
        <div className="vote-breakdown-card">
          <h3>Score Breakdown</h3>
          {sorted.map((r) => {
            const percent = totalScore > 0 ? Math.round((getScore(r) / totalScore) * 100) : 0;
            return (
              <div className="vote-breakdown-row" key={getOptionKey(r)}>
                <div className="vote-breakdown-label">
                  <span>
                    {getOptionLabel(r)} {getOptionKey(r) === getOptionKey(leading) && "✓"}
                  </span>
                  <span>
                    {getScore(r).toFixed(1)} pts ({percent}%)
                  </span>
                </div>
                <div className="vote-breakdown-bar-track">
                  <div className="vote-breakdown-bar-fill" style={{ width: `${percent}%` }} />
                </div>
              </div>
            );
          })}
        </div>

        <div className="consensus-share-card">
          <h3>Consensus Share</h3>
          <div className="donut-chart" style={donutStyle} />
          <div className="donut-legend">
            {sorted
              .filter((r) => getScore(r) > 0)
              .map((r, i) => (
                <div className="donut-legend-item" key={getOptionKey(r)}>
                  <span
                    className="legend-dot"
                    style={{ background: DONUT_COLORS[i % DONUT_COLORS.length] }}
                  />
                  {getOptionLabel(r)}
                </div>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function VoteCountResults({ decision }) {
  const options = decision.options || [];
  const totalVotes = options.reduce((sum, opt) => sum + (opt.voteCount ?? 0), 0);

  if (totalVotes === 0) {
    return (
      <div className="poll-results-empty">
        <p>No votes have been cast on this decision yet.</p>
      </div>
    );
  }

  const withPercent = options.map((opt) => ({
    ...opt,
    votes: opt.voteCount ?? 0,
    percent: totalVotes > 0 ? Math.round(((opt.voteCount ?? 0) / totalVotes) * 100) : 0
  }));

  const leading = withPercent.reduce(
    (max, opt) => (opt.votes > max.votes ? opt : max),
    withPercent[0]
  );

  let cumulativePercent = 0;
  const gradientStops = withPercent
    .filter((opt) => opt.votes > 0)
    .map((opt, i) => {
      const start = cumulativePercent;
      cumulativePercent += opt.percent;
      const color = DONUT_COLORS[i % DONUT_COLORS.length];
      return `${color} ${start}% ${cumulativePercent}%`;
    });
  const donutStyle = { background: `conic-gradient(${gradientStops.join(", ")})` };

  return (
    <div className="poll-results-panel">
      <div className="leading-option-banner">
        <span className="trophy-icon">🏆</span>
        <div>
          <strong>Leading Option: {leading.title}</strong>
          <p>
            Based on {totalVotes} total vote{totalVotes !== 1 ? "s" : ""}, {leading.title} is
            leading with {leading.percent}% of the network consensus.
          </p>
        </div>
      </div>

      <div className="poll-results-grid">
        <div className="vote-breakdown-card">
          <h3>Vote Breakdown</h3>
          {withPercent.map((opt) => (
            <div className="vote-breakdown-row" key={opt.id}>
              <div className="vote-breakdown-label">
                <span>
                  {opt.title} {opt.id === leading.id && opt.votes > 0 && "✓"}
                </span>
                <span>
                  {opt.votes} vote{opt.votes !== 1 ? "s" : ""} ({opt.percent}%)
                </span>
              </div>
              <div className="vote-breakdown-bar-track">
                <div className="vote-breakdown-bar-fill" style={{ width: `${opt.percent}%` }} />
              </div>
            </div>
          ))}
        </div>

        <div className="consensus-share-card">
          <h3>Consensus Share</h3>
          <div className="donut-chart" style={donutStyle} />
          <div className="donut-legend">
            {withPercent
              .filter((opt) => opt.votes > 0)
              .map((opt, i) => (
                <div className="donut-legend-item" key={opt.id}>
                  <span
                    className="legend-dot"
                    style={{ background: DONUT_COLORS[i % DONUT_COLORS.length] }}
                  />
                  {opt.title}
                </div>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PollResultsPanel({ decision, pollOpen, refreshTick }) {
  if (decision.votingType === "RATING_BASED") {
    return (
      <RatingResults decisionId={decision.id} pollOpen={pollOpen} refreshTick={refreshTick} />
    );
  }
  return <VoteCountResults decision={decision} />;
}
