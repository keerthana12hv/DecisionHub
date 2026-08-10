import { StrictMode, Component } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';

// Global error boundary — stops one page crash from blanking the whole app
class ErrorBoundary extends Component {
  state = { hasError: false, error: null };
  static getDerivedStateFromError(error) { return { hasError: true, error }; }
  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex', flexDirection: 'column', alignItems: 'center',
          justifyContent: 'center', minHeight: '100vh', gap: '1rem',
          fontFamily: 'Inter, sans-serif', color: '#e5e7eb', background: '#0a0a14',
          padding: '2rem', textAlign: 'center',
        }}>
          <h2 style={{ color: '#f87171' }}>Something went wrong</h2>
          <p style={{ color: '#9ca3af', fontSize: '0.9rem', maxWidth: '400px' }}>
            {this.state.error?.message ?? 'An unexpected error occurred.'}
          </p>
          <button
            onClick={() => { this.setState({ hasError: false, error: null }); window.location.href = '/dashboard'; }}
            style={{
              background: 'linear-gradient(135deg,#7c3aed,#a78bfa)', color: '#fff',
              border: 'none', borderRadius: '8px', padding: '10px 24px',
              cursor: 'pointer', fontWeight: '600',
            }}
          >
            Back to Dashboard
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </StrictMode>,
);
