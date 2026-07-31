import { useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import FeedbackForm from "../components/FeedbackForm";
import SuccessDialog from "../components/SuccessDialog";
import ErrorDialog from "../components/ErrorDialog";
import { useAuth } from "../context/AuthContext";
import "../styles/Feedback.css";

export default function FeedbackPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showError, setShowError] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [onSuccessClear, setOnSuccessClear] = useState(null);

  const handleSubmit = (feedbackData, clearFormCallback) => {
    setLoading(true);

    // Simulate submission delay
    setTimeout(() => {
      // If user types 'fail' or 'error' in queries, mock a failure
      const hasFailed =
        feedbackData.queries.toLowerCase().includes("fail") ||
        feedbackData.queries.toLowerCase().includes("error") ||
        feedbackData.experience.toLowerCase().includes("fail") ||
        feedbackData.experience.toLowerCase().includes("error");

      if (hasFailed) {
        setErrorMessage("An unexpected server error occurred during transmission. Please try again.");
        setShowError(true);
        setLoading(false);
      } else {
        try {
          // Retrieve existing feedbacks from localStorage
          const existingFeedbacksJson = localStorage.getItem("decisionhub-feedbacks");
          const feedbacks = existingFeedbacksJson ? JSON.parse(existingFeedbacksJson) : [];

          // Create new feedback entry
          const newFeedback = {
            id: Date.now(),
            userName: user?.username || "Anonymous User",
            easeOfUseRating: feedbackData.easeOfUseRating,
            featureSatisfactionRating: feedbackData.featureSatisfactionRating,
            overallExperienceRating: feedbackData.overallExperienceRating,
            submissionDate: new Date().toISOString(),
            experience: feedbackData.experience,
            queries: feedbackData.queries || "",
            reply: "",
          };

          // Save feedback to storage
          feedbacks.push(newFeedback);
          localStorage.setItem("decisionhub-feedbacks", JSON.stringify(feedbacks));

          // Set clear callback to run when user clicks OK
          setOnSuccessClear(() => clearFormCallback);
          setShowSuccess(true);
        } catch (err) {
          setErrorMessage("Failed to write to local storage. Disk space may be low.");
          setShowError(true);
        } finally {
          setLoading(false);
        }
      }
    }, 1000);
  };

  const handleSuccessClose = () => {
    setShowSuccess(false);
    if (onSuccessClear) {
      onSuccessClear(); // Clears form inputs
    }
  };

  const handleErrorClose = () => {
    setShowError(false);
  };

  return (
    <div className="dashboard">
      <Sidebar />
      <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content animate-fade-in">
          <div className="settings-header">
            <h1>Submit Feedback</h1>
            <p>Your input is vital. Tell us what you like and where we can improve.</p>
          </div>

          <div style={{ display: "flex", justifyContent: "center", marginTop: "1rem" }}>
            <FeedbackForm onSubmit={handleSubmit} loading={loading} />
          </div>
        </div>
      </div>

      <SuccessDialog isOpen={showSuccess} onClose={handleSuccessClose} />
      <ErrorDialog isOpen={showError} onClose={handleErrorClose} message={errorMessage} />
    </div>
  );
}
