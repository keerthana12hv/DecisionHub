import { useEffect, useState } from "react";
import "../styles/Boards.css";

const STORAGE_KEY = "decisionhub-decisions";

function Boards() {
  const [decisions, setDecisions] = useState([]);

  useEffect(() => {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setDecisions(saved);
  }, []);

  const boards = [...new Set(decisions.map((d) => d.board).filter(Boolean))];

  return (
    <div className="boards-page">
      <h1>Decision Boards</h1>
      {boards.length === 0 ? (
        <p>No boards yet — create a decision and add a board name.</p>
      ) : (
        boards.map((b) => (
          <div key={b} className="board-card">
            <h3>{b}</h3>
            <ul>
              {decisions.filter((d) => d.board === b).map((d) => (
                <li key={d.id}>{d.title}</li>
              ))}
            </ul>
          </div>
        ))
      )}
    </div>
  );
}

export default Boards;
