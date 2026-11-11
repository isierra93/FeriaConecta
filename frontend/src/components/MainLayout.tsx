import { Link, NavLink, Outlet } from 'react-router'
import { useAuth } from '../hooks'
import './MainLayout.css'

export function MainLayout() {
  const { status, user, hasRole, logout } = useAuth()

  return (
    <>
      <header className="main-header">
        <Link to="/" className="brand">
          FeriaConecta
        </Link>
        <nav aria-label="Navegación principal">
          <NavLink to="/" end>
            Inicio
          </NavLink>
          <NavLink to="/ferias">Ferias</NavLink>
          {hasRole('ORGANIZADOR') && (
            <NavLink to="/organizador">Organizador</NavLink>
          )}
          {hasRole('EMPRENDEDOR') && (
            <NavLink to="/emprendedor">Emprendedor</NavLink>
          )}
        </nav>
        {status !== 'loading' && (
          <div className="session" aria-label="Sesión">
            {user ? (
              <>
                <span className="session-user">{user.nombre}</span>
                <button type="button" className="link-button" onClick={logout}>
                  Cerrar sesión
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login">Iniciar sesión</NavLink>
                <NavLink to="/registro">Registrarse</NavLink>
              </>
            )}
          </div>
        )}
      </header>
      <main className="main-content">
        <Outlet />
      </main>
    </>
  )
}
