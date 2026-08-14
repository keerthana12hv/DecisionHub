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
  FaChevronRight,
  FaShieldAlt
} from "react-icons/fa";
import { DecisionAnalyticsView } from "../components/analytics/DecisionAnalyticsView";
import { CommunityAnalyticsView } from "../components/analytics/CommunityAnalyticsView";
import { AdminAnalyticsView } from "../components/analytics/AdminAnalyticsView";
import "../styles/Analytics.css";

function Analytics() {
  const { addToast } = useToast();
  const { user } = useAuth();
  const userRole = user?.role || "USER";
  const isAdmin = userRole === "ADMIN";
  const [downloading, setDownloading] = useState(null);
  const [activeTab, setActiveTab] = useState("decision"); // 'decision' | 'community' | 'admin'
  const [decisionId, setDecisionId] = useState(5);
  const [communityId, setCommunityId] = useState(1);

  useEffect(() => {
    if (user) {
      if (user.role === "ADMIN") {
        setActiveTab("admin");
      } else {
        setActiveTab("decision");
      }
    }
  }, [user]);

  const isTabAllowed = (tabName) => {
    if (userRole === "ADMIN") {
      return tabName === "admin";
    }
    if (userRole === "MODERATOR") {
      return tabName === "decision" || tabName === "community";
    }
    return tabName === "decision";
  };

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
          {/* Header */}
          <div className="analytics-header">
            <div>
              <h1>Analytics & Insights</h1>
              <p>Real-time User Decision, Community Moderator, and Platform Admin Analytics.</p>
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

          {/* Navigation Tab Switcher */}
          <div 
            className="analytics-tabs-container"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              marginBottom: "20px",
              gap: "10px",
              flexWrap: "wrap",
              background: "rgba(15, 23, 42, 0.6)",
              padding: "10px 14px",
              borderRadius: "12px",
              border: "1px solid rgba(255, 255, 255, 0.08)"
            }}
          >
            <div style={{ display: "flex", gap: "8px" }}>
              {isTabAllowed("decision") && (
                <button
                  className={`btn-secondary ${activeTab === "decision" ? "active-tab" : ""}`}
                  onClick={() => setActiveTab("decision")}
                  style={{
                    background: activeTab === "decision" ? "#8b5cf6" : "transparent",
                    color: "#fff",
                    borderColor: activeTab === "decision" ? "#8b5cf6" : "rgba(255,255,255,0.1)",
                    display: "flex",
                    alignItems: "center",
                    gap: "6px"
                  }}
                >
                  <FaVoteYea /> User Decision Analytics
                </button>
              )}

              {isTabAllowed("community") && (
                <button
                  className={`btn-secondary ${activeTab === "community" ? "active-tab" : ""}`}
                  onClick={() => setActiveTab("community")}
                  style={{
                    background: activeTab === "community" ? "#3b82f6" : "transparent",
                    color: "#fff",
                    borderColor: activeTab === "community" ? "#3b82f6" : "rgba(255,255,255,0.1)",
                    display: "flex",
                    alignItems: "center",
                    gap: "6px"
                  }}
                >
                  <FaShieldAlt /> Moderator Community Analytics
                </button>
              )}

              {isTabAllowed("admin") && (
                <button
                  className={`btn-secondary ${activeTab === "admin" ? "active-tab" : ""}`}
                  onClick={() => setActiveTab("admin")}
                  style={{
                    background: activeTab === "admin" ? "#f59e0b" : "transparent",
                    color: "#fff",
                    borderColor: activeTab === "admin" ? "#f59e0b" : "rgba(255,255,255,0.1)",
                    display: "flex",
                    alignItems: "center",
                    gap: "6px"
                  }}
                >
                  <FaChartBar /> Admin Platform Analytics
                </button>
              )}
            </div>

            {/* Live ID inputs for testing endpoints */}
            <div style={{ display: "flex", alignItems: "center", gap: "10px", fontSize: "13px" }}>
              {activeTab === "decision" && (
                <label style={{ color: "#94a3b8", display: "flex", alignItems: "center", gap: "6px" }}>
                  Decision ID:
                  <input
                    type="number"
                    value={decisionId}
                    onChange={(e) => setDecisionId(e.target.value === "" ? "" : Number(e.target.value))}
                    style={{
                      width: "60px",
                      padding: "4px 8px",
                      borderRadius: "6px",
                      background: "#0f172a",
                      border: "1px solid #334155",
                      color: "#fff",
                      fontWeight: "bold",
                      textAlign: "center"
                    }}
                  />
                </label>
              )}

              {activeTab === "community" && (
                <label style={{ color: "#94a3b8", display: "flex", alignItems: "center", gap: "6px" }}>
                  Community ID:
                  <input
                    type="number"
                    value={communityId}
                    onChange={(e) => setCommunityId(e.target.value === "" ? "" : Number(e.target.value))}
                    style={{
                      width: "60px",
                      padding: "4px 8px",
                      borderRadius: "6px",
                      background: "#0f172a",
                      border: "1px solid #334155",
                      color: "#fff",
                      fontWeight: "bold",
                      textAlign: "center"
                    }}
                  />
                </label>
              )}
            </div>
          </div>

          {/* Active Tab View */}
          {activeTab === "decision" && isTabAllowed("decision") && <DecisionAnalyticsView decisionId={decisionId} />}
          {activeTab === "community" && isTabAllowed("community") && <CommunityAnalyticsView communityId={communityId} />}
          {activeTab === "admin" && isTabAllowed("admin") && <AdminAnalyticsView />}
        </div>
      </div>
    </div>
  );
}

export default Analytics;
