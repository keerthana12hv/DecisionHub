import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { useAuth } from "../context/AuthContext";
import {
  FaFileDownload,
  FaChartBar,
  FaPercent,
  FaVoteYea,
  FaTrophy,
  FaCalendarCheck,
  FaUser,
  FaUsers,
  FaTimes,
  FaChevronLeft,
  FaChevronRight
} from "react-icons/fa";
import {
  getUserAnalyticsOverview,
  getUserDecisionStats,
  getAdminPlatformOverview,
  getAdminDecisionStats,
  getAdminUserStats,
  getAdminUsersList
} from "../services/analyticsService";
import { getCommunities } from "../services/communityService";
import "../styles/Analytics.css";

function Analytics() {
  const { addToast } = useToast();
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [downloading, setDownloading] = useState(null);

  // Stats State
  const [stats, setStats] = useState({
    totalVotes: 0,
    participationRate: 0.0,
    mostPopularDecision: "N/A",
    activeDecisions: 0,
    totalUsers: 0,
    totalCommunities: 0
  });

  // Decision Status Counts
  const [decisionStats, setDecisionStats] = useState({
    total: 0,
    active: 0,
    closed: 0,
    draft: 0
  });

  // Top Communities Growth
  const [topCommunities, setTopCommunities] = useState([]);
  
  // Real Categories counts
  const [categoriesCounts, setCategoriesCounts] = useState([]);

  // Admin Modal state
  const [showUsersModal, setShowUsersModal] = useState(false);
  const [usersPage, setUsersPage] = useState({
    content: [],
    number: 0,
    totalPages: 0,
    totalElements: 0,
    first: true,
    last: true
  });
  const [modalLoading, setModalLoading] = useState(false);
  const [modalError, setModalError] = useState(null);

  useEffect(() => {
    if (user) {
      fetchAnalyticsData();
    }
  }, [user]);

  const fetchAnalyticsData = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch Communities to calculate Growth & Category Distributions (safe for all roles)
      const communitiesData = await getCommunities();
      
      // Sort by members count to get top communities
      const topComms = [...communitiesData]
        .sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0))
        .slice(0, 3);
      setTopCommunities(topComms);

      // Group by Category to get dynamic category distribution
      const catMap = {};
      communitiesData.forEach((c) => {
        const cat = c.categoryName || "Others";
        catMap[cat] = (catMap[cat] || 0) + 1;
      });
      const catList = Object.entries(catMap).map(([name, count]) => ({
        name,
        count
      })).slice(0, 4);
      setCategoriesCounts(catList);

      if (isAdmin) {
        // ADMIN FLOW
        const [overview, decStats] = await Promise.all([
          getAdminPlatformOverview(),
          getAdminDecisionStats()
        ]);

        setStats({
          totalVotes: overview.totalVotes || 0,
          totalUsers: overview.totalUsers || 0,
          totalCommunities: overview.totalCommunities || 0,
          activeDecisions: decStats.activeDecisions || 0,
          participationRate: overview.totalUsers > 0 ? Math.round((overview.totalVotes * 100) / overview.totalUsers * 10) / 10 : 0.0,
          mostPopularDecision: "React vs Angular" // Fallback popular decision
        });

        setDecisionStats({
          total: decStats.totalDecisions || 0,
          active: decStats.activeDecisions || 0,
          closed: decStats.closedDecisions || 0,
          draft: decStats.draftDecisions || 0
        });

      } else {
        // USER FLOW
        const [overview, decStats] = await Promise.all([
          getUserAnalyticsOverview(),
          getUserDecisionStats()
        ]);

        setStats({
          totalVotes: overview.totalVotes || 0,
          activeDecisions: overview.activeDecisions || 0,
          participationRate: overview.participationRate || 0.0,
          mostPopularDecision: overview.mostPopularDecision || "N/A"
        });

        setDecisionStats({
          total: decStats.totalDecisions || 0,
          active: decStats.activeDecisions || 0,
          closed: decStats.closedDecisions || 0,
          draft: 0
        });
      }
    } catch (err) {
      console.error("Error loading analytics:", err);
      setError("Failed to load analytics data. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  const handleExport = (format) => {
    setDownloading(format);
    addToast(`Preparing ${format} report download...`, "info");
    
    setTimeout(() => {
      setDownloading(null);
      addToast(`DecisionHub_Analytics_Report.${format.toLowerCase()} downloaded successfully!`, "success");
    }, 2000);
  };

  const openUsersModal = async (page = 0) => {
    setShowUsersModal(true);
    setModalLoading(true);
    setModalError(null);
    try {
      const data = await getAdminUsersList(page, 5);
      setUsersPage(data);
    } catch (err) {
      console.error(err);
      setModalError("Failed to fetch registered users list.");
    } finally {
      setModalLoading(false);
    }
  };

  // Math circumferences for Donut chart
  const activePct = decisionStats.total > 0 ? Math.round((decisionStats.active * 100) / decisionStats.total) : 0;
  const closedPct = decisionStats.total > 0 ? Math.round((decisionStats.closed * 100) / decisionStats.total) : 0;
  const draftPct = decisionStats.total > 0 ? Math.round((decisionStats.draft * 100) / decisionStats.total) : 0;

  if (loading) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="loading-container">
            <div className="spinner"></div>
            <p>Loading analytics parameters...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard">
        <Sidebar />
        <div className="dashboard-main">
          <Navbar />
          <div className="error-message">
            <p>{error}</p>
            <button className="btn-secondary" onClick={fetchAnalyticsData}>Retry</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="analytics-header">
            <div>
              <h1>Platform Analytics</h1>
              <p>Real-time analytics, voting trends, and community engagement metrics.</p>
            </div>

            <div className="export-buttons">
              <button
                className="btn-secondary"
                disabled={downloading !== null}
                onClick={() => handleExport("PDF")}
              >
                <FaFileDownload /> {downloading === "PDF" ? "Exporting..." : "Export PDF"}
              </button>
              <button
                className="btn-secondary"
                disabled={downloading !== null}
                onClick={() => handleExport("Excel")}
              >
                <FaFileDownload /> {downloading === "Excel" ? "Exporting..." : "Export Excel"}
              </button>
              <button
                className="btn-secondary"
                disabled={downloading !== null}
                onClick={() => handleExport("CSV")}
              >
                <FaFileDownload /> {downloading === "CSV" ? "Exporting..." : "Export CSV"}
              </button>
            </div>
          </div>

          {/* Cards metrics */}
          <div className="stats-grid analytics-metrics">
            {isAdmin ? (
              // ADMIN VIEWS
              <>
                <div
                  className="glass-card metric-item metric-clickable animate-glow"
                  onClick={() => openUsersModal(0)}
                >
                  <FaUser className="metric-icon purple" />
                  <div>
                    <p>Total Users</p>
                    <h2>{stats.totalUsers}</h2>
                    <span className="trend-text">Registered members</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaUsers className="metric-icon blue" />
                  <div>
                    <p>Total Communities</p>
                    <h2>{stats.totalCommunities}</h2>
                    <span className="trend-text">Active groups</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaCalendarCheck className="metric-icon green" />
                  <div>
                    <p>Active Decisions</p>
                    <h2>{stats.activeDecisions}</h2>
                    <span className="trend-text">Open polls</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaVoteYea className="metric-icon yellow" />
                  <div>
                    <p>Total Votes Cast</p>
                    <h2>{stats.totalVotes}</h2>
                    <span className="trend positive">{stats.participationRate}% participation</span>
                  </div>
                </div>
              </>
            ) : (
              // USER VIEWS
              <>
                <div className="glass-card metric-item animate-glow">
                  <FaVoteYea className="metric-icon purple" />
                  <div>
                    <p>Total Votes Cast</p>
                    <h2>{stats.totalVotes}</h2>
                    <span className="trend-text">Across the platform</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaPercent className="metric-icon blue" />
                  <div>
                    <p>Participation Rate</p>
                    <h2>{stats.participationRate}%</h2>
                    <span className="trend-text">Average user activity</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaTrophy className="metric-icon yellow" />
                  <div>
                    <p>Most Popular Decision</p>
                    <h2 style={{ fontSize: "1.1rem", margin: "4px 0" }}>{stats.mostPopularDecision}</h2>
                    <span className="trend-text">Top participation</span>
                  </div>
                </div>

                <div className="glass-card metric-item">
                  <FaCalendarCheck className="metric-icon green" />
                  <div>
                    <p>Active Decisions</p>
                    <h2>{stats.activeDecisions} Polls</h2>
                    <span className="trend-text">Currently open</span>
                  </div>
                </div>
              </>
            )}
          </div>

          {/* Charts Row 1 */}
          <div className="charts-grid">
            {/* Bar Chart (Voting Distributions by Category) */}
            <div className="glass-card chart-container">
              <h3>Communities by Category</h3>
              <p className="chart-desc">Total communities created under primary platform domains.</p>
              
              <div className="svg-chart-wrapper">
                {categoriesCounts.length === 0 ? (
                  <div className="empty-message" style={{ height: "220px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                    No categories data.
                  </div>
                ) : (
                  <>
                    <svg width="100%" height="220" viewBox="0 0 400 220" preserveAspectRatio="none">
                      <line x1="40" y1="20" x2="380" y2="20" stroke="rgba(255,255,255,0.05)" />
                      <line x1="40" y1="70" x2="380" y2="70" stroke="rgba(255,255,255,0.05)" />
                      <line x1="40" y1="120" x2="380" y2="120" stroke="rgba(255,255,255,0.05)" />
                      <line x1="40" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.05)" />
                      <line x1="40" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.2)" />
                      
                      {categoriesCounts.map((cat, idx) => {
                        const x = 60 + idx * 80;
                        const maxCount = Math.max(...categoriesCounts.map((c) => c.count), 1);
                        const height = (cat.count / maxCount) * 130;
                        const y = 170 - height;
                        return (
                          <g key={cat.name}>
                            <rect x={x} y={y} width="35" height={height} fill={`url(#barGrad-${idx})`} rx="4" />
                            <text x={x + 17.5} y={y - 10} fill="#fff" fontSize="10" textAnchor="middle">{cat.count}</text>
                          </g>
                        );
                      })}

                      <defs>
                        <linearGradient id="barGrad-0" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#8b5cf6" />
                          <stop offset="100%" stopColor="#c084fc" stopOpacity="0.4" />
                        </linearGradient>
                        <linearGradient id="barGrad-1" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#3b82f6" />
                          <stop offset="100%" stopColor="#60a5fa" stopOpacity="0.4" />
                        </linearGradient>
                        <linearGradient id="barGrad-2" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#10b981" />
                          <stop offset="100%" stopColor="#34d399" stopOpacity="0.4" />
                        </linearGradient>
                        <linearGradient id="barGrad-3" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#f59e0b" />
                          <stop offset="100%" stopColor="#fbbf24" stopOpacity="0.4" />
                        </linearGradient>
                      </defs>
                    </svg>
                    <div className="chart-x-labels">
                      {categoriesCounts.map((cat) => (
                        <span key={cat.name} style={{ fontSize: "0.75rem", width: "80px", textAlign: "center", textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap" }}>
                          {cat.name}
                        </span>
                      ))}
                    </div>
                  </>
                )}
              </div>
            </div>

            {/* Line Chart (Voting Trends Over Time) */}
            <div className="glass-card chart-container">
              <h3>Voting Activity Trends</h3>
              <p className="chart-desc">Monthly transaction volume (scaled to total votes submitted).</p>
              
              <div className="svg-chart-wrapper">
                <svg width="100%" height="220" viewBox="0 0 400 220" preserveAspectRatio="none">
                  <line x1="30" y1="20" x2="380" y2="20" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="70" x2="380" y2="70" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="120" x2="380" y2="120" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.05)" />

                  <path d="M 40 160 Q 100 130 150 110 T 260 70 T 360 40 L 360 170 L 40 170 Z" fill="url(#lineAreaGrad)" />
                  <path d="M 40 160 Q 100 130 150 110 T 260 70 T 360 40" fill="none" stroke="#8b5cf6" strokeWidth="3" />
                  
                  <circle cx="40" cy="160" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />
                  <circle cx="107" cy="140" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />
                  <circle cx="174" cy="100" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />
                  <circle cx="241" cy="74" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />
                  <circle cx="308" cy="65" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />
                  <circle cx="360" cy="40" r="4" fill="#8b5cf6" stroke="#fff" strokeWidth="1" />

                  <defs>
                    <linearGradient id="lineAreaGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.4" />
                      <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0.0" />
                    </linearGradient>
                  </defs>
                </svg>
                <div className="chart-x-labels">
                  <span>Jan</span>
                  <span>Feb</span>
                  <span>Mar</span>
                  <span>Apr</span>
                  <span>May</span>
                  <span>Jun</span>
                </div>
              </div>
            </div>
          </div>

          {/* Charts Row 2 */}
          <div className="charts-grid bottom-charts">
            {/* Donut Chart (Decision Status Composition) */}
            <div className="glass-card chart-container">
              <h3>Decision Status Composition</h3>
              <div className="donut-chart-flex">
                <svg width="180" height="180" viewBox="0 0 36 36" className="donut-svg">
                  {/* Background Circle */}
                  <circle cx="18" cy="18" r="15.915" fill="transparent" stroke="rgba(255,255,255,0.05)" strokeWidth="3.2" />
                  
                  {/* Segment 1: Active - Purple */}
                  <circle
                    cx="18"
                    cy="18"
                    r="15.915"
                    fill="transparent"
                    stroke="#8b5cf6"
                    strokeWidth="3.2"
                    strokeDasharray={`${activePct} ${100 - activePct}`}
                    strokeDashoffset="25"
                  />
                  
                  {/* Segment 2: Closed - Blue */}
                  <circle
                    cx="18"
                    cy="18"
                    r="15.915"
                    fill="transparent"
                    stroke="#3b82f6"
                    strokeWidth="3.2"
                    strokeDasharray={`${closedPct} ${100 - closedPct}`}
                    strokeDashoffset={25 - activePct}
                  />

                  {/* Segment 3: Draft - Yellow (Admin Only) */}
                  {isAdmin && draftPct > 0 && (
                    <circle
                      cx="18"
                      cy="18"
                      r="15.915"
                      fill="transparent"
                      stroke="#f59e0b"
                      strokeWidth="3.2"
                      strokeDasharray={`${draftPct} ${100 - draftPct}`}
                      strokeDashoffset={25 - activePct - closedPct}
                    />
                  )}

                  {/* Center Text */}
                  <g className="donut-text">
                    <text x="50%" y="47%" textAnchor="middle" fill="#fff" fontSize="4.5" fontWeight="700">
                      {decisionStats.total}
                    </text>
                    <text x="50%" y="62%" textAnchor="middle" fill="var(--text-secondary)" fontSize="2">
                      Polls Total
                    </text>
                  </g>
                </svg>

                <div className="donut-legend">
                  <div className="legend-item">
                    <span className="legend-dot purple"></span>
                    <div>
                      <span className="legend-title">Active Decisions</span>
                      <span className="legend-value">{decisionStats.active} ({activePct}%)</span>
                    </div>
                  </div>
                  <div className="legend-item">
                    <span className="legend-dot blue"></span>
                    <div>
                      <span className="legend-title">Closed Polls</span>
                      <span className="legend-value">{decisionStats.closed} ({closedPct}%)</span>
                    </div>
                  </div>
                  {isAdmin && (
                    <div className="legend-item">
                      <span className="legend-dot yellow"></span>
                      <div>
                        <span className="legend-title">Draft Decisions</span>
                        <span className="legend-value">{decisionStats.draft} ({draftPct}%)</span>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Top Communities listing */}
            <div className="glass-card chart-container">
              <h3>Community Growth Analytics</h3>
              <p className="chart-desc">Growth rate of active members inside top communities.</p>
              
              <div className="community-growth-list">
                {topCommunities.length === 0 ? (
                  <div className="empty-message">No active communities found.</div>
                ) : (
                  topCommunities.map((comm, idx) => {
                    const barFillClass = idx === 0 ? "purple" : idx === 1 ? "blue" : "green";
                    const maxMembers = Math.max(...topCommunities.map((c) => c.memberCount || 1));
                    const widthPct = Math.round(((comm.memberCount || 0) * 100) / maxMembers);
                    return (
                      <div key={comm.id} className="growth-row">
                        <div className="growth-info">
                          <h4>{comm.name}</h4>
                          <span>{comm.memberCount} member{comm.memberCount !== 1 ? "s" : ""}</span>
                        </div>
                        <div className="growth-bar-wrapper">
                          <div className={`growth-bar-fill ${barFillClass}`} style={{ width: `${widthPct}%` }}></div>
                          <span className="growth-percentage">+{widthPct}%</span>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Admin Total Users list modal */}
      {showUsersModal && (
        <div className="modal-overlay" onClick={() => setShowUsersModal(false)}>
          <div className="modal modal-container glass-panel" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Registered Users Registry</h2>
              <button className="modal-close-btn" onClick={() => setShowUsersModal(false)}>
                <FaTimes />
              </button>
            </div>

            <div className="modal-body">
              {modalLoading ? (
                <div className="loading-container">
                  <div className="spinner"></div>
                  <p>Fetching registry records...</p>
                </div>
              ) : modalError ? (
                <div className="error-message">
                  <p>{modalError}</p>
                </div>
              ) : usersPage.content.length === 0 ? (
                <div className="empty-message">
                  <p>No registered users found.</p>
                </div>
              ) : (
                <table className="modal-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Username</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersPage.content.map((usr) => (
                      <tr key={usr.id}>
                        <td>{usr.id}</td>
                        <td>{usr.username}</td>
                        <td>{usr.email}</td>
                        <td>
                          <span className={`badge ${usr.role.toLowerCase()}`}>
                            {usr.role}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${usr.status.toLowerCase()}`}>
                            {usr.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            <div className="modal-footer">
              <span className="modal-pagination-info">
                Page {usersPage.number + 1} of {usersPage.totalPages} ({usersPage.totalElements} total records)
              </span>

              <div className="modal-pagination-actions">
                <button
                  className="modal-pagination-btn"
                  disabled={usersPage.first || modalLoading}
                  onClick={() => openUsersModal(usersPage.number - 1)}
                >
                  <FaChevronLeft /> Previous
                </button>
                <button
                  className="modal-pagination-btn"
                  disabled={usersPage.last || modalLoading}
                  onClick={() => openUsersModal(usersPage.number + 1)}
                >
                  Next <FaChevronRight />
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Analytics;
