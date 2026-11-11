import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../hooks'

/** Ruta interna guardada por ProtectedRoute, si es válida. */
function getRedirectTarget(state: unknown): string {
  if (typeof state === 'object' && state !== null && 'from' in state) {
    const { from } = state
    // Solo rutas internas, para no redirigir a otro sitio.
    if (typeof from === 'string' && from.startsWith('/')) {
      return from
    }
  }
  return '/'
}

/**
 * Layout route para /login y /registro: solo se ven sin sesión. Al iniciar
 * sesión redirige a la ruta que el usuario había pedido (o al inicio).
 */
export function GuestRoute() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <p className="loading">Cargando…</p>
  }

  if (status === 'authenticated') {
    return <Navigate to={getRedirectTarget(location.state)} replace />
  }

  return <Outlet />
}
