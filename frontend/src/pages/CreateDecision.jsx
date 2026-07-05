import { useNavigate } from "react-router-dom";
import "../styles/CreateDecision.css";

function CreateDecision() {
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    alert("Decision Created Successfully!");
    navigate("/dashboard");
  };

  return (
    <div className="create-container">
      <div className="create-card">

        <h1>DecisionHub</h1>
        <h2>Create New Decision</h2>

        <form onSubmit={handleSubmit}>

          <input
            type="text"
            placeholder="Decision Title"
            required
          />

          <textarea
            placeholder="Decision Description"
            rows="6"
            required
          />

          <button type="submit">
            Create Decision
          </button>

        </form>

      </div>
    </div>
  );
}

export default CreateDecision;