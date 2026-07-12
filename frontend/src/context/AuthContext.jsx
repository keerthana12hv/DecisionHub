import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem("decisionhub-session");
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = (email, password, role = "USER", rememberMe = false) => {
    // Simulated authentication
    const mockUser = {
      id: Date.now(),
      username: email.split("@")[0],
      email,
      role: role.toUpperCase(), // "ADMIN" or "USER"
      phone: "+1 (555) 234-5678",
      joinedDate: new Date().toLocaleDateString(),
      photo: `https://ui-avatars.com/api/?name=${email.split("@")[0]}&background=${role === "ADMIN" ? "8B5CF6" : "3B82F6"}&color=fff&size=128`,
      stats: {
        createdDecisions: role === "ADMIN" ? 8 : 0,
        totalVotes: role === "ADMIN" ? 142 : 45,
        joinedCommunities: 4,
        participationRate: 85,
        achievements: role === "ADMIN" 
          ? ["Architect", "Decision Master", "Influencer"] 
          : ["Early Voter", "Active Thinker", "Community Pillar"]
      }
    };

    setUser(mockUser);
    if (rememberMe) {
      localStorage.setItem("decisionhub-session", JSON.stringify(mockUser));
    }
    return mockUser;
  };

  const register = (username, email, password, role = "USER") => {
    const mockUser = {
      id: Date.now(),
      username,
      email,
      role: role.toUpperCase(),
      phone: "",
      joinedDate: new Date().toLocaleDateString(),
      photo: `https://ui-avatars.com/api/?name=${username}&background=${role === "ADMIN" ? "8B5CF6" : "3B82F6"}&color=fff&size=128`,
      stats: {
        createdDecisions: 0,
        totalVotes: 0,
        joinedCommunities: 0,
        participationRate: 0,
        achievements: []
      }
    };
    setUser(mockUser);
    localStorage.setItem("decisionhub-session", JSON.stringify(mockUser));
    return mockUser;
  };

  const updateProfile = (updatedData) => {
    setUser((prevUser) => {
      if (!prevUser) return null;
      const nextUser = { ...prevUser, ...updatedData };
      localStorage.setItem("decisionhub-session", JSON.stringify(nextUser));
      return nextUser;
    });
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("decisionhub-session");
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, updateProfile, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
