import { API_URL } from './config'
import { ApiRequestError, NETWORK_ERROR_STATUS, isApiError } from './errors'
import { tokenStorage } from './tokenStorage'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export interface RequestOptions {
  headers?: Record<string, string>
  signal?: AbortSignal
  /** Si es false no se adjunta el token JWT (por ejemplo, en /auth/login). */
  auth?: boolean
}

type UnauthorizedListener = () => void

const unauthorizedListeners = new Set<UnauthorizedListener>()

/**
 * Registra un callback para cuando la sesión expira (401 con token enviado).
 * Devuelve una función para desuscribirse, pensada para el cleanup de useEffect.
 */
export function onUnauthorized(listener: UnauthorizedListener): () => void {
  unauthorizedListeners.add(listener)
  return () => {
    unauthorizedListeners.delete(listener)
  }
}

function notifyUnauthorized(): void {
  for (const listener of unauthorizedListeners) {
    listener()
  }
}

/** Lee el cuerpo como JSON; devuelve undefined si está vacío y texto si no es JSON. */
async function parseBody(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) {
    return undefined
  }
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

async function request<T>(
  method: HttpMethod,
  path: string,
  body: unknown,
  options: RequestOptions = {},
): Promise<T> {
  const { headers: extraHeaders, signal, auth = true } = options
  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...extraHeaders,
  }

  let payload: BodyInit | undefined
  if (body instanceof FormData) {
    // El navegador define el Content-Type con el boundary.
    payload = body
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }

  const token = auth ? tokenStorage.getToken() : null
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: payload,
      signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw error
    }
    throw new ApiRequestError(
      NETWORK_ERROR_STATUS,
      'No se pudo conectar con el servidor. Verificá tu conexión e intentá nuevamente.',
      null,
    )
  }

  const data = await parseBody(response)

  if (!response.ok) {
    const apiError = isApiError(data) ? data : null

    // Solo es "sesión expirada" si se envió un token: un 401 sin token
    // (por ejemplo, credenciales inválidas en el login) es un error común.
    if (response.status === 401 && token) {
      tokenStorage.clearToken()
      notifyUnauthorized()
    }

    throw new ApiRequestError(
      response.status,
      apiError?.message ??
        `Ocurrió un error inesperado (código ${response.status}).`,
      apiError,
    )
  }

  return data as T
}

/**
 * Cliente HTTP centralizado. Las rutas son relativas a VITE_API_URL, que ya
 * incluye /api/v1: usar '/auth/login', no '/api/v1/auth/login'.
 */
export const apiClient = {
  get: <T>(path: string, options?: RequestOptions) =>
    request<T>('GET', path, undefined, options),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('POST', path, body, options),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PUT', path, body, options),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PATCH', path, body, options),
  delete: <T>(path: string, options?: RequestOptions) =>
    request<T>('DELETE', path, undefined, options),
}
