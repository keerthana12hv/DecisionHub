function CreateDecision() {
  return (
    <div style={{ width: "500px", margin: "50px auto" }}>
      <h1>Create New Decision</h1>

      <input
        type="text"
        placeholder="Decision Title"
        style={{ width: "100%", padding: "10px", margin: "10px 0" }}
      />

      <textarea
        placeholder="Decision Description"
        style={{
          width: "100%",
          height: "120px",
          padding: "10px",
          margin: "10px 0",
        }}
      ></textarea>

      <input
        type="text"
        placeholder="Option 1"
        style={{ width: "100%", padding: "10px", margin: "10px 0" }}
      />

      <input
        type="text"
        placeholder="Option 2"
        style={{ width: "100%", padding: "10px", margin: "10px 0" }}
      />

      <button
        style={{
          width: "100%",
          padding: "12px",
          backgroundColor: "#4F46E5",
          color: "white",
          border: "none",
          cursor: "pointer",
          fontSize: "16px",
        }}
      >
        Create Decision
      </button>
    </div>
  );
}

export default CreateDecision;