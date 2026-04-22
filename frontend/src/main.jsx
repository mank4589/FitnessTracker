import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';

console.log('[main.jsx] Script loaded');

try {
  const container = document.getElementById('app');
  console.log('[main.jsx] Container element:', container);
  
  if (!container) {
    console.error('[main.jsx] FATAL: #app element not found!');
  } else {
    const root = ReactDOM.createRoot(container);
    console.log('[main.jsx] Root created, attempting render...');
    root.render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    );
    console.log('[main.jsx] render() called successfully');
  }
} catch (err) {
  console.error('[main.jsx] FATAL ERROR:', err);
  document.body.innerHTML = '<pre style="color:red">' + err.stack + '</pre>';
}
