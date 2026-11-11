import { useAuth } from '../hooks'

// Placeholder protegido por rol: lo reemplaza "Gestión de ferias — Frontend"
// (pantalla "Mis ferias").
export function OrganizadorPage() {
  const { user } = useAuth()
  return (
    <section>
      <h1>Panel del organizador</h1>
      <p>
        Hola, {user?.nombre}. Próximamente vas a poder gestionar tus ferias.
      </p>
    </section>
  )
}
