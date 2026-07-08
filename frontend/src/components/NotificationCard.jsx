import {
  FaBell,
  FaCommentDots,
  FaUsers,
  FaCheckCircle
} from "react-icons/fa";

import "../styles/NotificationCard.css";

function NotificationCard() {

  const notifications = [
    {
      icon: <FaBell />,
      title: "New Vote",
      message: "Rahul voted on your poll."
    },
    {
      icon: <FaCommentDots />,
      title: "New Comment",
      message: "Priya commented on MBA vs Job."
    },
    {
      icon: <FaUsers />,
      title: "Community",
      message: "You have a new community invitation."
    },
    {
      icon: <FaCheckCircle />,
      title: "Decision Closed",
      message: "Laptop Purchase poll has ended."
    }
  ];

  return (
    <>
      <h2 className="section-title">
        Notifications
      </h2>

      <div className="notification-card">

        {notifications.map((item, index) => (

          <div
            key={index}
            className="notification-item"
          >

            <div className="notification-icon">
              {item.icon}
            </div>

            <div>
              <h4>{item.title}</h4>
              <p>{item.message}</p>
            </div>

          </div>

        ))}

      </div>
    </>
  );
}

export default NotificationCard;