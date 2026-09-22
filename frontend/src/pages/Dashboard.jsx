import React from 'react';
import { useAuth } from '../context/AuthContext'; // ◄ Un solo nivel hacia atrás
import AdminDashboard from './dashboards/AdminDashboard';
import StaffDashboard from './dashboards/StaffDashboard';
import EntidadDashboard from './dashboards/EntidadDashboard';
import VecinoDashboard from './dashboards/VecinoDashboard';

function Dashboard() {
  const { user, cargando, profileError } = useAuth();

  if (cargando) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-red-500 font-bold uppercase tracking-widest animate-pulse">
        Cargando Consola Operativa...
      </div>
    );
  }

  if (profileError) {
    return <div className="min-h-screen bg-slate-950 text-red-300 grid place-items-center px-6 text-center">{profileError}</div>;
  }

  if (!user?.role) {
    return <div className="min-h-screen bg-slate-950 text-amber-300 grid place-items-center px-6 text-center">Tu cuenta no tiene un rol de aplicación autorizado para acceder al dashboard.</div>;
  }

  // Switch de enrutamiento dinámico según el Rol real del usuario
  switch (user?.role) {
    case 'ADMIN':
      return <AdminDashboard />;
      
    case 'STAFF':
      return <StaffDashboard />;
      
    case 'EMERGENCY_ENTITY':
      return <EntidadDashboard />;
      
    case 'USER':
      return <VecinoDashboard />;
    default:
      return <div className="min-h-screen bg-slate-950 text-amber-300 grid place-items-center">Acceso insuficiente.</div>;
  }
}

export default Dashboard;
