import { createContext } from 'react'
import type { CurrentUser, LoginRequest, RegistroRequest, Rol } from '../types'

/**
 * - loading: hay un token guardado y se está restaurando la sesión.
 * - authenticated: sesión válida con los datos de /auth/me.
 * - anonymous: sin sesión.
 */
export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export interface AuthContextValue {
  status: AuthStatus
  user: CurrentUser | null
  /** true si la última sesión terminó por expiración (no por logout). */
  sessionExpired: boolean
  /** Inicia sesión; lanza ApiRequestError si falla. */
  login: (request: LoginRequest) => Promise<void>
  /** Registra la cuenta y deja la sesión iniciada; lanza ApiRequestError si falla. */
  register: (request: RegistroRequest) => Promise<void>
  logout: () => void
  /** Indica si el usuario tiene al menos uno de los roles indicados. */
  hasRole: (...roles: Rol[]) => boolean
}

export const AuthContext = createContext<AuthContextValue | null>(null)
