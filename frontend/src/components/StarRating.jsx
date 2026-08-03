import { useState } from "react";
import { FaStar } from "react-icons/fa";

export default function StarRating({ rating, onChange, readOnly = false }) {
  const [hoverRating, setHoverRating] = useState(0);

  const handleMouseEnter = (index) => {
    if (readOnly) return;
    setHoverRating(index);
  };

  const handleMouseLeave = () => {
    if (readOnly) return;
    setHoverRating(0);
  };

  const handleClick = (index) => {
    if (readOnly || !onChange) return;
    onChange(index);
  };

  return (
    <div className="star-rating-container" onMouseLeave={handleMouseLeave}>
      <div className="stars-row">
        {[1, 2, 3, 4, 5].map((index) => {
          const isStarActive = hoverRating ? index <= hoverRating : index <= rating;

          return (
            <button
              key={index}
              type="button"
              className={`star-icon-btn ${isStarActive ? "active" : ""} ${readOnly ? "readonly" : ""}`}
              onMouseEnter={() => handleMouseEnter(index)}
              onClick={() => handleClick(index)}
              aria-label={`Rate ${index} out of 5 stars`}
              disabled={readOnly}
            >
              <FaStar />
            </button>
          );
        })}
      </div>
    </div>
  );
}
