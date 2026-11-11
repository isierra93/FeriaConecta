import { Link } from 'react-router'

/** Se muestra cuando el usuario tiene sesión pero no el rol requerido. */
export function AccessDeniedPage() {
  return (
    <section>
      <h1>Acceso denegado</h1>
      <p>No tenés permiso para ver esta sección.</p>
      <p>
        <Link to="/">Volver al inicio</Link>
      </p>
    </section>
  )
}
