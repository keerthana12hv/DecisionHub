import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import "../styles/CommunityDetail.css";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import ModeratorPanel from "../components/ModeratorPanel";

const API = "http://localhost:8080/api";
const token = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("authToken") ||
  localStorage.getItem("jwt");
const headers = () => ({ headers: { Authorization: `Bearer ${token()}` } });

export default function CommunityDetail() {
  const { communityId } = useParams();
  const [community, setCommunity] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCommunity();
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

              {isModerator && <ModeratorPanel communityId={community.id} />}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}