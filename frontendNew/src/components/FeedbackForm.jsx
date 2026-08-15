import { useState } from "react";
import StarRating from "./StarRating";
import { FaExclamationCircle } from "react-icons/fa";

export default function FeedbackForm({ onSubmit, loading }) {
  const [easeOfUseRating, setEaseOfUseRating] = useState(0);
  const [featureSatisfactionRating, setFeatureSatisfactionRating] = useState(0);
  const [overallExperienceRating, setOverallExperienceRating] = useState(0);
  const [experience, setExperience] = useState("");
  const [queries, setQueries] = useState("");
  const [errors, setErrors] = useState({});

  const validate = () => {
    const newErrors = {};
    if (easeOfUseRating === 0) {
      newErrors.easeOfUseRating = "Ease of Use rating is required.";
    }
    if (featureSatisfactionRating === 0) {
      newErrors.featureSatisfactionRating = "Feature Satisfaction rating is required.";
    }
    if (overallExperienceRating === 0) {
      newErrors.overallExperienceRating = "Overall Experience rating is required.";
    }
    if (!experience.trim()) {
      newErrors.experience = "Experience description is required.";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;

    onSubmit({
      easeOfUseRating,
      featureSatisfactionRating,
      overallExperienceRating,
      experience: experience.trim(),
      queries: queries.trim(),
    }, resetForm);
  };

  const resetForm = () => {
    setEaseOfUseRating(0);
    setFeatureSatisfactionRating(0);
    setOverallExperienceRating(0);
    setExperience("");
    setQueries("");
    setErrors({});
  };

  return (
    <form onSubmit={handleSubmit} className="feedback-form-card glass-panel animate-fade-in">
      <div className="feedback-form-group">
        <label>
          Ease of Use <span className="required-asterisk">*</span>
        </label>
        <StarRating rating={easeOfUseRating} onChange={(val) => {
          setEaseOfUseRating(val);
          if (errors.easeOfUseRating) {
            setErrors((prev) => ({ ...prev, easeOfUseRating: null }));
          }
        }} />
        {errors.easeOfUseRating && (
          <div className="validation-error">
            <FaExclamationCircle /> {errors.easeOfUseRating}
          </div>
        )}
      </div>

      <div className="feedback-form-group">
        <label>
          Feature Satisfaction <span className="required-asterisk">*</span>
        </label>
        <StarRating rating={featureSatisfactionRating} onChange={(val) => {
          setFeatureSatisfactionRating(val);
          if (errors.featureSatisfactionRating) {
            setErrors((prev) => ({ ...prev, featureSatisfactionRating: null }));
          }
        }} />
        {errors.featureSatisfactionRating && (
          <div className="validation-error">
            <FaExclamationCircle /> {errors.featureSatisfactionRating}
          </div>
        )}
      </div>

      <div className="feedback-form-group">
        <label>
          Overall Experience <span className="required-asterisk">*</span>
        </label>
        <StarRating rating={overallExperienceRating} onChange={(val) => {
          setOverallExperienceRating(val);
          if (errors.overallExperienceRating) {
            setErrors((prev) => ({ ...prev, overallExperienceRating: null }));
          }
        }} />
        {errors.overallExperienceRating && (
          <div className="validation-error">
            <FaExclamationCircle /> {errors.overallExperienceRating}
          </div>
        )}
      </div>

      <div className="feedback-form-group">
        <label htmlFor="experience-input">
          Experience <span className="required-asterisk">*</span>
        </label>
        <textarea
          id="experience-input"
          placeholder="Please describe your experience using Decision Hub..."
          value={experience}
          onChange={(e) => {
            setExperience(e.target.value);
            if (errors.experience && e.target.value.trim()) {
              setErrors((prev) => ({ ...prev, experience: null }));
            }
          }}
          className={errors.experience ? "error-border" : ""}
        />
        {errors.experience && (
          <div className="validation-error">
            <FaExclamationCircle /> {errors.experience}
          </div>
        )}
      </div>

      <div className="feedback-form-group">
        <label htmlFor="queries-input">Enter if you have any queries</label>
        <textarea
          id="queries-input"
          placeholder="Enter issues, suggestions, bugs, feature requests, or questions here (optional)..."
          value={queries}
          onChange={(e) => setQueries(e.target.value)}
        />
      </div>

      <button
        type="submit"
        className="btn-primary feedback-submit-btn"
        disabled={loading}
      >
        {loading ? (
          <>
            <span className="spinner"></span> Submitting...
          </>
        ) : (
          "Submit Feedback"
        )}
      </button>
    </form>
  );
}
