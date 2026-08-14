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

export const getNotifications = async (unreadOnly = false) => {
  const response = await api.get(`/api/notifications?unreadOnly=${unreadOnly}`);
  const content = response.data.content || response.data;
  return content.map(mapNotification);
};

export const getUnreadCount = async () => {
  const response = await api.get("/api/notifications/unread-count");
  return response.data;
};

export const markAsRead = async (id) => {
  const response = await api.put(`/api/notifications/${id}/read`);
  return mapNotification(response.data);
};

export const markAllAsRead = async () => {
  await api.put("/api/notifications/read-all");
};

export const deleteNotification = async (id) => {
  await api.delete(`/api/notifications/${id}`);
};

export const clearAllNotifications = async () => {
  await api.delete("/api/notifications");
};
