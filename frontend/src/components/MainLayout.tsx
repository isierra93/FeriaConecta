import { Link, NavLink, Outlet } from 'react-router'
import './MainLayout.css'

export function MainLayout() {
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
        </nav>
      </header>
      <main className="main-content">
        <Outlet />
      </main>
    </>
  )
}
