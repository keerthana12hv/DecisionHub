import { useEffect, useState, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import {
  FaFileDownload, FaPercent, FaVoteYea,
  FaTrophy, FaCalendarCheck, FaSync, FaUsers,
} from "react-icons/fa";
import ComparisonDashboard from "../components/ComparisonDashboard";
import VotingInsights from "../components/VotingInsights";
import api from "../services/api";
import "../styles/Analytics.css";

// ─── helpers ──────────────────────────────────────────────────────────────────

/** Total votes across all options of a decision (choice-based: voteCount; rating-based: scores) */
const decisionVoteTotal = (decision) =>
  (decision.options ?? []).reduce((sum, o) => {
    const choiceVotes  = o.voteCount ?? 0;
    const ratingVoters = new Set((o.comparisonScores ?? []).map((s) => s.userId)).size;
    return sum + (choiceVotes > 0 ? choiceVotes : ratingVoters);
  }, 0);

/** Bar chart bar height (px) relative to max value, range 10–140 */
const barHeight = (val, max) => (max === 0 ? 10 : Math.round(10 + (val / max) * 130));

const COLORS = ["#8b5cf6", "#3b82f6", "#10b981", "#f59e0b", "#ec4899", "#06b6d4"];

// ─── Analytics page ───────────────────────────────────────────────────────────

export default function Analytics() {
  const { addToast } = useToast();

  const [decisions,    setDecisions]    = useState([]);
  const [communities,  setCommunities]  = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [lastRefresh,  setLastRefresh]  = useState(null);
  const [downloading,  setDownloading]  = useState(null);

  // ── fetch all data ──────────────────────────────────────────────────────────
  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [decRes, commRes] = await Promise.all([
        api.get("/api/decisions"),
        api.get("/api/communities").catch(() => ({ data: [] })),
      ]);
      setDecisions(decRes.data ?? []);
      setCommunities(commRes.data ?? []);
      setLastRefresh(new Date());
    } catch (err) {
      addToast("Failed to load analytics data.", "error");
    } finally {
      setLoading(false);
    }
  }, [addToast]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  // ── derived metrics ─────────────────────────────────────────────────────────
  const totalDecisions   = decisions.length;
  const activeDecisions  = decisions.filter((d) => d.status === "ACTIVE");
  const closedDecisions  = decisions.filter((d) => d.status === "CLOSED");

  // Total votes = sum of voteCount (choice) + unique raters (rating)
  const totalVotes = decisions.reduce((sum, d) => sum + decisionVoteTotal(d), 0);

  // Most voted decision
  const mostVotedDecision = [...decisions].sort(
    (a, b) => decisionVoteTotal(b) - decisionVoteTotal(a)
  )[0];

  // Participation rate = decisions with at least 1 vote / total active decisions
  const activeWithVotes = activeDecisions.filter((d) => decisionVoteTotal(d) > 0).length;
  const participationRate = activeDecisions.length === 0
    ? 0
    : Math.round((activeWithVotes / activeDecisions.length) * 100);

  // Vote distribution per voting type (for bar chart)
  const typeGroups = { SINGLE_CHOICE: 0, MULTIPLE_CHOICE: 0, RATING_BASED: 0 };
  for (const d of decisions) {
    typeGroups[d.votingType] = (typeGroups[d.votingType] ?? 0) + decisionVoteTotal(d);
  }
  const typeLabels = {
    SINGLE_CHOICE:   "Single",
    MULTIPLE_CHOICE: "Multiple",
    RATING_BASED:    "Rating",
  };

  // Top 5 decisions by vote count (for bar chart)
  const topDecisions = [...decisions]
    .sort((a, b) => decisionVoteTotal(b) - decisionVoteTotal(a))
    .slice(0, 5);
  const maxTopVotes = Math.max(...topDecisions.map(decisionVoteTotal), 1);

  // Donut chart — decision status breakdown (Active vs Closed only)
  const donutTotal  = Math.max(activeDecisions.length + closedDecisions.length, 1);
  const activePct   = Math.round((activeDecisions.length / donutTotal) * 100);
  const closedPct   = 100 - activePct;

  // Donut strokeDasharray helpers (circumference ≈ 100 for r=15.915)
  const donutOffset = (idx) => {
    const pcts = [activePct, closedPct];
    return -1 * pcts.slice(0, idx).reduce((s, p) => s + p, 0) + 25;
  };

  // Community top list by member count
  const topCommunities = [...communities]
    .sort((a, b) => (b.memberCount ?? 0) - (a.memberCount ?? 0))
    .slice(0, 5);
  const maxCommunityMembers = Math.max(...topCommunities.map((c) => c.memberCount ?? 0), 1);

  // Decisions created per voting type (for type distribution bar chart)
  const typeCounts = {
    SINGLE_CHOICE:   decisions.filter((d) => d.votingType === "SINGLE_CHOICE").length,
    MULTIPLE_CHOICE: decisions.filter((d) => d.votingType === "MULTIPLE_CHOICE").length,
    RATING_BASED:    decisions.filter((d) => d.votingType === "RATING_BASED").length,
  };
  const maxTypeCount = Math.max(...Object.values(typeCounts), 1);

  // ── export (mock — no backend endpoint yet) ─────────────────────────────────
  const handleExport = (format) => {
    setDownloading(format);
    addToast(`Preparing ${format} export…`, "info");
    setTimeout(() => {
      setDownloading(null);
      addToast(`Export ready (feature coming soon).`, "success");
    }, 1500);
  };

  // ── render ──────────────────────────────────────────────────────────────────
  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">

          {/* ── Header ── */}
          <div className="analytics-header">
            <div>
              <h1>Platform Analytics</h1>
              <p>
                Real-time decision, voting, and community metrics pulled
                directly from the database.
                {lastRefresh && (
                  <span className="an-refresh-ts">
                    {" "}· Updated {lastRefresh.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                  </span>
                )}
              </p>
            </div>
            <div className="analytics-header-right">
              <button
                className="vp-refresh-btn"
                onClick={fetchAll}
                disabled={loading}
                title="Refresh"
              >
                <FaSync className={loading ? "spin" : ""} /> Refresh
              </button>
              <div className="export-buttons">
                {["PDF", "Excel", "CSV"].map((fmt) => (
                  <button
                    key={fmt}
                    className="btn-secondary"
                    disabled={downloading !== null}
                    onClick={() => handleExport(fmt)}
                  >
                    <FaFileDownload />
                    {downloading === fmt ? "Exporting…" : fmt}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* ── KPI Cards ── */}
          <div className="stats-grid analytics-metrics">
            <div className="glass-card metric-item animate-glow">
              <FaVoteYea className="metric-icon purple" />
              <div>
                <p>Total Votes Cast</p>
                <h2>{loading ? "…" : totalVotes.toLocaleString()}</h2>
                <span className="trend-text">
                  across {totalDecisions} decision{totalDecisions !== 1 ? "s" : ""}
                </span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaPercent className="metric-icon blue" />
              <div>
                <p>Participation Rate</p>
                <h2>{loading ? "…" : `${participationRate}%`}</h2>
                <span className="trend-text">
                  {activeWithVotes} of {activeDecisions.length} active polls have votes
                </span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaTrophy className="metric-icon yellow" />
              <div>
                <p>Most Voted Decision</p>
                <h2 className="metric-h2-sm">
                  {loading ? "…" : (mostVotedDecision?.title ?? "None yet")}
                </h2>
                <span className="trend-text">
                  {mostVotedDecision
                    ? `${decisionVoteTotal(mostVotedDecision)} total votes`
                    : "No votes recorded"}
                </span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaCalendarCheck className="metric-icon green" />
              <div>
                <p>Active Decisions</p>
                <h2>{loading ? "…" : activeDecisions.length}</h2>
                <span className="trend-text">
                  {closedDecisions.length} closed
                </span>
              </div>
            </div>
          </div>

          {/* ── Charts Row 1 ── */}
          <div className="charts-grid">

            {/* Top decisions by vote count — dynamic bar chart */}
            <div className="glass-card chart-container">
              <h3>Top Decisions by Vote Count</h3>
              <p className="chart-desc">
                {loading ? "Loading…" : `${topDecisions.length} most-voted decisions on the platform.`}
              </p>
              {loading ? (
                <div className="an-chart-loading">
                  <FaSync className="spin" /> Loading chart…
                </div>
              ) : topDecisions.length === 0 ? (
                <div className="an-empty-chart">No votes recorded yet.</div>
              ) : (
                <div className="an-bar-chart">
                  {topDecisions.map((d, i) => {
                    const votes = decisionVoteTotal(d);
                    const h = barHeight(votes, maxTopVotes);
                    return (
                      <div key={d.id} className="an-bar-col">
                        <span className="an-bar-value">{votes}</span>
                        <div
                          className="an-bar"
                          style={{ height: `${h}px`, background: COLORS[i % COLORS.length] }}
                          title={`${d.title}: ${votes} votes`}
                        />
                        <span className="an-bar-label">
                          {d.title.length > 12 ? d.title.slice(0, 11) + "…" : d.title}
                        </span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Voting type distribution */}
            <div className="glass-card chart-container">
              <h3>Decisions by Voting Type</h3>
              <p className="chart-desc">How many decisions use each voting mechanism.</p>
              {loading ? (
                <div className="an-chart-loading"><FaSync className="spin" /> Loading…</div>
              ) : (
                <div className="an-type-list">
                  {Object.entries(typeCounts).map(([type, count], i) => {
                    const pct = Math.round((count / Math.max(totalDecisions, 1)) * 100);
                    return (
                      <div key={type} className="an-type-row">
                        <div className="an-type-row__header">
                          <span className={`voting-type-tag voting-type-tag--${type.toLowerCase()}`}>
                            {typeLabels[type]}
                          </span>
                          <span className="an-type-count">
                            {count} decision{count !== 1 ? "s" : ""}
                            <span className="an-type-pct"> · {pct}%</span>
                          </span>
                        </div>
                        <div className="an-type-bar-track">
                          <div
                            className="an-type-bar-fill"
                            style={{
                              width: `${pct}%`,
                              background: COLORS[i],
                            }}
                          />
                        </div>
                        <div className="an-type-votes">
                          {typeGroups[type]} total vote{typeGroups[type] !== 1 ? "s" : ""}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* ── Live widgets ── */}
          <ComparisonDashboard />
          <VotingInsights />

          {/* ── Charts Row 2 ── */}
          <div className="charts-grid bottom-charts">

            {/* Decision status donut — real data */}
            <div className="glass-card chart-container">
              <h3>Decision Status Breakdown</h3>
              {loading ? (
                <div className="an-chart-loading"><FaSync className="spin" /> Loading…</div>
              ) : totalDecisions === 0 ? (
                <div className="an-empty-chart">No decisions created yet.</div>
              ) : (
                <div className="donut-chart-flex">
                  <svg width="180" height="180" viewBox="0 0 36 36" className="donut-svg">
                    <circle cx="18" cy="18" r="15.915" fill="transparent"
                      stroke="rgba(255,255,255,0.05)" strokeWidth="3.2" />

                    {/* Active segment */}
                    <circle cx="18" cy="18" r="15.915" fill="transparent"
                      stroke="#8b5cf6" strokeWidth="3.2"
                      strokeDasharray={`${activePct} ${100 - activePct}`}
                      strokeDashoffset={donutOffset(0)} />

                    {/* Closed segment */}
                    <circle cx="18" cy="18" r="15.915" fill="transparent"
                      stroke="#3b82f6" strokeWidth="3.2"
                      strokeDasharray={`${closedPct} ${100 - closedPct}`}
                      strokeDashoffset={donutOffset(1)} />

                    <g className="donut-text">
                      <text x="50%" y="47%" textAnchor="middle" fill="#fff"
                        fontSize="4.5" fontWeight="700">
                        {activeDecisions.length + closedDecisions.length}
                      </text>
                      <text x="50%" y="62%" textAnchor="middle"
                        fill="var(--text-secondary)" fontSize="2">Total</text>
                    </g>
                  </svg>

                  <div className="donut-legend">
                    <div className="legend-item">
                      <span className="legend-dot purple" />
                      <div>
                        <span className="legend-title">Active</span>
                        <span className="legend-value">
                          {activeDecisions.length} ({activePct}%)
                        </span>
                      </div>
                    </div>
                    <div className="legend-item">
                      <span className="legend-dot blue" />
                      <div>
                        <span className="legend-title">Closed</span>
                        <span className="legend-value">
                          {closedDecisions.length} ({closedPct}%)
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Community leaderboard — real member counts */}
            <div className="glass-card chart-container">
              <h3>Top Communities by Members</h3>
              <p className="chart-desc">
                {loading
                  ? "Loading…"
                  : `${communities.length} communit${communities.length !== 1 ? "ies" : "y"} on the platform.`}
              </p>
              {loading ? (
                <div className="an-chart-loading"><FaSync className="spin" /> Loading…</div>
              ) : topCommunities.length === 0 ? (
                <div className="an-empty-chart">No communities created yet.</div>
              ) : (
                <div className="community-growth-list">
                  {topCommunities.map((c, i) => {
                    const members = c.memberCount ?? 0;
                    const pct = Math.round((members / maxCommunityMembers) * 100);
                    const barColors = ["purple", "blue", "green", "yellow", "purple"];
                    return (
                      <div key={c.id} className="growth-row">
                        <div className="growth-info">
                          <h4>{c.name}</h4>
                          <span>{members} member{members !== 1 ? "s" : ""}</span>
                        </div>
                        <div className="growth-bar-wrapper">
                          <div
                            className={`growth-bar-fill ${barColors[i % barColors.length]}`}
                            style={{ width: `${Math.max(pct, 4)}%` }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
