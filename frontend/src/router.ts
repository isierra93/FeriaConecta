import { createBrowserRouter } from 'react-router'
import { MainLayout } from './components'
import { FeriasPage, HomePage, NotFoundPage } from './pages'

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

      // Rutas protegidas por rol (/organizador/..., /emprendedor/...,
      // /acreditar/:token): se agregan en el issue de Autenticación — Frontend,
      // agrupadas bajo un layout route con el guard de rol.

      { path: '*', Component: NotFoundPage },
    ],
  },
])
