import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ExternalLink, Flame, UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register } = useAuth();
  const [error, setError] = useState('');
  const configured = Boolean(import.meta.env.VITE_ENTRA_SIGN_UP_URL);
  const comenzar = async () => {
    try { await register(); } catch (e) { setError(e?.message || 'No se pudo abrir el flujo de identidad.'); }
  };
  return <div className="min-h-screen bg-slate-950 text-white grid place-items-center p-6">
    <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl p-8">
      <div className="flex gap-3 items-center mb-6"><Flame className="text-red-500"/><h1 className="text-2xl font-black uppercase">Registro de usuario</h1></div>
      <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5 text-sm text-slate-300 space-y-3">
        <p>Las cuentas ya no se crean desde React. Un tenant organizacional de Microsoft Entra ID requiere que un administrador cree/invite al usuario y asigne su rol.</p>
        <p>{configured ? 'Este entorno tiene configurado un flujo de registro de Entra External ID.' : 'No hay un flujo de Entra External ID configurado; el botón abrirá el inicio de sesión del tenant.'}</p>
      </div>
      {error && <p className="mt-4 text-red-400">{error}</p>}
      <button onClick={comenzar} className="mt-6 w-full bg-red-600 hover:bg-red-500 rounded-xl p-4 font-black uppercase flex justify-center items-center gap-2"><UserPlus size={18}/> {configured ? 'Abrir registro' : 'Ir a Microsoft Entra ID'} <ExternalLink size={15}/></button>
      <Link to="/login" className="block text-center mt-5 text-sm text-slate-400">Ya tengo una cuenta</Link>
    </div>
  </div>;
}
