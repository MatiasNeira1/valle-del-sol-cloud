import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, Flame, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login, isAuthenticated, cargando } = useAuth();
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (isAuthenticated) navigate(location.state?.from || '/dashboard', { replace: true });
  }, [isAuthenticated, location.state, navigate]);

  const iniciarSesion = async () => {
    setError('');
    try { await login(); } catch (e) { setError(e?.message || 'No se pudo iniciar sesión con Microsoft Entra ID.'); }
  };

  return <div className="min-h-screen bg-slate-950 text-white grid place-items-center p-6">
    <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl">
      <div className="flex items-center gap-3 mb-8"><div className="bg-red-600 p-3 rounded-2xl"><Flame /></div><div><h1 className="text-2xl font-black uppercase">Acceso seguro</h1><p className="text-xs text-slate-400">Municipalidad Valle del Sol</p></div></div>
      <div className="rounded-2xl border border-blue-500/20 bg-blue-500/10 p-4 mb-6 flex gap-3"><ShieldCheck className="text-blue-400 shrink-0"/><p className="text-sm text-slate-300">Inicia sesión mediante Microsoft Entra ID. Se usa Authorization Code Flow con PKCE y no se almacena el access token manualmente.</p></div>
      {error && <p role="alert" className="mb-4 rounded-xl bg-red-500/10 border border-red-500/20 p-3 text-sm text-red-300">{error}</p>}
      <button disabled={cargando} onClick={iniciarSesion} className="w-full bg-red-600 hover:bg-red-500 disabled:opacity-50 rounded-xl p-4 font-black uppercase flex justify-center items-center gap-2">Continuar con Microsoft <ArrowRight size={18}/></button>
      <div className="flex justify-between mt-6 text-xs text-slate-400"><Link to="/">Volver al inicio</Link><Link to="/register" className="text-red-400">Solicitar registro</Link></div>
    </div>
  </div>;
}
