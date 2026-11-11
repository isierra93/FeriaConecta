import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  ApiRequestError,
  authApi,
  getTokenExpiration,
  isTokenExpired,
  onUnauthorized,
  tokenStorage,
} from '../api'
import type { AuthResponse, CurrentUser, Rol } from '../types'
import {
  AuthContext,
  type AuthContextValue,
  type AuthStatus,
} from './authContext'

interface AuthState {
  status: AuthStatus
  user: CurrentUser | null
  sessionExpired: boolean
}

// setTimeout no admite demoras mayores a ~24,8 días.
const MAX_TIMEOUT_MS = 2 ** 31 - 1

function initialState(): AuthState {
  const token = tokenStorage.getToken()
  const restoring = token !== null && !isTokenExpired(token)
  return {
    status: restoring ? 'loading' : 'anonymous',
    user: null,
    sessionExpired: false,
  }
}

/**
 * Mantiene la sesión del usuario. No navega por su cuenta: cuando la sesión
 * termina, los guards de rutas redirigen al login.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(initialState)

  const expire = useCallback(() => {
    tokenStorage.clearToken()
    setState({ status: 'anonymous', user: null, sessionExpired: true })
  }, [])

  // Restaura la sesión al recargar la página.
  useEffect(() => {
    const token = tokenStorage.getToken()
    if (!token) {
      return
    }
    if (isTokenExpired(token)) {
      tokenStorage.clearToken()
      return
    }

    const controller = new AbortController()
    authApi
      .getCurrentUser(controller.signal)
      .then((user) => {
        setState({ status: 'authenticated', user, sessionExpired: false })
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) {
          return
        }
        // Sin conexión se conserva el token para reintentar al recargar.
        if (!(error instanceof ApiRequestError && error.isNetworkError)) {
          tokenStorage.clearToken()
        }
        // Un 401 ya marcó la sesión como expirada vía onUnauthorized.
        setState((prev) => ({ ...prev, status: 'anonymous', user: null }))
      })

    return () => controller.abort()
  }, [])

  // Sesión expirada detectada por el cliente HTTP (401 con token enviado).
  useEffect(() => onUnauthorized(expire), [expire])

  // Cierra la sesión cuando vence el token, sin esperar a la próxima petición.
  useEffect(() => {
    if (state.status !== 'authenticated') {
      return
    }
    const token = tokenStorage.getToken()
    const expiration = token ? getTokenExpiration(token) : null
    if (expiration === null) {
      return
    }
    const delay = Math.max(expiration - Date.now(), 0)
    if (delay > MAX_TIMEOUT_MS) {
      return
    }
    const timeoutId = setTimeout(expire, delay)
    return () => clearTimeout(timeoutId)
  }, [state.status, expire])

  const startSession = useCallback(async ({ token }: AuthResponse) => {
    tokenStorage.setToken(token)
    try {
      const user = await authApi.getCurrentUser()
      setState({ status: 'authenticated', user, sessionExpired: false })
    } catch (error) {
      tokenStorage.clearToken()
      throw error
    }
  }, [])

  const value = useMemo<AuthContextValue>(() => {
    const { user } = state
    return {
      ...state,
      login: async (request) => startSession(await authApi.login(request)),
      register: async (request) =>
        startSession(await authApi.register(request)),
      logout: () => {
        tokenStorage.clearToken()
        setState({ status: 'anonymous', user: null, sessionExpired: false })
      },
      hasRole: (...roles: Rol[]) =>
        user !== null && roles.some((rol) => user.roles.includes(rol)),
    }
  }, [state, startSession])

  return <AuthContext value={value}>{children}</AuthContext>
}
