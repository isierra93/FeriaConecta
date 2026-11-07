// Réplica del contrato de error del backend
// (backend/.../common/dto/ApiError.java). Cambiar uno obliga a cambiar el otro.

/** Error de validación sobre un campo. */
export interface FieldErrorDetail {
  field: string
  message: string
}

/** Cuerpo de error común que devuelve toda la API. */
export interface ApiError {
  /** Momento del error (UTC, ISO-8601). */
  timestamp: string
  /** Código HTTP. */
  status: number
  /** Frase estándar del código HTTP (por ejemplo, "Not Found"). */
  error: string
  /** Mensaje legible en español. */
  message: string
  /** URI de la petición (incluye /api/v1). */
  path: string
  /** Errores por campo; solo presente en errores de validación. */
  details?: FieldErrorDetail[]
}
