import { useEffect, useState } from "react";
import EditDecisionModal from "../components/EditDecisionModal";
import { FaEdit, FaTrash, FaVoteYea, FaChartBar, FaSearch } from "react-icons/fa";
import { Link, useNavigate } from "react-router-dom";
import DeleteModal from "../components/DeleteModal";
import "../styles/DecisionList.css";

const STORAGE_KEY = "decisionhub-decisions";

function DecisionList() {
  const navigate = useNavigate();
  const [showDelete, setShowDelete] = useState(false);
  const [selectedDecision, setSelectedDecision] = useState(null);
  const [showEdit, setShowEdit] = useState(false);
  const [editDecision, setEditDecision] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("All");
  const [decisions, setDecisions] = useState([]);

  useEffect(() => {
    const savedDecisions = JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    setDecisions(savedDecisions);
  }, []);

  const filteredDecisions = decisions.filter((decision) => {
    const matchesSearch = decision.title.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === "All" || decision.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const persistDecisions = (nextDecisions) => {
    setDecisions(nextDecisions);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextDecisions));
  };

  const handleDelete = () => {
    const nextDecisions = decisions.filter((decision) => decision.id !== selectedDecision.id);
    persistDecisions(nextDecisions);
    setShowDelete(false);
    setSelectedDecision(null);
    alert("Decision Deleted Successfully!");
  };

  const handleSave = (updatedDecision) => {
    const nextDecisions = decisions.map((decision) =>
      decision.id === editDecision.id ? { ...decision, ...updatedDecision } : decision
    );
    persistDecisions(nextDecisions);
    setShowEdit(false);
    setEditDecision(null);
    alert("Decision Updated Successfully!");
  };

  return (
    <div className="decision-page">
      <div className="decision-header">
        <h1>Decision Management</h1>

        <div className="decision-actions">
          <div className="search-box">
            <FaSearch />
            <input
              type="text"
              placeholder="Search Decision..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <select className="filter" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="All">All</option>
            <option value="Active">Active</option>
            <option value="Closed">Closed</option>
          </select>

          <Link to="/create-decision">
            <button className="create-decision-btn">+ Create Decision</button>
          </Link>
        </div>
      </div>

      <table className="decision-table">
        <thead>
          <tr>
            <th>Decision</th>
            <th>Status</th>
            <th>Votes</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filteredDecisions.length === 0 ? (
            <tr>
              <td colSpan="4">No decisions yet. Create one to see it here.</td>
            </tr>
          ) : (
            filteredDecisions.map((decision) => (
              <tr key={decision.id}>
              <td>{decision.title}</td>
              <td>
                <span className={decision.status === "Active" ? "status-active" : "status-closed"}>
                  {decision.status}
                </span>
              </td>
              <td>{decision.votes}</td>
              <td>
                <button
                  className="edit-btn"
                  onClick={() => {
                    setEditDecision(decision);
                    setShowEdit(true);
                  }}
                >
                  <FaEdit />
                </button>

                <button
                  className="delete-btn"
                  onClick={() => {
                    setSelectedDecision(decision);
                    setShowDelete(true);
                  }}
                >
                  <FaTrash />
                </button>

                <button className="vote-btn" onClick={() => navigate("/vote")}>
                  <FaVoteYea />
                </button>

                <button className="analytics-btn">
                  <FaChartBar />
                </button>
              </td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      {showDelete && selectedDecision && (
        <DeleteModal
          decisionTitle={selectedDecision.title}
          onCancel={() => setShowDelete(false)}
          onDelete={handleDelete}
        />
      )}

      {showEdit && editDecision && (
        <EditDecisionModal
          decision={editDecision}
          onClose={() => {
            setShowEdit(false);
            setEditDecision(null);
          }}
          onSave={handleSave}
        />
      )}
    </div>
  );
}

export default DecisionList;