import "../styles/StatCard.css";

function StatCard({ title, value, icon, trend, onClick }) {
  const isClickable = typeof onClick === "function";
  return (
    <div 
      className={`stat-card glass-card ${isClickable ? "clickable-stat-card" : ""}`}
      onClick={onClick}
      style={{ cursor: isClickable ? "pointer" : "default" }}
    >
      <div className="stat-card-header">
        <span className="stat-value">{value}</span>
        {icon && <div className="stat-icon-wrapper">{icon}</div>}
      </div>
      <div className="stat-card-footer">
        <span className="stat-title">{title}</span>
        {trend && <span className="stat-trend">{trend}</span>}
      </div>
    </div>
  );
}

export default StatCard;