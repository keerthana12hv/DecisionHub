import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
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
import Discussion from "./pages/Discussion";
import Analytics from "./pages/Analytics";
import NotificationsPage from "./pages/NotificationsPage";
import Profile from "./pages/Profile";
import Settings from "./pages/Settings";
import NotFound from "./pages/NotFound";
import ModeratorDashboard from "./pages/ModeratorDashboard";

// ─── Protected Route (any logged-in user) ────────────────────────────────────
function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  return user ? children : <Navigate to="/login" replace />;
}

// ─── Moderator Route (MODERATOR or ADMIN only) ────────────────────────────────
function ModeratorRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "MODERATOR" && user.role !== "ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Private — any logged-in user */}
      <Route path="/dashboard"        element={<PrivateRoute><Dashboard /></PrivateRoute>} />
      <Route path="/decisions"        element={<PrivateRoute><DecisionList /></PrivateRoute>} />
<Route path="/create-decision" element={<PrivateRoute><CreateDecision /></PrivateRoute>} />
      <Route path="/decisions/:id"    element={<PrivateRoute><DecisionDetail /></PrivateRoute>} />
      {/* VotingPage shows a feed of ALL active SINGLE_CHOICE/MULTIPLE_CHOICE
          decisions — it doesn't read an :id param, so it's registered here
          at plain /vote to match the Sidebar's "Voting Room" link.
          RATING_BASED decisions are intentionally excluded here — those are
          voted on via RatingPanel directly on the decision's own page. */}
      <Route path="/vote"             element={<PrivateRoute><VotingPage /></PrivateRoute>} />
      <Route path="/decisions/:id/discuss"  element={<PrivateRoute><Discussion /></PrivateRoute>} />
      <Route path="/communities"      element={<PrivateRoute><Communities /></PrivateRoute>} />
      <Route path="/communities/:id"  element={<PrivateRoute><CommunityDetail /></PrivateRoute>} />
      <Route path="/analytics"        element={<PrivateRoute><Analytics /></PrivateRoute>} />
      <Route path="/notifications"    element={<PrivateRoute><NotificationsPage /></PrivateRoute>} />
      <Route path="/profile"          element={<PrivateRoute><Profile /></PrivateRoute>} />
      <Route path="/settings"         element={<PrivateRoute><Settings /></PrivateRoute>} />

      {/* Moderator only */}
      <Route
        path="/moderator-dashboard"
        element={
          <ModeratorRoute>
            <ModeratorDashboard />
          </ModeratorRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <AppRoutes />
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}