import { useState, useEffect } from "react";
import api from "../services/api";
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
  const isTie = runnerUp && leader.value === runnerUp.value;

  const formatValue = (v) => (Number.isInteger(v) ? v : v.toFixed(1));

  return (
    <div className="results-card">
      <div className="results-headline">
        <span className="results-headline-figure">{Math.round(leaderPercent)}%</span>
        <div className="results-headline-copy">
          <p className="results-headline-lead">
            {isTie ? (
              <><strong>Tied</strong> &mdash; <strong>{leader.label}</strong> and <strong>{runnerUp.label}</strong> are tied with {formatValue(leader.value)} {unit} each</>
            ) : (
              <>
                <strong>{leader.label}</strong> is ahead
                {runnerUp && (
                  <> &mdash; +{formatValue(margin)} {unit} over {runnerUp.label}</>
                )}
              </>
            )}
          </p>
          <span className={`results-majority-tag ${isTie ? "is-tie" : isMajority ? "is-majority" : "is-plurality"}`}>
            {isTie ? "Tie / No clear majority" : isMajority ? "Clear majority" : "Leading plurality"}
          </span>
        </div>
      </div>

      <div className="results-lanes">
        {sorted.map((item, i) => {
          const percent = (item.value / total) * 100;
          const isLeaderLane = i === 0 || (isTie && item.value === leader.value);
          return (
            <div className={`results-lane ${isLeaderLane ? "is-leader" : ""}`} key={item.key}>
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


function VoteCountResults({ decisionId, pollOpen }) {
  const [distribution, setDistribution] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDistribution = async (silent = false) => {
    if (!decisionId) return;
    if (!silent) setLoading(true);
    try {
      const res = await api.get(
          `/analytics/decisions/${decisionId}/distribution`
      );
      console.log("Analytics distribution response:", res.data);
      setDistribution(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error("Failed to fetch vote distribution:", err);
      setDistribution([]);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchDistribution();
  }, [decisionId]);

  useEffect(() => {
    if (!pollOpen || !decisionId) return;
    const intervalId = setInterval(() => fetchDistribution(true), 5000);
    return () => clearInterval(intervalId);
  }, [decisionId, pollOpen]);

  if (loading) return <p className="poll-results-empty">Loading results...</p>;

  const list = Array.isArray(distribution) ? distribution : [];
  const totalVotes = list.reduce((sum, d) => sum + (d.voteCount ?? 0), 0);

  const items = list.map((d) => ({
    key: d.optionId,
    label: d.optionName,
    value: d.voteCount ?? 0
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
  return <VoteCountResults decisionId={decision.id} pollOpen={pollOpen} />;
}
