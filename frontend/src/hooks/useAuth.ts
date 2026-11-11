import { useContext } from 'react'
import { AuthContext, type AuthContextValue } from '../context/authContext'

/** Acceso a la sesión actual. Requiere estar dentro de <AuthProvider>. */
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>.')
  }
  return context
}
