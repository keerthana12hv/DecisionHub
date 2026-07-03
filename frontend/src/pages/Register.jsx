function Register() {
  return (
    <div style={{ textAlign: "center", marginTop: "100px" }}>
      <h1>DecisionHub</h1>
      <h2>Register</h2>

      <form style={{ width: "300px", margin: "auto" }}>
        <input
          type="text"
          placeholder="Enter Name"
          style={{ width: "100%", padding: "10px", margin: "10px 0" }}
        />

        <input
          type="email"
          placeholder="Enter Email"
          style={{ width: "100%", padding: "10px", margin: "10px 0" }}
        />

        <input
          type="password"
          placeholder="Enter Password"
          style={{ width: "100%", padding: "10px", margin: "10px 0" }}
        />

        <button
          type="submit"
          style={{
            width: "100%",
            padding: "10px",
            backgroundColor: "#4F46E5",
            color: "white",
            border: "none",
            cursor: "pointer",
          }}
        >
          Register
        </button>

        <p style={{ marginTop: "15px" }}>
          Already have an account? <a href="/">Login</a>
        </p>
      </form>
    </div>
  );
}

export default Register;