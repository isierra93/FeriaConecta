import type { ApiError } from '../types'

/** Estado usado cuando la petición no obtuvo respuesta (red caída, CORS). */
export const NETWORK_ERROR_STATUS = 0

/** Comprueba si un cuerpo de respuesta tiene el formato ApiError del backend. */
export function isApiError(body: unknown): body is ApiError {
  if (typeof body !== 'object' || body === null) {
    return false
  }
  const candidate = body as Record<string, unknown>
  return (
    typeof candidate.status === 'number' &&
    typeof candidate.message === 'string'
  )
}

/** Error del frontend para cualquier petición fallida a la API. */
export class ApiRequestError extends Error {
  /** Código HTTP, o 0 si no hubo respuesta. */
  readonly status: number
  /** Cuerpo de error del backend, si la respuesta lo incluía. */
  readonly apiError: ApiError | null

  constructor(status: number, message: string, apiError: ApiError | null) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.apiError = apiError
  }

  /** Indica que no se pudo contactar al servidor. */
  get isNetworkError(): boolean {
    return this.status === NETWORK_ERROR_STATUS
  }

  /** Errores de validación indexados por campo, listos para formularios. */
  get fieldErrors(): Record<string, string> {
    const result: Record<string, string> = {}
    for (const detail of this.apiError?.details ?? []) {
      result[detail.field] ??= detail.message
    }
    return result
  }
}
