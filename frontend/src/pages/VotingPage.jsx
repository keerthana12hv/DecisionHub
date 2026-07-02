import { useState } from "react";

const VotingPage = () => {
  const [voted, setVoted] = useState(false);
  const [selectedOption, setSelectedOption] = useState(null);

  const decision = {
    title: "MBA vs Job",
    description: "Which option is better after graduation?",
    options: [
      { id: 1, name: "MBA", votes: 12 },
      { id: 2, name: "Job", votes: 8 },
    ],
  };

  const totalVotes = decision.options.reduce((sum, o) => sum + o.votes, 0);

  const handleVote = (optionId) => {
    setSelectedOption(optionId);
    setVoted(true);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-lg">

        {/* Title */}
        <h1 className="text-2xl font-bold text-purple-700 mb-2">
          {decision.title}
        </h1>
        <p className="text-gray-500 mb-6">{decision.description}</p>

        {/* Options */}
        {decision.options.map((option) => {
          const percentage = Math.round((option.votes / totalVotes) * 100);
          return (
            <div key={option.id} className="mb-4">
              <div className="flex justify-between mb-1">
                <span className="font-medium text-gray-700">{option.name}</span>
                {voted && (
                  <span className="text-sm text-gray-500">{percentage}%</span>
                )}
              </div>

              {/* Progress Bar - shows after voting */}
              {voted && (
                <div className="w-full bg-gray-200 rounded-full h-3 mb-2">
                  <div
                    className="bg-purple-500 h-3 rounded-full"
                    style={{ width: `${percentage}%` }}
                  ></div>
                </div>
              )}

              {/* Vote Button */}
              {!voted && (
                <button
                  onClick={() => handleVote(option.id)}
                  className="w-full border-2 border-purple-400 text-purple-600 py-2 rounded-lg hover:bg-purple-50 transition"
                >
                  Vote for {option.name}
                </button>
              )}

              {/* Selected badge */}
              {voted && selectedOption === option.id && (
                <span className="text-green-500 text-sm">✅ You voted this</span>
              )}
            </div>
          );
        })}

        {/* Total Votes */}
        <p className="text-gray-400 text-sm mt-4 text-center">
          Total votes: {totalVotes}
        </p>

        {/* Thank you message */}
        {voted && (
          <div className="mt-4 bg-purple-50 text-purple-700 text-center py-3 rounded-lg font-medium">
            🎉 Thank you for voting!
          </div>
        )}
      </div>
    </div>
  );
};

export default VotingPage;