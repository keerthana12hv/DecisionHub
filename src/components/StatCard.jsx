import "../styles/StatCard.css";

function StatCard({ title, value, icon, trend }) {
  return (
    <div className="stat-card glass-card">
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