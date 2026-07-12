import { useState } from "react";
import "../styles/InviteModal.css";

function InviteModal({ closeModal }) {

  const [email, setEmail] = useState("");

  return (

    <div className="modal-overlay">

      <div className="modal">

        <h2>Invite Members</h2>

        <input
          type="email"
          placeholder="Enter Email Address"
          value={email}
          onChange={(e)=>setEmail(e.target.value)}
        />

        <select>

          <option>Member</option>

          <option>Moderator</option>

        </select>

        <button className="send-btn">

          Send Invitation

        </button>

        <hr/>

        <h3>Invite Link</h3>

        <div className="invite-link">

          <input
            readOnly
            value="https://decisionhub.app/invite/ABCD123"
          />

          <button>

            Copy

          </button>

        </div>

        <div className="pending">

          <h3>Pending Invitations</h3>

          <p>rahul@gmail.com</p>

          <p>priya@gmail.com</p>

        </div>

        <button
          className="close-btn"
          onClick={closeModal}
        >

          Close

        </button>

      </div>

    </div>

  );

}

export default InviteModal;