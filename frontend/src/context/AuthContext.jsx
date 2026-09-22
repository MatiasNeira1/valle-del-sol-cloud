import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import { loginRequest, tokenRequest } from '../auth/msal-config';
import { identityApi } from '../services/api';
import { normalizeRoles, selectDashboardRole } from '../utils/roles';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const { instance, accounts, inProgress } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const account = instance.getActiveAccount() || accounts[0] || null;
  const accountId = account?.homeAccountId || null;
  const [profile, setProfile] = useState(null);
  const [profileStatus, setProfileStatus] = useState('idle');
  const [profileError, setProfileError] = useState(null);

  useEffect(() => {
    if (!instance.getActiveAccount() && accounts[0]) instance.setActiveAccount(accounts[0]);
  }, [accounts, instance]);

  useEffect(() => {
    let active = true;
    if (!isAuthenticated || !accountId || inProgress !== 'none') return undefined;

    const loadProfile = async () => {
      setProfileStatus('loading');
      setProfileError(null);
      try {
        const { data } = await identityApi.get('/api/me');
        if (!active) return;
        const roles = normalizeRoles(data);
        if (roles.length === 0) {
          setProfile(null);
          setProfileError('Tu cuenta está autenticada, pero no tiene un rol de aplicación asignado.');
          setProfileStatus('error');
          return;
        }
        
        setProfile({ ...data, accountId, roles, role: selectDashboardRole(roles) });
        setProfileStatus('ready');
      } catch (error) {
        if (!active) return;
        setProfile(null);
        setProfileError(error.userMessage || 'No fue posible obtener el perfil autorizado desde la API.');
        setProfileStatus('error');
      }
    };
    loadProfile();

    return () => { active = false; };
  }, [accountId, inProgress, isAuthenticated]);

  const getAccessToken = useCallback(async () => {
    const activeAccount = instance.getActiveAccount() || accounts[0];
    if (!activeAccount) throw new InteractionRequiredAuthError('No active account');
    try {
      const result = await instance.acquireTokenSilent({ ...tokenRequest, account: activeAccount });
      return result.accessToken;
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        await instance.acquireTokenRedirect({ ...tokenRequest, account: activeAccount });
      }
      throw error;
    }
  }, [accounts, instance]);

  const user = isAuthenticated && profile?.accountId === accountId ? {
    uid: profile.id,
    email: profile.email,
    fullName: profile.name,
    role: profile.role,
    roles: profile.roles,
    username: profile.email,
    active: true,
  } : null;

  const login = () => instance.loginRedirect(loginRequest);
  const logout = () => instance.logoutRedirect({ account });
  const registrationUrl = import.meta.env.VITE_ENTRA_SIGN_UP_URL;
  const register = () => registrationUrl ? window.location.assign(registrationUrl) : login();
  const unsupportedAdminAction = async () => {
    throw new Error('La administración de usuarios y roles se realiza en Microsoft Entra ID o mediante una API Microsoft Graph protegida.');
  };
  const loading = inProgress !== 'none'
    || (isAuthenticated && (profile?.accountId !== accountId || profileStatus === 'idle' || profileStatus === 'loading'));

  const value = {
    user,
    usuarios: user ? [user] : [],
    loading,
    cargando: loading,
    profileError,
    cargandoUsuarios: false,
    isAuthenticated,
    login,
    logout,
    register,
    getAccessToken,
    crearUsuario: unsupportedAdminAction,
    editarUsuario: unsupportedAdminAction,
    eliminarUsuario: unsupportedAdminAction,
    alternarEstadoUsuario: unsupportedAdminAction,
    traducirErrorIdentidad: (error) => error?.message || 'No se pudo completar la operación de identidad.',
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() { return useContext(AuthContext); }
