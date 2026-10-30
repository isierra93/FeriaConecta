import { Link } from 'react-router'

export function HomePage() {
  return (
    <section>
      <h1>FeriaConecta</h1>
      <p>Organizá ferias de emprendedores y gestioná tu participación.</p>
      <p>
        <Link to="/ferias">Ver ferias</Link>
      </p>
    </section>
  )
}
