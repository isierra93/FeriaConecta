import { useState, type FormEvent } from 'react'
import { Link, useLocation } from 'react-router'
import { ApiRequestError } from '../api'
import { FormField } from '../components'
import { useAuth } from '../hooks'
import {
  compactErrors,
  validateEmail,
  type FieldErrors,
} from './formValidation'
import './AuthForm.css'

function validate(email: string, contrasena: string): FieldErrors {
  return compactErrors({
    email: validateEmail(email),
    contrasena: contrasena ? undefined : 'La contraseña es obligatoria',
  })
}

export function LoginPage() {
  const { login, sessionExpired } = useAuth()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const errors = validate(email, contrasena)
    setFieldErrors(errors)
    setFormError(null)
    if (Object.keys(errors).length > 0) {
      return
    }

    setSubmitting(true)
    try {
      // Al quedar autenticado, GuestRoute redirige a la ruta pedida.
      await login({ email: email.trim(), contrasena })
    } catch (error) {
      if (error instanceof ApiRequestError) {
        setFieldErrors(error.fieldErrors)
        setFormError(error.message)
      } else {
        setFormError('Ocurrió un error inesperado. Intentá nuevamente.')
      }
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page">
      <h1>Iniciar sesión</h1>

      {sessionExpired && !formError && (
        <p className="form-notice" role="status">
          Tu sesión expiró. Volvé a iniciar sesión para continuar.
        </p>
      )}
      {formError && (
        <p className="form-error" role="alert">
          {formError}
        </p>
      )}

      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <FormField
          id="login-email"
          label="Email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          error={fieldErrors.email}
        />
        <FormField
          id="login-contrasena"
          label="Contraseña"
          type="password"
          autoComplete="current-password"
          value={contrasena}
          onChange={(event) => setContrasena(event.target.value)}
          error={fieldErrors.contrasena}
        />
        <button type="submit" disabled={submitting}>
          {submitting ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>

      <p className="auth-alt">
        ¿No tenés cuenta?{' '}
        <Link to="/registro" state={location.state}>
          Registrate
        </Link>
      </p>
    </section>
  )
}
