import React from 'react';
import ReactDOM from 'react-dom/client';
import { MsalProvider } from '@azure/msal-react';
import App from './App.jsx';
import './index.css';
import { msalInstance } from './auth/msal-config';

await msalInstance.initialize();
const redirectResult = await msalInstance.handleRedirectPromise();
if (redirectResult?.account) msalInstance.setActiveAccount(redirectResult.account);
else if (!msalInstance.getActiveAccount() && msalInstance.getAllAccounts()[0]) {
  msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode><MsalProvider instance={msalInstance}><App /></MsalProvider></React.StrictMode>,
);
