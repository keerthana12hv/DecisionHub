import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/CommunityDetail.css";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import ModeratorPanel from "../components/ModeratorPanel";
import { FaThumbtack, FaLock, FaArrowRight } from "react-icons/fa";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

export default function CommunityDetail() {
  const { communityId } = useParams();
  const navigate = useNavigate();
  const [community, setCommunity] = useState(null);
  const [decisions, setDecisions] = useState([]);
  const [decisionsLoading, setDecisionsLoading] = useState(true);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCommunity();
    fetchCommunityDecisions();
    try {
      const t = token();
      const payload = JSON.parse(atob(t.split(".")[1]));
      setCurrentUserId(payload.id);
    } catch (err) {
      console.error("Failed to decode token:", err);
    }
  }, [communityId]);

  const fetchCommunity = async () => {
    try {
      const res = await axios.get(`${API}/communities/${communityId}`, headers());
      setCommunity(res.data);
    } catch (err) {
      console.error("Failed to fetch community:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCommunityDecisions = async () => {
    try {
      setDecisionsLoading(true);
      const res = await axios.get(
        `${API}/decisions?communityId=${communityId}`,
        headers()
      );
      setDecisions(res.data);
    } catch (err) {
      console.error("Failed to fetch community decisions:", err);
    } finally {
      setDecisionsLoading(false);
    }
  };

  const isModerator = community && String(community.ownerId) === String(currentUserId);

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          {loading && <p>Loading community...</p>}
          {!loading && !community && <p>Community not found.</p>}

          {!loading && community && (
            <div className="community-detail-page">
              <h2>{community.name}</h2>
              <p>{community.description}</p>
              <p>Category: {community.categoryName}</p>

              <section className="community-decisions-section">
                <h3>Decisions in this Community</h3>
                {decisionsLoading ? (
                  <p>Loading decisions...</p>
                ) : decisions.length === 0 ? (
                  <p className="empty-community-decisions">
                    No decisions have been created in this community yet.
                  </p>
                ) : (
                  <ul className="community-decisions-list">
                    {decisions.map((d) => (
                      <li key={d.id} className="community-decision-row">
                        <div className="community-decision-info">
                          <span className="community-decision-title">
                            {d.pinned && <FaThumbtack title="Pinned" style={{ marginRight: 6 }} />}
                            {d.locked && <FaLock title="Locked" style={{ marginRight: 6 }} />}
                            {d.title}
                          </span>
                          <span className={`status-badge ${d.status.toLowerCase()}`}>
                            {d.status}
                          </span>
                        </div>
                        <button
                          className="community-decision-view-btn"
                          onClick={() => navigate(`/decision/${d.id}`)}
                        >
                          View <FaArrowRight />
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </section>

              {isModerator && <ModeratorPanel communityId={community.id} />}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}