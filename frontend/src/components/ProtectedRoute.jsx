import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, cargando } = useAuth();
  const location = useLocation();
  if (cargando) return <div className="min-h-screen bg-slate-950 text-white grid place-items-center">Validando sesión...</div>;
  return isAuthenticated ? children : <Navigate to="/login" replace state={{ from: location.pathname }} />;
}
