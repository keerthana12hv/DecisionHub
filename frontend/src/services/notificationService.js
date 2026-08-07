import api from "./api";

export const getNotifications = async (page = 0, size = 20) => {
  const res = await api.get(`/api/notifications?page=${page}&size=${size}`);
  return res.data;
};

export const getUnreadCount = async () => {
  const res = await api.get("/api/notifications/unread-count");
  return res.data;
};

export const markNotificationAsRead = async (id) => {
  return api.put(`/api/notifications/${id}/read`);
};

export const markAllNotificationsAsRead = async () => {
  return api.put("/api/notifications/read-all");
};

export const deleteNotification = async (id) => {
  return api.delete(`/api/notifications/${id}`);
};

export const deleteAllNotifications = async () => {
  return api.delete("/api/notifications");
};