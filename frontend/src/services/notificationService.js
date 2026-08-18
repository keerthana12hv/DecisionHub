import api from "./api";

const formatTime = (dateStr) => {
  if (!dateStr) return "";
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? "s" : ""} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? "s" : ""} ago`;
    return `${diffDays} day${diffDays > 1 ? "s" : ""} ago`;
  } catch (e) {
    return dateStr;
  }
};

const mapNotification = (n) => ({
  id: n.id,
  text: n.message,
  unread: !n.isRead,
  time: formatTime(n.createdAt)
});

// HEAD exports
export const getNotifications = async (page = 0, size = 20, unreadOnly = false) => {
  // Sriram compatibility check: if page is a boolean (representing unreadOnly)
  if (typeof page === "boolean") {
    const unreadOnlyVal = page;
    const response = await api.get(`/notifications?unreadOnly=${unreadOnlyVal}`);
    const content = response.data.content || response.data;
    return content.map(mapNotification);
  }
  const res = await api.get(`/notifications?page=${page}&size=${size}&unreadOnly=${unreadOnly}`);
  return res.data;
};

export const getUnreadCount = async () => {
  const res = await api.get("/notifications/unread-count");
  return res.data;
};

export const markNotificationAsRead = async (id) => {
  return api.put(`/notifications/${id}/read`);
};

export const markAllNotificationsAsRead = async () => {
  return api.put("/notifications/read-all");
};

export const deleteNotification = async (id) => {
  return api.delete(`/notifications/${id}`);
};

export const deleteAllNotifications = async () => {
  return api.delete("/notifications");
};

// Sriram compatibility exports
export const markAsRead = async (id) => {
  const response = await api.put(`/notifications/${id}/read`);
  return mapNotification(response.data);
};

export const markAllAsRead = async () => {
  await api.put("/notifications/read-all");
};

export const clearAllNotifications = async () => {
  await api.delete("/notifications");
};
