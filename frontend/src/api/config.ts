const rawApiUrl = import.meta.env.VITE_API_URL

if (!rawApiUrl) {
  throw new Error(
    'Falta la variable de entorno VITE_API_URL. Copiar .env.example a .env.local y configurarla.',
  )
}

/** URL base del backend sin barra final (ya incluye /api/v1). */
export const API_URL = rawApiUrl.replace(/\/+$/, '')
