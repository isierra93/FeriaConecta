import { createBrowserRouter } from 'react-router'
import {
  EmprendedorRoute,
  GuestRoute,
  MainLayout,
  OrganizadorRoute,
} from './components'
import {
  EmprendedorPage,
  FeriasPage,
  HomePage,
  LoginPage,
  NotFoundPage,
  OrganizadorPage,
  RegistroPage,
} from './pages'

// Configuración central de rutas. La convención (públicas, por rol y 404)
// está documentada en frontend/README.md, sección "Rutas".
export const router = createBrowserRouter([
  {
    path: '/',
    Component: MainLayout,
    children: [
      // Rutas públicas
      { index: true, Component: HomePage },
      { path: 'ferias', Component: FeriasPage },

      // Solo sin sesión
      {
        Component: GuestRoute,
        children: [
          { path: 'login', Component: LoginPage },
          { path: 'registro', Component: RegistroPage },
        ],
      },

      // Rol ORGANIZADOR (/organizador/..., /acreditar/:token)
      {
        Component: OrganizadorRoute,
        children: [{ path: 'organizador', Component: OrganizadorPage }],
      },

      // Rol EMPRENDEDOR (/emprendedor/...)
      {
        Component: EmprendedorRoute,
        children: [{ path: 'emprendedor', Component: EmprendedorPage }],
      },

      { path: '*', Component: NotFoundPage },
    ],
  },
])
