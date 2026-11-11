import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../hooks'
import { AccessDeniedPage } from '../pages/AccessDeniedPage'
import type { Rol } from '../types'

interface ProtectedRouteProps {
  /** Roles admitidos (alcanza con tener uno). Sin roles basta con tener sesión. */
  roles?: Rol[]
}

/**
 * Layout route que exige sesión y, opcionalmente, un rol. Solo mejora la
 * experiencia: la autorización real la aplica el backend en cada petición.
 */
export function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { status, hasRole } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <p className="loading">Cargando…</p>
  }

  if (status === 'anonymous') {
    // Se guarda la ruta pedida para volver a ella tras iniciar sesión.
    const from = `${location.pathname}${location.search}${location.hash}`
    return <Navigate to="/login" replace state={{ from }} />
  }

  if (roles && roles.length > 0 && !hasRole(...roles)) {
    return <AccessDeniedPage />
  }

  return <Outlet />
}

/** Guard para las rutas /organizador/... y /acreditar/:token. */
export function OrganizadorRoute() {
  return <ProtectedRoute roles={['ORGANIZADOR']} />
}

/** Guard para las rutas /emprendedor/... */
export function EmprendedorRoute() {
  return <ProtectedRoute roles={['EMPRENDEDOR']} />
}
