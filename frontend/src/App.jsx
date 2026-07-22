import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./components/Toast";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";

import DecisionDetail from "./pages/DecisionDetail";
import CreateDecision from "./pages/CreateDecision";
import VotingPage from "./pages/VotingPage";
import Communities from "./pages/Communities";
import CommunityDetail from "./pages/CommunityDetails";
import DecisionList from "./pages/DecisionList";
import Profile from "./pages/Profile";
import Settings from "./pages/Settings";
import NotificationsPage from "./pages/NotificationsPage";
import Analytics from "./pages/Analytics";
import NotFound from "./pages/NotFound";
import Discussion from "./pages/Discussion";

function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Navigate to="/login" />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={<Dashboard />} />

            <Route path="/decision/:decisionId" element={<DecisionDetail />} />
            <Route path="/create-decision" element={<CreateDecision />} />
            <Route path="/vote" element={<VotingPage />} />
            <Route path="/communities" element={<Communities />} />
            <Route path="/communities/:communityId" element={<CommunityDetail />} />
            <Route path="/decisions" element={<DecisionList />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/discussion" element={<Discussion />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  );
}

export default App;