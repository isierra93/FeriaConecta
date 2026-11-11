import { useState, type FormEvent } from 'react'
import { Link, useLocation } from 'react-router'
import { ApiRequestError } from '../api'
import { FormField } from '../components'
import { useAuth } from '../hooks'
import type { Rol } from '../types'
import {
  MAX_LENGTH,
  compactErrors,
  validateEmail,
  type FieldErrors,
} from './formValidation'
import './AuthForm.css'

const ROLE_OPTIONS: { value: Rol; label: string; description: string }[] = [
  {
    value: 'ORGANIZADOR',
    label: 'Organizador',
    description: 'Creo y gestiono ferias.',
  },
  {
    value: 'EMPRENDEDOR',
    label: 'Emprendedor',
    description: 'Me postulo para vender en ferias.',
  },
]

interface RegistroForm {
  email: string
  nombre: string
  contrasena: string
  confirmacion: string
  roles: Rol[]
}

const EMPTY_FORM: RegistroForm = {
  email: '',
  nombre: '',
  contrasena: '',
  confirmacion: '',
  roles: [],
}

function validate(form: RegistroForm): FieldErrors {
  const nombre = form.nombre.trim()
  return compactErrors({
    email: validateEmail(form.email),
    nombre: !nombre
      ? 'El nombre es obligatorio'
      : nombre.length > MAX_LENGTH
        ? `El nombre no puede superar los ${MAX_LENGTH} caracteres`
        : undefined,
    contrasena: form.contrasena.trim()
      ? undefined
      : 'La contraseña es obligatoria',
    confirmacion:
      form.confirmacion === form.contrasena
        ? undefined
        : 'Las contraseñas no coinciden',
    roles:
      form.roles.length > 0 ? undefined : 'Debe seleccionar al menos un rol',
  })
}

export function RegistroPage() {
  const { register } = useAuth()
  const location = useLocation()
  const [form, setForm] = useState<RegistroForm>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function updateField(
    field: 'email' | 'nombre' | 'contrasena' | 'confirmacion',
    value: string,
  ) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function toggleRole(rol: Rol) {
    setForm((prev) => ({
      ...prev,
      roles: prev.roles.includes(rol)
        ? prev.roles.filter((r) => r !== rol)
        : [...prev.roles, rol],
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const errors = validate(form)
    setFieldErrors(errors)
    setFormError(null)
    if (Object.keys(errors).length > 0) {
      return
    }

    setSubmitting(true)
    try {
      // La cuenta queda con sesión iniciada; GuestRoute redirige.
      await register({
        email: form.email.trim(),
        nombre: form.nombre.trim(),
        contrasena: form.contrasena,
        roles: form.roles,
      })
    } catch (error) {
      if (error instanceof ApiRequestError && error.status === 409) {
        setFieldErrors({ email: error.message })
      } else if (error instanceof ApiRequestError) {
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
      <h1>Crear cuenta</h1>

      {formError && (
        <p className="form-error" role="alert">
          {formError}
        </p>
      )}

      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <FormField
          id="registro-nombre"
          label="Nombre"
          autoComplete="name"
          maxLength={MAX_LENGTH}
          value={form.nombre}
          onChange={(event) => updateField('nombre', event.target.value)}
          error={fieldErrors.nombre}
        />
        <FormField
          id="registro-email"
          label="Email"
          type="email"
          autoComplete="email"
          maxLength={MAX_LENGTH}
          value={form.email}
          onChange={(event) => updateField('email', event.target.value)}
          error={fieldErrors.email}
        />
        <FormField
          id="registro-contrasena"
          label="Contraseña"
          type="password"
          autoComplete="new-password"
          value={form.contrasena}
          onChange={(event) => updateField('contrasena', event.target.value)}
          error={fieldErrors.contrasena}
        />
        <FormField
          id="registro-confirmacion"
          label="Repetir contraseña"
          type="password"
          autoComplete="new-password"
          value={form.confirmacion}
          onChange={(event) => updateField('confirmacion', event.target.value)}
          error={fieldErrors.confirmacion}
        />

        <fieldset
          className="form-field role-options"
          aria-describedby={
            fieldErrors.roles ? 'registro-roles-error' : undefined
          }
        >
          <legend>¿Cómo vas a usar FeriaConecta?</legend>
          {ROLE_OPTIONS.map((option) => (
            <label key={option.value} className="role-option">
              <input
                type="checkbox"
                checked={form.roles.includes(option.value)}
                onChange={() => toggleRole(option.value)}
              />
              <span>
                <strong>{option.label}</strong> {option.description}
              </span>
            </label>
          ))}
          <p className="field-hint">Podés elegir los dos.</p>
          {fieldErrors.roles && (
            <p id="registro-roles-error" className="field-error">
              {fieldErrors.roles}
            </p>
          )}
        </fieldset>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>

      <p className="auth-alt">
        ¿Ya tenés cuenta?{' '}
        <Link to="/login" state={location.state}>
          Iniciá sesión
        </Link>
      </p>
    </section>
  )
}
