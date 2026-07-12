const SEEDED_KEY = "decisionhub-seeded";

export const seedDatabase = () => {
  if (localStorage.getItem(SEEDED_KEY)) {
    return; // Already seeded
  }

  // 1. Seed Decisions
  const decisions = [
    {
      id: 1,
      title: "MBA vs Job after B.Tech",
      description: "Seeking advice on whether to immediately pursue an MBA or work in the software industry for 2 years to gain experience before business school.",
      category: "Education",
      visibility: "Public",
      status: "Active",
      deadline: "2026-08-15",
      userVoteOptionId: null,
      options: [
        { id: 1, name: "Pursue MBA immediately", votes: 42 },
        { id: 2, name: "Work for 2 years first", votes: 85 }
      ],
      comments: [
        { id: 101, user: "Priya Sharma", text: "Work experience adds massive value to MBA discussions. Go for the job first!", likes: 14, replies: [], reactions: { "👍": 4, "❤️": 2 } },
        { id: 102, user: "Rahul Verma", text: "If you get into a Tier 1 MBA college directly, do not wait. Otherwise, work.", likes: 8, replies: [], reactions: { "👍": 2 } }
      ]
    },
    {
      id: 2,
      title: "React vs Angular for new SaaS project",
      description: "We are starting a collaborative analytics dashboard SaaS. Which frontend framework is better for rapid scaling, ecosystem support, and ease of hiring developers?",
      category: "Technology",
      visibility: "Public",
      status: "Closed",
      deadline: "2026-06-30",
      userVoteOptionId: 1,
      options: [
        { id: 1, name: "React + Vite (Flexible)", votes: 120 },
        { id: 2, name: "Angular (Structured)", votes: 45 }
      ],
      comments: [
        { id: 201, user: "Dev Guru", text: "React has a larger ecosystem and simpler state management choices. Highly recommended.", likes: 25, replies: [], reactions: { "👍": 8, "🚀": 3 } },
        { id: 202, user: "Architect John", text: "Angular is great for larger enterprise apps, but Vite + React is faster for bootstrapping startups.", likes: 5, replies: [], reactions: { "👍": 1 } }
      ]
    },
    {
      id: 3,
      title: "Goa vs Bali for Annual Team Retreat",
      description: "Our company retreat is coming up! Let's choose between the beaches of Goa or the cultural landscapes of Bali. Budget is $1500 per person.",
      category: "Travel",
      visibility: "Public",
      status: "Active",
      deadline: "2026-09-01",
      userVoteOptionId: null,
      options: [
        { id: 1, name: "Goa, India", votes: 28 },
        { id: 2, name: "Bali, Indonesia", votes: 34 }
      ],
      comments: [
        { id: 301, user: "HR Team", text: "Please consider travel times and visa requirements when casting your vote!", likes: 10, replies: [], reactions: { "🎉": 5 } }
      ]
    }
  ];
  localStorage.setItem("decisionhub-decisions", JSON.stringify(decisions));

  // 2. Seed Communities
  const communities = [
    {
      id: 1,
      name: "Career Community",
      category: "Education",
      members: 156,
      joined: true,
      admins: ["admin", "Mythili"],
      banner: "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600&auto=format&fit=crop&q=60",
      pinnedDecisions: [1],
      feed: [
        { id: 1001, user: "admin", text: "Welcome to the Career Community! Post your questions about placements, internships, and career decisions.", likes: 15, reactions: { "👍": 6, "❤️": 4 } },
        { id: 1002, user: "Priya Sharma", text: "Has anyone interviewed at Google recently? Sharing a thread soon.", likes: 8, reactions: { "🚀": 2 } }
      ]
    },
    {
      id: 2,
      name: "Travel Lovers",
      category: "Travel",
      members: 89,
      joined: false,
      admins: ["TravelGuru"],
      banner: "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=600&auto=format&fit=crop&q=60",
      pinnedDecisions: [3],
      feed: [
        { id: 2001, user: "TravelGuru", text: "What's the best time to visit Bali? Share your insights!", likes: 4, reactions: { "❤️": 1 } }
      ]
    },
    {
      id: 3,
      name: "Startup Founders",
      category: "Business",
      members: 230,
      joined: true,
      admins: ["VCInvestor", "admin"],
      banner: "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=600&auto=format&fit=crop&q=60",
      pinnedDecisions: [2],
      feed: [
        { id: 3001, user: "VCInvestor", text: "Don't focus too much on coding initially; talk to users first.", likes: 32, reactions: { "👍": 12, "🚀": 9 } }
      ]
    }
  ];
  localStorage.setItem("decisionhub-communities", JSON.stringify(communities));

  // 3. Seed Activities
  const activities = [
    { id: 1, icon: "create", text: "You created 'MBA vs Job after B.Tech'", time: "Just now" },
    { id: 2, icon: "vote", text: "Rahul Verma voted on 'MBA vs Job after B.Tech'", time: "5 mins ago" },
    { id: 3, icon: "join", text: "Priya Sharma joined your community 'Career Community'", time: "15 mins ago" },
    { id: 4, icon: "close", text: "React vs Angular poll has closed", time: "1 day ago" }
  ];
  localStorage.setItem("decisionhub-activities", JSON.stringify(activities));

  // Mark as seeded
  localStorage.setItem(SEEDED_KEY, "true");
};
