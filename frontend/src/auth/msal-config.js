import { LogLevel, PublicClientApplication } from '@azure/msal-browser';

const tenantId = import.meta.env.VITE_ENTRA_TENANT_ID;
const clientId = import.meta.env.VITE_ENTRA_CLIENT_ID;
const authority = import.meta.env.VITE_ENTRA_AUTHORITY
  || (tenantId ? `https://login.microsoftonline.com/${tenantId}` : 'https://login.microsoftonline.com/common');

export const apiScopes = [import.meta.env.VITE_API_SCOPE].filter(Boolean);

export const msalConfig = {
  auth: {
    clientId: clientId || '00000000-0000-0000-0000-000000000000',
    authority,
    redirectUri: import.meta.env.VITE_ENTRA_REDIRECT_URI || window.location.origin,
    postLogoutRedirectUri: import.meta.env.VITE_ENTRA_POST_LOGOUT_REDIRECT_URI || window.location.origin,
    navigateToLoginRequestUrl: true,
  },
  cache: {
    cacheLocation: 'sessionStorage',
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      logLevel: LogLevel.Warning,
      piiLoggingEnabled: false,
      loggerCallback: (level, message, containsPii) => {
        if (!containsPii && level === LogLevel.Error) console.error(message);
      },
    },
  },
};

export const loginRequest = { scopes: ['openid', 'profile', 'email', ...apiScopes] };
export const tokenRequest = { scopes: apiScopes };
export const msalInstance = new PublicClientApplication(msalConfig);
