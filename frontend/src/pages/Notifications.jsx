import { useState } from "react";
import { Bell, CheckCheck, Trash2 } from "lucide-react";

const Notifications = () => {
  const [notifications, setNotifications] = useState([
    {
      id: 1,
      type: "vote",
      message: "Keerthana voted on your decision 'MBA vs Job'",
      time: "2 mins ago",
      isRead: false,
    },
    {
      id: 2,
      type: "comment",
      message: "Aparna commented on 'iPhone vs Samsung'",
      time: "10 mins ago",
      isRead: false,
    },
    {
      id: 3,
      type: "invite",
      message: "You were invited to join 'Tech Community'",
      time: "1 hour ago",
      isRead: false,
    },
    {
      id: 4,
      type: "vote",
      message: "5 people voted on your decision 'Goa vs Bali'",
      time: "2 hours ago",
      isRead: true,
    },
    {
      id: 5,
      type: "comment",
      message: "Someone replied to your comment",
      time: "1 day ago",
      isRead: true,
    },
  ]);

  const markAllRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, isRead: true })));
  };

  const deleteNotification = (id) => {
    setNotifications(notifications.filter((n) => n.id !== id));
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const getIcon = (type) => {
    if (type === "vote") return "🗳️";
    if (type === "comment") return "💬";
    if (type === "invite") return "👥";
    return "🔔";
  };

  return (
    <div className="p-6 max-w-3xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Bell className="text-purple-600" size={28} />
          <h1 className="text-2xl font-bold text-gray-800">Notifications</h1>
          {unreadCount > 0 && (
            <span className="bg-purple-600 text-white text-xs px-2 py-1 rounded-full">
              {unreadCount} new
            </span>
          )}
        </div>
        <button
          onClick={markAllRead}
          className="flex items-center gap-2 text-purple-600 hover:text-purple-800 text-sm font-medium"
        >
          <CheckCheck size={16} />
          Mark all as read
        </button>
      </div>

      {/* Notification List */}
      {notifications.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <Bell size={48} className="mx-auto mb-4 opacity-30" />
          <p className="text-lg">No notifications yet</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`flex items-start gap-4 p-4 rounded-xl border transition-all ${
                n.isRead
                  ? "bg-white border-gray-100"
                  : "bg-purple-50 border-purple-200"
              }`}
            >
              {/* Icon */}
              <div className="text-2xl">{getIcon(n.type)}</div>

              {/* Content */}
              <div className="flex-1">
                <p
                  className={`text-sm ${
                    n.isRead
                      ? "text-gray-600"
                      : "text-gray-800 font-semibold"
                  }`}
                >
                  {n.message}
                </p>
                <p className="text-xs text-gray-400 mt-1">{n.time}</p>
              </div>

              {/* Unread dot */}
              {!n.isRead && (
                <div className="w-2 h-2 bg-purple-600 rounded-full mt-2"></div>
              )}

              {/* Delete button */}
              <button
                onClick={() => deleteNotification(n.id)}
                className="text-gray-300 hover:text-red-400 transition"
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Notifications;