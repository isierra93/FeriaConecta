import { useAuth } from '../hooks'

// Placeholder protegido por rol: lo reemplaza "Postulaciones — Frontend"
// (pantalla de postulaciones del emprendedor).
export function EmprendedorPage() {
  const { user } = useAuth()
  return (
    <section>
      <h1>Panel del emprendedor</h1>
      <p>
        Hola, {user?.nombre}. Próximamente vas a poder ver tus postulaciones.
      </p>
    </section>
  )
}
