import "../styles/RecentDecision.css";

function RecentDecision({ decisions = [] }) {
  const visibleDecisions = decisions.length > 0 ? decisions : [
    { title: "No decisions yet", status: "Active" },
  ];

  return (
    <>
      <h2 className="section-title">
        Recent Decisions
      </h2>

      <div className="decision-table">

        <div className="table-header">
          <span>Decision</span>
          <span>Status</span>
        </div>

        {visibleDecisions.map((decision, index) => (

          <div
            className="table-row"
            key={index}
          >

            <span>{decision.title}</span>

            <span
              className={
                decision.status === "Active"
                  ? "active"
                  : "closed"
              }
            >
              {decision.status}
            </span>

          </div>

        ))}

      </div>
    </>
  );
}

export default RecentDecision;