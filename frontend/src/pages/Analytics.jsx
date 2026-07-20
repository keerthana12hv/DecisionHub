import { useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { useToast } from "../components/Toast";
import { FaFileDownload, FaChartBar, FaPercent, FaVoteYea, FaTrophy, FaCalendarCheck } from "react-icons/fa";
import ComparisonDashboard from "../components/ComparisonDashboard";
import VotingInsights from "../components/VotingInsights";
import "../styles/Analytics.css";

function Analytics() {
  const { addToast } = useToast();
  const [downloading, setDownloading] = useState(null);

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
            <div className="glass-card metric-item animate-glow">
              <FaVoteYea className="metric-icon purple" />
              <div>
                <p>Total Votes Cast</p>
                <h2>1,482</h2>
                <span className="trend positive">+12.5% this month</span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaPercent className="metric-icon blue" />
              <div>
                <p>Participation Rate</p>
                <h2>87.4%</h2>
                <span className="trend positive">+3.2% vs last week</span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaTrophy className="metric-icon yellow" />
              <div>
                <p>Most Popular Decision</p>
                <h2>React vs Angular</h2>
                <span className="trend-text">165 total votes</span>
              </div>
            </div>

            <div className="glass-card metric-item">
              <FaCalendarCheck className="metric-icon green" />
              <div>
                <p>Active Decisions</p>
                <h2>12 Polls</h2>
                <span className="trend-text">5 closing this week</span>
              </div>
            </div>
          </div>

          {/* Charts Row 1 */}
          <div className="charts-grid">
            {/* Bar Chart (Voting Distributions by Category) */}
            <div className="glass-card chart-container">
              <h3>Voting Distribution by Category</h3>
              <p className="chart-desc">Total votes recorded across primary platform domains.</p>
              
              <div className="svg-chart-wrapper">
                <svg width="100%" height="220" viewBox="0 0 400 220" preserveAspectRatio="none">
                  {/* Grid Lines */}
                  <line x1="40" y1="20" x2="380" y2="20" stroke="rgba(255,255,255,0.05)" />
                  <line x1="40" y1="70" x2="380" y2="70" stroke="rgba(255,255,255,0.05)" />
                  <line x1="40" y1="120" x2="380" y2="120" stroke="rgba(255,255,255,0.05)" />
                  <line x1="40" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.05)" />
                  <line x1="40" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.2)" />
                  
                  {/* Bars */}
                  {/* Category 1: Tech (120) */}
                  <rect x="60" y="40" width="35" height="130" fill="url(#purpleGrad)" rx="4" />
                  <text x="77" y="30" fill="#fff" fontSize="10" textAnchor="middle">120</text>
                  
                  {/* Category 2: Education (85) */}
                  <rect x="140" y="75" width="35" height="95" fill="url(#blueGrad)" rx="4" />
                  <text x="157" y="65" fill="#fff" fontSize="10" textAnchor="middle">85</text>
                  
                  {/* Category 3: Travel (62) */}
                  <rect x="220" y="100" width="35" height="70" fill="url(#greenGrad)" rx="4" />
                  <text x="237" y="90" fill="#fff" fontSize="10" textAnchor="middle">62</text>
                  
                  {/* Category 4: Business (45) */}
                  <rect x="300" y="120" width="35" height="50" fill="url(#yellowGrad)" rx="4" />
                  <text x="317" y="110" fill="#fff" fontSize="10" textAnchor="middle">45</text>

                  {/* Gradients */}
                  <defs>
                    <linearGradient id="purpleGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#8b5cf6" />
                      <stop offset="100%" stopColor="#c084fc" stopOpacity="0.4" />
                    </linearGradient>
                    <linearGradient id="blueGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#3b82f6" />
                      <stop offset="100%" stopColor="#60a5fa" stopOpacity="0.4" />
                    </linearGradient>
                    <linearGradient id="greenGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#10b981" />
                      <stop offset="100%" stopColor="#34d399" stopOpacity="0.4" />
                    </linearGradient>
                    <linearGradient id="yellowGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#f59e0b" />
                      <stop offset="100%" stopColor="#fbbf24" stopOpacity="0.4" />
                    </linearGradient>
                  </defs>
                </svg>
                <div className="chart-x-labels">
                  <span>Technology</span>
                  <span>Education</span>
                  <span>Travel</span>
                  <span>Business</span>
                </div>
              </div>
            </div>

            {/* Line Chart (Voting Trends Over Time) */}
            <div className="glass-card chart-container">
              <h3>Voting Activity Trends</h3>
              <p className="chart-desc">Monthly transaction volume (total votes submitted).</p>
              
              <div className="svg-chart-wrapper">
                <svg width="100%" height="220" viewBox="0 0 400 220" preserveAspectRatio="none">
                  {/* Grid Lines */}
                  <line x1="30" y1="20" x2="380" y2="20" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="70" x2="380" y2="70" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="120" x2="380" y2="120" stroke="rgba(255,255,255,0.05)" />
                  <line x1="30" y1="170" x2="380" y2="170" stroke="rgba(255,255,255,0.05)" />

                  {/* Gradient Area under curve */}
                  <path d="M 40 160 Q 100 130 150 110 T 260 70 T 360 40 L 360 170 L 40 170 Z" fill="url(#lineAreaGrad)" />

                  {/* Trend line */}
                  <path d="M 40 160 Q 100 130 150 110 T 260 70 T 360 40" fill="none" stroke="#8b5cf6" strokeWidth="3" />
                  
                  {/* Interactive Nodes */}
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

          <ComparisonDashboard />
          <VotingInsights />

          {/* Charts Row 2 */}
          <div className="charts-grid bottom-charts">
            {/* Donut Chart (Decision Status Composition) */}
            <div className="glass-card chart-container">
              <h3>Decision Status Composition</h3>
              <div className="donut-chart-flex">
                <svg width="180" height="180" viewBox="0 0 36 36" className="donut-svg">
                  {/* Background Circle */}
                  <circle cx="18" cy="18" r="15.915" fill="transparent" stroke="rgba(255,255,255,0.05)" strokeWidth="3.2" />
                  
                  {/* Segment 1: Active - Purple (58%) */}
                  <circle
                    cx="18"
                    cy="18"
                    r="15.915"
                    fill="transparent"
                    stroke="#8b5cf6"
                    strokeWidth="3.2"
                    strokeDasharray="58 42"
                    strokeDashoffset="25"
                  />
                  
                  {/* Segment 2: Closed - Blue (30%) */}
                  <circle
                    cx="18"
                    cy="18"
                    r="15.915"
                    fill="transparent"
                    stroke="#3b82f6"
                    strokeWidth="3.2"
                    strokeDasharray="30 70"
                    strokeDashoffset="-33"
                  />

                  {/* Segment 3: Private - Yellow (12%) */}
                  <circle
                    cx="18"
                    cy="18"
                    r="15.915"
                    fill="transparent"
                    stroke="#f59e0b"
                    strokeWidth="3.2"
                    strokeDasharray="12 88"
                    strokeDashoffset="-63"
                  />

                  {/* Center Text */}
                  <g className="donut-text">
                    <text x="50%" y="47%" textAnchor="middle" fill="#fff" fontSize="4.5" fontWeight="700">72</text>
                    <text x="50%" y="62%" textAnchor="middle" fill="var(--text-secondary)" fontSize="2">Polls Total</text>
                  </g>
                </svg>

                <div className="donut-legend">
                  <div className="legend-item">
                    <span className="legend-dot purple"></span>
                    <div>
                      <span className="legend-title">Active Decisions</span>
                      <span className="legend-value">42 (58%)</span>
                    </div>
                  </div>
                  <div className="legend-item">
                    <span className="legend-dot blue"></span>
                    <div>
                      <span className="legend-title">Closed Polls</span>
                      <span className="legend-value">21 (30%)</span>
                    </div>
                  </div>
                  <div className="legend-item">
                    <span className="legend-dot yellow"></span>
                    <div>
                      <span className="legend-title">Private Invites</span>
                      <span className="legend-value">9 (12%)</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent voting log summaries */}
            <div className="glass-card chart-container">
              <h3>Community Growth Analytics</h3>
              <p className="chart-desc">Growth rate of active members inside top communities.</p>
              
              <div className="community-growth-list">
                <div className="growth-row">
                  <div className="growth-info">
                    <h4>Startup Founders</h4>
                    <span>230 members</span>
                  </div>
                  <div className="growth-bar-wrapper">
                    <div className="growth-bar-fill purple" style={{ width: "85%" }}></div>
                    <span className="growth-percentage">+85%</span>
                  </div>
                </div>

                <div className="growth-row">
                  <div className="growth-info">
                    <h4>Career Community</h4>
                    <span>156 members</span>
                  </div>
                  <div className="growth-bar-wrapper">
                    <div className="growth-bar-fill blue" style={{ width: "62%" }}></div>
                    <span className="growth-percentage">+62%</span>
                  </div>
                </div>

                <div className="growth-row">
                  <div className="growth-info">
                    <h4>Travel Lovers</h4>
                    <span>89 members</span>
                  </div>
                  <div className="growth-bar-wrapper">
                    <div className="growth-bar-fill green" style={{ width: "40%" }}></div>
                    <span className="growth-percentage">+40%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Analytics;
