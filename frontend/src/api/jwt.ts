// Lectura del payload del JWT en el cliente. No valida la firma: solo se usa
// para anticipar la expiración de la sesión. La validación real la hace el
// backend en cada petición.

/** Devuelve el vencimiento del token en milisegundos, o null si no se puede leer. */
export function getTokenExpiration(token: string): number | null {
  const payload = token.split('.')[1]
  if (!payload) {
    return null
  }
  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const claims: unknown = JSON.parse(atob(base64))
    if (typeof claims !== 'object' || claims === null || !('exp' in claims)) {
      return null
    }
    return typeof claims.exp === 'number' ? claims.exp * 1000 : null
  } catch {
    return null
  }
}

/** Indica si el token ya venció según su claim exp (sin exp se asume vigente). */
export function isTokenExpired(token: string, now = Date.now()): boolean {
  const expiration = getTokenExpiration(token)
  return expiration !== null && expiration <= now
}
