import { useState, useEffect } from "react";
import { getRanking } from "../services/voteService";

// Confirmed from the real API response: { decisionId, decisionTitle, options: [...], status }
const getOptionLabel = (r) => r.optionTitle;
const getOptionKey = (r) => r.optionId;
const getScore = (r) => r.score ?? 0;

// Shared results view for both RATING_BASED (points) and SINGLE/MULTIPLE_CHOICE
// (vote counts) — normalized to { key, label, value } so both share one design.
// Signature idea: instead of a donut + separate bar list (which reads the same
// as any other dashboard), lead with what a decision actually needs to answer —
// how decisively did the group land on an option? The headline is the margin
// between 1st and 2nd, and each ranked lane carries a 50% majority tick so you
// can tell a true majority from a narrow plurality at a glance.
function ResultsCard({ items, unit, emptyLabel }) {
  const total = items.reduce((sum, item) => sum + item.value, 0);

  if (items.length === 0 || total <= 0) {
    return (
      <div className="poll-results-empty">
        <p>{emptyLabel}</p>
      </div>
    );
  }

  const sorted = [...items].sort((a, b) => b.value - a.value);
  const leader = sorted[0];
  const runnerUp = sorted[1];
  const leaderPercent = (leader.value / total) * 100;
  const margin = runnerUp ? leader.value - runnerUp.value : leader.value;
  const isMajority = leaderPercent >= 50;

  const formatValue = (v) => (Number.isInteger(v) ? v : v.toFixed(1));

  return (
    <div className="results-card">
      <div className="results-headline">
        <span className="results-headline-figure">{Math.round(leaderPercent)}%</span>
        <div className="results-headline-copy">
          <p className="results-headline-lead">
            <strong>{leader.label}</strong> is ahead
            {runnerUp && (
              <> &mdash; +{formatValue(margin)} {unit} over {runnerUp.label}</>
            )}
          </p>
          <span className={`results-majority-tag ${isMajority ? "is-majority" : "is-plurality"}`}>
            {isMajority ? "Clear majority" : "Leading plurality"}
          </span>
        </div>
      </div>

      <div className="results-lanes">
        {sorted.map((item, i) => {
          const percent = (item.value / total) * 100;
          return (
            <div className={`results-lane ${i === 0 ? "is-leader" : ""}`} key={item.key}>
              <span className="results-rank">{String(i + 1).padStart(2, "0")}</span>
              <div className="results-lane-main">
                <div className="results-lane-label">
                  <span>{item.label}</span>
                  <span>
                    {formatValue(item.value)} {unit} &middot; {Math.round(percent)}%
                  </span>
                </div>
                <div className="results-lane-track">
                  <span className="results-majority-tick" />
                  <div className="results-lane-fill" style={{ width: `${percent}%` }} />
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

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

  const items = (ranking || []).map((r) => ({
    key: getOptionKey(r),
    label: getOptionLabel(r),
    value: getScore(r)
  }));

  return (
    <ResultsCard
      items={items}
      unit="pts"
      emptyLabel="No ratings have been submitted on this decision yet."
    />
  );
}

function VoteCountResults({ decision }) {
  const options = decision.options || [];
  const totalVotes = options.reduce((sum, opt) => sum + (opt.voteCount ?? 0), 0);

  const items = options.map((opt) => ({
    key: opt.id,
    label: opt.title,
    value: opt.voteCount ?? 0
  }));

  return (
    <ResultsCard
      items={items}
      unit={totalVotes === 1 ? "vote" : "votes"}
      emptyLabel="No votes have been cast on this decision yet."
    />
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
