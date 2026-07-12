import { createContext, useContext, useState, useCallback } from "react";
import { FaCheckCircle, FaExclamationCircle, FaInfoCircle } from "react-icons/fa";
import "../styles/Toast.css";

const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = "success") => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ addToast }}>
      {children}
      <div className="toast-container">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type} animate-pop-in`} onClick={() => removeToast(t.id)}>
            <div className="toast-icon">
              {t.type === "success" && <FaCheckCircle />}
              {t.type === "error" && <FaExclamationCircle />}
              {t.type === "info" && <FaInfoCircle />}
            </div>
            <div className="toast-message">{t.message}</div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
};
