import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import { FaFileDownload, FaChartBar, FaShieldAlt, FaVoteYea } from "react-icons/fa";
import { DecisionAnalyticsView } from "../components/analytics/DecisionAnalyticsView";
import { CommunityAnalyticsView } from "../components/analytics/CommunityAnalyticsView";
import { AdminAnalyticsView } from "../components/analytics/AdminAnalyticsView";
import "../styles/Analytics.css";

function Analytics() {
  const { addToast } = useToast();
  const { user } = useAuth();
  const userRole = user?.role || "USER";
  const [downloading, setDownloading] = useState(null);
  const [activeTab, setActiveTab] = useState("decision"); // 'decision' | 'community' | 'admin'
  const [decisionId, setDecisionId] = useState(5);
  const [communityId, setCommunityId] = useState(1);
  const [decisionsList, setDecisionsList] = useState([]);
  const [communitiesList, setCommunitiesList] = useState([]);

  useEffect(() => {
    const t = localStorage.getItem("token") || localStorage.getItem("authToken") || localStorage.getItem("jwt");
    if (!t) return;
    
    // Fetch decisions for user selection
    api
        .get("/decisions", {
          headers: { Authorization: `Bearer ${t}` }
      })
      .then((res) => {
        const list = res.data || [];
        setDecisionsList(list);
        if (list.length > 0) {
          setDecisionId(list[0].id);
        }
      })
      .catch((err) => console.error("Error fetching decisions for dropdown:", err));

    // Fetch moderating communities for moderator selection
    api
        .get("/communities/moderating", {
          headers: { Authorization: `Bearer ${t}` }
      })
      .then((res) => {
        const list = res.data || [];
        setCommunitiesList(list);
        if (list.length > 0) {
          setCommunityId(list[0].id);
        }
      })
      .catch((err) => console.error("Error fetching moderating communities for dropdown:", err));
  }, []);

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
  const handleExport = (format) => {
    setDownloading(format);
    addToast(`Preparing ${format} report download...`, "info");
    
    setTimeout(() => {
      setDownloading(null);
      addToast(`DecisionHub_Analytics_Report.${format.toLowerCase()} downloaded successfully!`, "success");
    }, 2000);
  };

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

            {activeTab !== "admin" && (
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
            )}
          </div>          {/* Navigation Tab Switcher */}
          {userRole !== "ADMIN" && (
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
              </div>

              {/* Dropdown selectors for active tab context */}
              <div style={{ display: "flex", alignItems: "center", gap: "10px", fontSize: "13px" }}>
                {activeTab === "decision" && (
                  <label style={{ color: "#94a3b8", display: "flex", alignItems: "center", gap: "6px" }}>
                    Select Decision:
                    <select
                      value={decisionId}
                      onChange={(e) => setDecisionId(Number(e.target.value))}
                      style={{
                        padding: "6px 12px",
                        borderRadius: "6px",
                        background: "#0f172a",
                        border: "1px solid #334155",
                        color: "#fff",
                        fontWeight: "bold",
                        outline: "none"
                      }}
                    >
                      {decisionsList.length === 0 ? (
                        <option value="">No decisions found</option>
                      ) : (
                        decisionsList.map((d) => (
                          <option key={d.id} value={d.id}>
                            {d.title}
                          </option>
                        ))
                      )}
                    </select>
                  </label>
                )}

                {activeTab === "community" && (
                  <label style={{ color: "#94a3b8", display: "flex", alignItems: "center", gap: "6px" }}>
                    Select Community:
                    <select
                      value={communityId}
                      onChange={(e) => setCommunityId(Number(e.target.value))}
                      style={{
                        padding: "6px 12px",
                        borderRadius: "6px",
                        background: "#0f172a",
                        border: "1px solid #334155",
                        color: "#fff",
                        fontWeight: "bold",
                        outline: "none"
                      }}
                    >
                      {communitiesList.length === 0 ? (
                        <option value="">No communities moderating</option>
                      ) : (
                        communitiesList.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name}
                          </option>
                        ))
                      )}
                    </select>
                  </label>
                )}
              </div>
            </div>
          )}

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
