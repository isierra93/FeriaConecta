// Persistencia del token JWT. El cliente HTTP solo lo lee; el AuthContext lo
// escribe al iniciar o cerrar sesión. Los accesos a localStorage van en
// try/catch porque puede no estar disponible (modo privado, bloqueo del navegador).

const TOKEN_KEY = 'feriaconecta.token'

function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    // Sin almacenamiento disponible la sesión no se persiste.
  }
}

function clearToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // Nada que limpiar.
  }
}

export const tokenStorage = { getToken, setToken, clearToken }
