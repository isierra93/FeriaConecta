// Réplica de los DTOs del módulo auth del backend
// (backend/.../auth/dto/*.java). Cambiar uno obliga a cambiar el otro.

/** Rol de usuario; un usuario puede tener ambos. */
export type Rol = 'ORGANIZADOR' | 'EMPRENDEDOR'

/** Respuesta de POST /auth/login y POST /auth/registro. */
export interface AuthResponse {
  token: string
  /** Esquema de autenticación (siempre Bearer). */
  tipo: string
}

/** Respuesta de GET /auth/me. */
export interface CurrentUser {
  nombre: string
  email: string
  roles: Rol[]
}

/** Cuerpo de POST /auth/login. */
export interface LoginRequest {
  email: string
  contrasena: string
}

/** Cuerpo de POST /auth/registro. */
export interface RegistroRequest {
  email: string
  nombre: string
  contrasena: string
  roles: Rol[]
}
