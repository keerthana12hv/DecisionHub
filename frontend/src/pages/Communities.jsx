import { useState } from "react";
import { FaUsers, FaUserPlus, FaArrowRight } from "react-icons/fa";
import InviteModal from "../components/InviteModal";
import "../styles/Communities.css";

function Communities() {

  const [showModal, setShowModal] = useState(false);

  const communities = [
    {
      id: 1,
      name: "Career Community",
      category: "Education",
      members: 156
    },
    {
      id: 2,
      name: "Travel Lovers",
      category: "Travel",
      members: 89
    },
    {
      id: 3,
      name: "Startup Founders",
      category: "Business",
      members: 230
    }
  ];

  return (
    <div className="community-page">

      <div className="community-header">

        <h1>Communities</h1>

        <button className="create-btn">
          + Create Community
        </button>

      </div>

      <div className="community-grid">

        {communities.map((community) => (

          <div
            key={community.id}
            className="community-card"
          >

            <FaUsers className="community-icon" />

            <h2>{community.name}</h2>

            <p>
              <strong>Category:</strong> {community.category}
            </p>

            <p>
              <strong>Members:</strong> {community.members}
            </p>

            <div className="community-buttons">

              <button className="view-btn">
                <FaArrowRight /> View
              </button>

              <button
                className="invite-btn"
                onClick={() => setShowModal(true)}
              >
                <FaUserPlus /> Invite
              </button>

            </div>

          </div>

        ))}

      </div>

      {showModal && (
        <InviteModal
          closeModal={() => setShowModal(false)}
        />
      )}

    </div>
  );
}

export default Communities;