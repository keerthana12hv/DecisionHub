import {
  FaPlusCircle,
  FaVoteYea,
  FaUsers,
  FaChartLine
} from "react-icons/fa";

import "../styles/Activity.css";

function Activity() {

  const activities = [
    {
      icon: <FaPlusCircle />,
      text: "You created 'MBA vs Job'"
    },
    {
      icon: <FaVoteYea />,
      text: "Rahul voted on your poll"
    },
    {
      icon: <FaUsers />,
      text: "Priya joined your community"
    },
    {
      icon: <FaChartLine />,
      text: "Analytics updated successfully"
    }
  ];

  return (
    <>
      <h2 className="section-title">
        Recent Activity
      </h2>

      <div className="activity-card">

        {activities.map((item, index) => (

          <div
            className="activity-item"
            key={index}
          >

            <div className="activity-icon">
              {item.icon}
            </div>

            <span>{item.text}</span>

          </div>

        ))}

      </div>
    </>
  );
}

export default Activity;