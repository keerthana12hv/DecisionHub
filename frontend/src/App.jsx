import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { NotificationsProvider } from "./context/NotificationsContext";
import { ToastProvider } from "./components/Toast";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import DecisionDetail from "./pages/DecisionDetail";
import CreateDecision from "./pages/CreateDecision";
// import VotingPage from "./pages/VotingPage"; // Voting Room removed per mentor review — voting now happens directly on each decision's own page
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
import FeedbackPage from "./pages/FeedbackPage";
import FeedbackDashboard from "./pages/FeedbackDashboard";
import AdminCommunityDecisions from "./pages/AdminCommunityDecisions";
import AdminDecisionDiscussion from "./pages/AdminDecisionDiscussion";
import AdminDecisions from "./pages/AdminDecisions";

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
  if (user.role !== "MODERATOR") {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

// ─── Admin Route (ADMIN only) ────────────────────────────────────────────────
function AdminRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") {
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
      {/* Voting Room removed per mentor review (2026-07-30) — a separate feed
          page duplicated voting that already happens inline on each decision's
          own page. Left commented (not deleted) so it can be restored in one
          line if needed later; VotingPage.jsx itself is untouched.
      <Route path="/vote" element={<PrivateRoute><VotingPage /></PrivateRoute>} />
      */}
      <Route path="/decisions/:id/discuss"  element={<PrivateRoute><Discussion /></PrivateRoute>} />
      <Route path="/communities"      element={<PrivateRoute><Communities /></PrivateRoute>} />
      <Route path="/communities/:id"  element={<PrivateRoute><CommunityDetail /></PrivateRoute>} />
      <Route path="/analytics"        element={<PrivateRoute><Analytics /></PrivateRoute>} />
      <Route path="/notifications"    element={<PrivateRoute><NotificationsPage /></PrivateRoute>} />
      <Route path="/profile"          element={<PrivateRoute><Profile /></PrivateRoute>} />
      <Route path="/settings"         element={<PrivateRoute><Settings /></PrivateRoute>} />
      <Route path="/feedback"         element={<PrivateRoute><FeedbackPage /></PrivateRoute>} />
      <Route
        path="/feedback-dashboard"
        element={
          <AdminRoute>
            <FeedbackDashboard />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/communities/:id/decisions"
        element={
          <AdminRoute>
            <AdminCommunityDecisions />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/decisions"
        element={
          <AdminRoute>
            <AdminDecisions />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/decisions/:id/discuss"
        element={
          <AdminRoute>
            <AdminDecisionDiscussion />
          </AdminRoute>
        }
      />

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
        <NotificationsProvider>
          <ToastProvider>
            <AppRoutes />
          </ToastProvider>
        </NotificationsProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}