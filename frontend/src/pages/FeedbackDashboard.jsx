import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import FeedbackCard from "../components/FeedbackCard";
import ReplyModal from "../components/ReplyModal";
import EmptyState from "../components/EmptyState";
import LoadingState from "../components/LoadingState";
import { useToast } from "../components/Toast";
import { FaExclamationTriangle, FaRedo, FaBug } from "react-icons/fa";
import "../styles/Feedback.css";

// Initial Seed Data
const SEED_FEEDBACKS = [
  {
    id: 1,
    userName: "Keerthana H V",
    easeOfUseRating: 5,
    featureSatisfactionRating: 5,
    overallExperienceRating: 5,
    submissionDate: "2026-07-30T10:15:30Z",
    experience: "The tool is extremely helpful for team decision-making. The real-time ranking update is fantastic!",
    queries: "Will there be a mobile app version soon?",
    reply: "",
  },
  {
    id: 2,
    userName: "Alex Johnson",
    easeOfUseRating: 4,
    featureSatisfactionRating: 4,
    overallExperienceRating: 4,
    submissionDate: "2026-07-29T14:20:00Z",
    experience: "Smooth interface and very clean design. Glassmorphism looks amazing.",
    queries: "",
    reply: "",
  },
  {
    id: 3,
    userName: "Sarah Lee",
    easeOfUseRating: 3,
    featureSatisfactionRating: 3,
    overallExperienceRating: 3,
    submissionDate: "2026-07-28T09:05:00Z",
    experience: "Overall good, but some dropdown selectors load a bit slow on my screen.",
    queries: "Can we get custom tags for decision rooms?",
    reply: "",
  },
];

export default function FeedbackDashboard() {
  const { addToast } = useToast();
  const [feedbacks, setFeedbacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [shouldFail, setShouldFail] = useState(false); // Developer toggle to test failure and retry

  // Reply state
  const [replyModalOpen, setReplyModalOpen] = useState(false);
  const [selectedFeedback, setSelectedFeedback] = useState(null);

  const loadFeedbacks = () => {
    setLoading(true);
    setError(null);

    // Simulate network latency
    setTimeout(() => {
      if (shouldFail) {
        setError("Network Error: Failed to fetch feedback lists from Decision Hub servers.");
        setLoading(false);
        return;
      }

      try {
        let stored = localStorage.getItem("decisionhub-feedbacks");
        if (!stored) {
          // Seed initial data
          localStorage.setItem("decisionhub-feedbacks", JSON.stringify(SEED_FEEDBACKS));
          stored = JSON.stringify(SEED_FEEDBACKS);
        }

        const data = JSON.parse(stored);
        // Sort descending (latest date/id first)
        const sorted = data.sort((a, b) => {
          return new Date(b.submissionDate).getTime() - new Date(a.submissionDate).getTime();
        });

        setFeedbacks(sorted);
      } catch (err) {
        setError("Parse Error: Local storage feedback records are corrupted.");
      } finally {
        setLoading(false);
      }
    }, 800);
  };

  useEffect(() => {
    loadFeedbacks();
  }, []);

  const handleRetry = () => {
    setShouldFail(false); // Reset fail condition on user retry
    loadFeedbacks();
  };

  const handleOpenReply = (feedback) => {
    setSelectedFeedback(feedback);
    setReplyModalOpen(true);
  };

  const handleCloseReply = () => {
    setReplyModalOpen(false);
    setSelectedFeedback(null);
  };

  const handleSendReply = (replyText) => {
    try {
      const stored = localStorage.getItem("decisionhub-feedbacks");
      const list = stored ? JSON.parse(stored) : [];

      const updatedList = list.map((item) => {
        if (item.id === selectedFeedback.id) {
          return { ...item, reply: replyText };
        }
        return item;
      });

      localStorage.setItem("decisionhub-feedbacks", JSON.stringify(updatedList));
      addToast("Reply Sent Successfully", "success");
      handleCloseReply();

      // Reload dashboard list
      const sorted = updatedList.sort((a, b) => {
        return new Date(b.submissionDate).getTime() - new Date(a.submissionDate).getTime();
      });
      setFeedbacks(sorted);
    } catch (err) {
      addToast("Failed to save reply.", "error");
    }
  };

  // Toggle helper to verify error loading state
  const toggleSimulateError = () => {
    const nextFail = !shouldFail;
    setShouldFail(nextFail);
    addToast(
      nextFail ? "Fetch failure simulated. Reloading list..." : "Fetch failure disabled. Reloading list...",
      "info"
    );
    // Trigger reloading
    setLoading(true);
    setTimeout(() => {
      if (nextFail) {
        setError("Network Error: Failed to fetch feedback lists from Decision Hub servers.");
      } else {
        setError(null);
        const stored = localStorage.getItem("decisionhub-feedbacks");
        const sorted = JSON.parse(stored || "[]").sort((a, b) => {
          return new Date(b.submissionDate).getTime() - new Date(a.submissionDate).getTime();
        });
        setFeedbacks(sorted);
      }
      setLoading(false);
    }, 800);
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="feedback-dashboard-header">
            <div className="feedback-dashboard-title">
              <h1>Feedback Dashboard</h1>
              <p>Review submitted user ratings and reply directly to user queries.</p>
            </div>
            <div>
              <button
                className="btn-secondary"
                onClick={toggleSimulateError}
                style={{ fontSize: "0.85rem", padding: "0.5rem 1rem" }}
                title="Toggle simulated load errors to verify error/retry states"
              >
                <FaBug /> {shouldFail ? "Disable Error Sim" : "Simulate Loading Error"}
              </button>
            </div>
          </div>

          {loading ? (
            <LoadingState />
          ) : error ? (
            <div className="empty-state glass-panel animate-fade-in">
              <div className="empty-state-icon" style={{ color: "var(--danger)" }}>
                <FaExclamationTriangle />
              </div>
              <h3>Loading Failed</h3>
              <p style={{ marginBottom: "1.5rem" }}>{error}</p>
              <button className="btn-primary" onClick={handleRetry}>
                <FaRedo /> Retry Loading
              </button>
            </div>
          ) : feedbacks.length === 0 ? (
            <EmptyState message="Users have not submitted any feedbacks yet." />
          ) : (
            <div className="feedback-list">
              {feedbacks.map((item) => (
                <FeedbackCard
                  key={item.id}
                  feedback={item}
                  onReplyClick={handleOpenReply}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      <ReplyModal
        isOpen={replyModalOpen}
        onClose={handleCloseReply}
        onSubmit={handleSendReply}
        userName={selectedFeedback?.userName}
      />
    </div>
  );
}
