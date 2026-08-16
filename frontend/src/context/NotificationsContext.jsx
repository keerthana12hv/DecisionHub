import { createContext, useContext, useState, useEffect, useRef, useCallback } from "react";
import { useAuth } from "./AuthContext";
import {
  getNotifications,
  getUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
} from "../services/notificationService";

const NotificationsContext = createContext();

export const NotificationsProvider = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  
  const fetchingRef = useRef(false);
  const fetchingUnreadRef = useRef(false);

  const fetchNotifications = useCallback(async (force = false) => {
    if (!user) return;
    if (fetchingRef.current && !force) return;
    fetchingRef.current = true;
    setLoading(true);
    try {
      const data = await getNotifications(0, 100);
      if (data && data.content) {
        setNotifications(data.content);
      } else if (Array.isArray(data)) {
        setNotifications(data);
      } else {
        setNotifications([]);
      }
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
    } finally {
      setLoading(false);
      fetchingRef.current = false;
    }
  }, [user]);

  const fetchUnreadCount = useCallback(async (force = false) => {
    if (!user) return;
    if (fetchingUnreadRef.current && !force) return;
    fetchingUnreadRef.current = true;
    try {
      const count = await getUnreadCount();
      setUnreadCount(count);
    } catch (err) {
      console.error("Failed to fetch unread count:", err);
    } finally {
      fetchingUnreadRef.current = false;
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      fetchNotifications();
      fetchUnreadCount();

      const interval = setInterval(() => {
        fetchNotifications(true);
        fetchUnreadCount(true);
      }, 30000); // Poll every 30 seconds

      return () => clearInterval(interval);
    } else {
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [user, fetchNotifications, fetchUnreadCount]);

  const markAsRead = async (id) => {
    try {
      await markNotificationAsRead(id);
      // Optimistic UI updates
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
      // Full sync
      await Promise.all([fetchNotifications(true), fetchUnreadCount(true)]);
    } catch (err) {
      console.error("Failed to mark notification as read:", err);
      throw err;
    }
  };

  const markAllAsRead = async () => {
    try {
      await markAllNotificationsAsRead();
      // Optimistic UI updates
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, isRead: true }))
      );
      setUnreadCount(0);
      // Full sync
      await Promise.all([fetchNotifications(true), fetchUnreadCount(true)]);
    } catch (err) {
      console.error("Failed to mark all as read:", err);
      throw err;
    }
  };

  const deleteNotif = async (id) => {
    try {
      const notifToDelete = notifications.find((n) => n.id === id);
      const isUnread = notifToDelete ? !notifToDelete.isRead : false;
      await deleteNotification(id);
      // Optimistic UI updates
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      if (isUnread) {
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
      // Full sync
      await Promise.all([fetchNotifications(true), fetchUnreadCount(true)]);
    } catch (err) {
      console.error("Failed to delete notification:", err);
      throw err;
    }
  };

  const clearAllNotifs = async () => {
    try {
      await deleteAllNotifications();
      // Optimistic UI updates
      setNotifications([]);
      setUnreadCount(0);
      // Full sync
      await Promise.all([fetchNotifications(true), fetchUnreadCount(true)]);
    } catch (err) {
      console.error("Failed to clear notifications:", err);
      throw err;
    }
  };

  return (
    <NotificationsContext.Provider
      value={{
        notifications,
        unreadCount,
        loading,
        fetchNotifications,
        fetchUnreadCount,
        markAsRead,
        markAllAsRead,
        deleteNotif,
        clearAllNotifs,
      }}
    >
      {children}
    </NotificationsContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationsContext);
  if (!context) {
    throw new Error("useNotifications must be used within a NotificationsProvider");
  }
  return context;
};
