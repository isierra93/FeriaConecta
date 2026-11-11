import type {
  AuthResponse,
  CurrentUser,
  LoginRequest,
  RegistroRequest,
} from '../types'
import { apiClient } from './client'

/** Endpoints del módulo de autenticación. */
export const authApi = {
  login: (body: LoginRequest) =>
    apiClient.post<AuthResponse>('/auth/login', body, { auth: false }),

  register: (body: RegistroRequest) =>
    apiClient.post<AuthResponse>('/auth/registro', body, { auth: false }),

  getCurrentUser: (signal?: AbortSignal) =>
    apiClient.get<CurrentUser>('/auth/me', { signal }),
}
