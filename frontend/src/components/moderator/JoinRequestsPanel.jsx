import { useEffect, useState, useCallback } from "react";
import "./JoinRequestsPanel.css";

const API_BASE = "http://localhost:8080";

function getToken() {
  return localStorage.getItem("token");
}

async function apiRequest(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
      ...(options.headers || {}),
    },
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || body.error || message;
    } catch {
      // response wasn't JSON, keep default message
    }
    throw new Error(message);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

function getRequestId(req) {
  return req.memberId ?? req.id ?? req.userId;
}

function getDisplayName(req) {
  return (
    req.username ??
    req.user?.username ??
    req.userName ??
    req.name ??
    "Unknown user"
  );
}

function getEmail(req) {
  return req.email ?? req.user?.email ?? "";
}

function getRequestedAt(req) {
  return req.requestedAt ?? req.joinedAt ?? req.createdAt ?? null;
}

function formatDate(isoString) {
  if (!isoString) return "";
  try {
    const date = new Date(isoString);
    return date.toLocaleDateString(undefined, {
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return "";
  }
}

function getInitials(name) {
  if (!name || name === "Unknown user") return "?";
  const parts = name.trim().split(/\s+/);
  const initials = parts.slice(0, 2).map((p) => p[0]?.toUpperCase() ?? "");
  return initials.join("") || "?";
}

const AVATAR_PALETTE = ["#5B5FE6", "#2A9D8F", "#E07A5F", "#3D5A80", "#9B5DE5", "#118AB2"];
function getAvatarColor(name) {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return AVATAR_PALETTE[Math.abs(hash) % AVATAR_PALETTE.length];
}

export default function JoinRequestsPanel({ communityId }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actingOnId, setActingOnId] = useState(null);

  const fetchRequests = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest(`/api/communities/${communityId}/requests`);
      setRequests(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [communityId]);

  useEffect(() => {
    if (communityId) fetchRequests();
  }, [communityId, fetchRequests]);

  async function handleDecision(req, decision) {
    const requestId = getRequestId(req);
    setActingOnId(requestId);
    setError(null);
    try {
      await apiRequest(
        `/api/communities/${communityId}/requests/${requestId}/${decision}`,
        { method: "PUT" }
      );
      setRequests((prev) => prev.filter((r) => getRequestId(r) !== requestId));
    } catch (err) {
      setError(err.message);
    } finally {
      setActingOnId(null);
    }
  }

  return (
    <div className="jrp">
      <div className="jrp__header">
        <h2 className="jrp__title">Join requests</h2>
        {!loading && requests.length > 0 && (
          <span className="jrp__count">{requests.length} pending</span>
        )}
      </div>

      {error && (
        <div className="jrp__error">
          {error}
          <button className="jrp__retry" onClick={fetchRequests}>
            Retry
          </button>
        </div>
      )}

      {loading && (
        <div className="jrp__state">
          <div className="jrp__spinner" />
          <p>Loading requests…</p>
        </div>
      )}

      {!loading && !error && requests.length === 0 && (
        <div className="jrp__state jrp__empty">
          <p>No pending requests right now.</p>
          <span>New requests to join this community will show up here.</span>
        </div>
      )}

      {!loading && requests.length > 0 && (
        <ul className="jrp__list">
          {requests.map((req) => {
            const id = getRequestId(req);
            const name = getDisplayName(req);
            const email = getEmail(req);
            const requestedAt = formatDate(getRequestedAt(req));
            const isActing = actingOnId === id;

            return (
              <li key={id} className="jrp__row">
                <div
                  className="jrp__avatar"
                  style={{ backgroundColor: getAvatarColor(name) }}
                >
                  {getInitials(name)}
                </div>

                <div className="jrp__info">
                  <span className="jrp__name">{name}</span>
                  {email && <span className="jrp__email">{email}</span>}
                  {requestedAt && (
                    <span className="jrp__date">Requested {requestedAt}</span>
                  )}
                </div>

                <div className="jrp__actions">
                  <button
                    className="jrp__btn jrp__btn--reject"
                    disabled={isActing}
                    onClick={() => handleDecision(req, "reject")}
                  >
                    Reject
                  </button>
                  <button
                    className="jrp__btn jrp__btn--approve"
                    disabled={isActing}
                    onClick={() => handleDecision(req, "approve")}
                  >
                    {isActing ? "Working…" : "Approve"}
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}