import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import process from 'node:process'

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), '')
  if (command === 'build' && mode === 'production') {
    const value = env.VITE_API_URL
    if (!value) throw new Error('VITE_API_URL es obligatoria para producción y debe ser el origen HTTPS de Azure API Management.')
    const url = new URL(value)
    if (url.protocol !== 'https:' || ['localhost', '127.0.0.1'].includes(url.hostname) || url.pathname !== '/') {
      throw new Error('VITE_API_URL de producción debe ser un origen HTTPS de APIM sin ruta (ejemplo: https://mi-apim.azure-api.net).')
    }
  }
  return {
    plugins: [react()],
    server: { host: true },
  }
})
