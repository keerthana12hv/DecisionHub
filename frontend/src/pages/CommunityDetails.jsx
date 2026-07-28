import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
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
    setCurrentUserId(localStorage.getItem("userId"));
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

  if (loading) return <p>Loading community...</p>;
  if (!community) return <p>Community not found.</p>;

  const isModerator = String(community.ownerId) === String(currentUserId);

  return (
    <div className="community-detail-page">
      <h2>{community.name}</h2>
      <p>{community.description}</p>
      <p>Category: {community.categoryName}</p>

      {isModerator && <ModeratorPanel communityId={community.id} />}
    </div>
  );
}