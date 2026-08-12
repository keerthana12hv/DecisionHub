import { useEffect, useState } from "react";
import { getReportedComments, deleteReport, modDeleteComment } from "../../services/moderationService";
import { useToast } from "../../components/Toast";

export default function ReportedComments() {
  const { addToast } = useToast();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const data = await getReportedComments();
      setReports(data || []);
    } catch (err) {
      addToast("Failed to load reported comments.", "error");
    } finally {
      setLoading(false);
    }
  };

  const handleDismiss = async (reportId) => {
    try {
      await deleteReport(reportId);
      setReports((prev) => prev.filter((r) => r.id !== reportId));
      addToast("Report dismissed.", "success");
    } catch (err) {
      addToast("Failed to dismiss report.", "error");
    }
  };

  const handleRemoveComment = async (report) => {
    try {
      await modDeleteComment(report.commentId);
      // Remove any reports for that comment
      setReports((prev) => prev.filter((r) => r.commentId !== report.commentId));
      addToast("Comment removed.", "success");
    } catch (err) {
      addToast("Failed to remove comment.", "error");
    }
  };

  if (loading) return <p>Loading reported comments...</p>;

  if (reports.length === 0) return <p>No reported comments.</p>;

  return (
    <div className="reported-comments">
      <h3>Reported Comments</h3>
      <ul>
        {reports.map((r) => (
          <li key={r.id} style={{ marginBottom: 12 }}>
            <div style={{ fontSize: 13, color: "#9CA3AF" }}>
              Reported by <strong>{r.reporterUsername}</strong> at {new Date(r.createdAt).toLocaleString()}
            </div>
            <div style={{ marginTop: 6 }}>
              <div style={{ color: "#F3F4F6" }}>Reason: {r.reason}</div>
              <div style={{ marginTop: 6 }}>
                <button onClick={() => handleRemoveComment(r)} style={{ marginRight: 8 }}>Remove comment</button>
                <button onClick={() => handleDismiss(r.id)}>Dismiss report</button>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
