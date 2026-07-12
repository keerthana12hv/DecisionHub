import "../styles/StatCard.css";

function StatCard({ title, value }) {
  return (
    <div className="stat-card">
      <h2>{value}</h2>
      <p>{title}</p>
    </div>
  );
}

export default StatCard;