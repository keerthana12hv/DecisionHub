import "../styles/Profile.css";

function Profile() {
  const stats = [
    { label: "Email", value: "mythili@gmail.com" },
    { label: "Role", value: "User" },
    { label: "Communities", value: "5" },
    { label: "Decisions", value: "12" },
  ];

  const recentActivity = [
    "Created a new decision about campus events",
    "Voted in the travel planning poll",
    "Joined 2 new communities",
  ];

  return (
    <div className="profile-page">
      <div className="profile-card">
        <img
          src="https://ui-avatars.com/api/?name=Mythili&background=6C63FF&color=fff"
          alt="profile"
        />
        <h2>Mythili</h2>
        <p>Computer Science Student</p>

        <div className="profile-info">
          {stats.map((item) => (
            <div key={item.label}>
              <h4>{item.label}</h4>
              <span>{item.value}</span>
            </div>
          ))}
        </div>

        <div className="profile-activity">
          <h3>Recent Activity</h3>
          <ul>
            {recentActivity.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </div>

        <button>Edit Profile</button>
      </div>
    </div>
  );
}

export default Profile;