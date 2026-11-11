// Validaciones de formularios alineadas con las del backend
// (backend/.../auth/dto/RegistroRequest.java y LoginRequest.java).

export type FieldErrors = Record<string, string>

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const MAX_LENGTH = 150

export function validateEmail(email: string): string | undefined {
  if (!email.trim()) {
    return 'El email es obligatorio'
  }
  if (!EMAIL_PATTERN.test(email.trim())) {
    return 'El formato del email es inválido'
  }
  if (email.trim().length > MAX_LENGTH) {
    return `El email no puede superar los ${MAX_LENGTH} caracteres`
  }
  return undefined
}

/** Quita las entradas sin error para saber si el formulario es válido. */
export function compactErrors(
  errors: Record<string, string | undefined>,
): FieldErrors {
  const result: FieldErrors = {}
  for (const [field, message] of Object.entries(errors)) {
    if (message) {
      result[field] = message
    }
  }
  return result
}
