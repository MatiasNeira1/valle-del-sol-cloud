import axios from 'axios';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { msalInstance, tokenRequest } from '../auth/msal-config';

const configuredApiUrl = import.meta.env.VITE_API_URL;
const apiRoot = (configuredApiUrl || (import.meta.env.DEV ? 'http://localhost:8080' : '')).replace(/\/$/, '');

if (!apiRoot) {
  throw new Error('VITE_API_URL es obligatoria fuera del desarrollo local. Usa el origen de Azure API Management.');
}

const createApi = (path) => {
  const client = axios.create({
    baseURL: `${apiRoot}${path}`,
    headers: { 'Content-Type': 'application/json' },
    timeout: 15000,
  });

  client.interceptors.request.use(async (config) => {
    const account = msalInstance.getActiveAccount() || msalInstance.getAllAccounts()[0];
    if (!account) throw new InteractionRequiredAuthError('No active account');
    try {
      const result = await msalInstance.acquireTokenSilent({ ...tokenRequest, account });
      config.headers.Authorization = `Bearer ${result.accessToken}`;
      config.headers['X-Correlation-ID'] = crypto.randomUUID();
      return config;
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        await msalInstance.acquireTokenRedirect({ ...tokenRequest, account });
      }
      throw error;
    }
  });

  client.interceptors.response.use((response) => response, (error) => {
    const status = error.response?.status;
    const messages = {
      401: 'La sesión no es válida o el token expiró.',
      403: 'Tu cuenta no tiene permisos para esta operación.',
      404: 'El recurso solicitado no existe.',
      500: 'El servicio encontró un error interno.',
    };
    error.userMessage = messages[status] || 'No fue posible comunicarse con el servicio.';
    return Promise.reject(error);
  });
  return client;
};

export const reportesApi = createApi('/api/reportes');
export const mapaApi = createApi('/api/mapa');
export const notificacionesApi = createApi('/api/notificaciones');
export const identityApi = createApi('');
export default reportesApi;
