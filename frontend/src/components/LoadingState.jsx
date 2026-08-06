export default function LoadingState() {
  return (
    <div className="feedback-list">
      {[1, 2, 3].map((n) => (
        <div key={n} className="loading-skeleton-card glass-panel">
          <div className="skeleton-header">
            <div className="skeleton-user">
              <div className="skeleton-pulse skeleton-title"></div>
              <div className="skeleton-pulse skeleton-date"></div>
            </div>
            <div className="skeleton-pulse skeleton-rating"></div>
          </div>
          <div className="skeleton-pulse skeleton-line"></div>
          <div className="skeleton-pulse skeleton-line"></div>
          <div className="skeleton-pulse skeleton-line short"></div>
        </div>
      ))}
    </div>
  );
}
