import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import CreateDecision from "./pages/CreateDecision";
import VotingPage from "./pages/VotingPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />

        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/create-decision" element={<CreateDecision />} />
        <Route path="/vote" element={<VotingPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;