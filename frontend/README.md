# FeriaConecta — Frontend

Frontend de FeriaConecta, una plataforma web para organizar ferias y
conectar emprendedores.

Forma parte del repositorio principal y se encuentra en `/frontend`.

## Tecnologías

- React y TypeScript.
- React Router v7 (paquete `react-router`) para la navegación.
- Vite como herramienta de desarrollo y compilación.
- pnpm para gestionar dependencias.
- ESLint para analizar el código.
- Prettier para mantener un formato consistente.

## Entorno utilizado

- Node.js 24.
- pnpm 10.33.0, declarado en `package.json`. Si no está instalado, se
  puede habilitar con `corepack enable` (incluido en Node.js).

Los comandos siguientes deben ejecutarse desde la carpeta `frontend`.

## Instalación y ejecución

Instalar las dependencias:

```bash
pnpm install
```

Crear la configuración local a partir del archivo de ejemplo:

```bash
cp .env.example .env.local
```

Revisar `VITE_API_URL` en `.env.local` y ajustar la dirección según la
configuración del backend. El valor inicial de ejemplo es:

```dotenv
VITE_API_URL=http://localhost:8080/api/v1
```

Iniciar el servidor de desarrollo:

```bash
pnpm dev
```

Abrir la dirección que muestra la terminal.

## Comandos disponibles

| Comando             | Función                                                   |
| ------------------- | --------------------------------------------------------- |
| `pnpm dev`          | Inicia el servidor de desarrollo.                         |
| `pnpm lint`         | Analiza el código con ESLint.                             |
| `pnpm format`       | Aplica el formato de Prettier.                            |
| `pnpm format:check` | Comprueba el formato sin modificar archivos.              |
| `pnpm build`        | Comprueba los tipos y genera el build en `dist`.          |
| `pnpm preview`      | Permite revisar localmente un build generado previamente. |

## Organización del código

El código del frontend se encuentra en `src`.

| Carpeta      | Responsabilidad                                         |
| ------------ | ------------------------------------------------------- |
| `pages`      | Pantallas completas de la aplicación.                   |
| `components` | Componentes visuales reutilizables.                     |
| `api`        | Funciones de comunicación con el backend.               |
| `hooks`      | Hooks personalizados para reutilizar lógica de React.   |
| `types`      | Tipos e interfaces compartidos de TypeScript.           |
| `context`    | Contextos y proveedores de React para compartir estado. |
| `assets`     | Recursos estáticos importados desde el código.          |

## Rutas

La navegación usa React Router v7 en modo _data router_
(`createBrowserRouter` + `RouterProvider`). Todas las rutas se definen
en `src/router.ts`; las pantallas viven en `src/pages` y el layout con
la navegación principal (`MainLayout`) en `src/components`.

### Convención

| Ruta                          | Acceso                         |
| ----------------------------- | ------------------------------ |
| `/`, `/ferias`, `/ferias/:id` | Pública.                       |
| `/login`, `/registro`         | Pública (solo sin sesión).     |
| `/organizador/...`            | Requiere el rol `ORGANIZADOR`. |
| `/emprendedor/...`            | Requiere el rol `EMPRENDEDOR`. |
| `/acreditar/:token`           | Requiere el rol `ORGANIZADOR`. |
| `*`                           | Página 404 (`NotFoundPage`).   |

- Los segmentos de las rutas están en español y en minúsculas
  (`kebab-case` si tienen varias palabras), porque son términos del
  dominio: `/organizador/ferias/:id/puestos`.
- Las pantallas exclusivas de un rol van bajo su prefijo
  (`/organizador/...` o `/emprendedor/...`).
- `/acreditar/:token` es una excepción al prefijo: es la URL que
  contiene la credencial QR, por lo que debe mantenerse corta y estable.
  Tras iniciar sesión, el usuario vuelve a la ruta original.
- Todas las rutas son hijas de `MainLayout`, incluida la 404, para
  conservar la navegación.

### Rutas protegidas

Las rutas protegidas se agrupan bajo un _layout route_ sin `path`
(`src/components/ProtectedRoute.tsx`) que renderiza un `<Outlet />` si
el usuario cumple los requisitos. `OrganizadorRoute` y
`EmprendedorRoute` son el mismo guard con el rol ya fijado:

```ts
{
  Component: OrganizadorRoute, // ProtectedRoute con roles={['ORGANIZADOR']}
  children: [
    { path: 'organizador/ferias', Component: MisFeriasPage },
    { path: 'acreditar/:token', Component: AcreditarPage },
  ],
}
```

- Mientras se restaura la sesión: muestra "Cargando…".
- Sin sesión: redirige a `/login` y guarda la ruta solicitada en
  `location.state.from` para volver a ella al iniciar sesión o
  registrarse.
- Con sesión pero sin el rol requerido: muestra `AccessDeniedPage`
  sin cambiar la URL.
- `/login` y `/registro` están bajo `GuestRoute`: con sesión iniciada
  redirigen a la ruta guardada (o a `/`). Es la única redirección
  posterior al login; las pantallas no navegan por su cuenta.
- El guard solo mejora la experiencia: la autorización real la aplica
  el backend en cada petición.

## Autenticación

La sesión vive en `AuthProvider` (`src/context/`), que envuelve al
`RouterProvider` en `main.tsx`. Los componentes la leen con
`useAuth()` (`src/hooks/`):

```ts
const { status, user, hasRole, login, register, logout } = useAuth()
```

- `status`: `loading` (restaurando), `authenticated` o `anonymous`.
- `login` y `register` llaman a `/auth/login` y `/auth/registro`,
  guardan el token y cargan el usuario desde `/auth/me`. Tras
  registrarse, el usuario queda con la sesión iniciada. Si fallan,
  lanzan `ApiRequestError` para que el formulario muestre el error.
- Al recargar la página, si hay un token guardado y vigente, se
  restaura la sesión consultando `/auth/me`.
- `hasRole(...roles)` indica si el usuario tiene al menos uno de los
  roles. Un usuario puede tener `ORGANIZADOR` y `EMPRENDEDOR`, y la
  navegación muestra las secciones de ambos.

### Expiración de la sesión

La sesión se cierra (con el aviso "Tu sesión expiró" en el login) en
dos casos:

- El backend responde `401` a una petición que llevaba token
  (`onUnauthorized` del cliente HTTP).
- Llega el `exp` del JWT: se programa un temporizador al iniciar
  sesión, sin esperar a la próxima petición. El `exp` se lee del
  payload sin validar la firma (`src/api/jwt.ts`).

### Almacenamiento del token

El JWT se guarda en `localStorage` (clave `feriaconecta.token`). Es
simple y sobrevive a la recarga de la página, pero cualquier script
que se ejecute en la página puede leerlo, por lo que queda expuesto a
XSS. Es una decisión aceptada para el alcance del proyecto; la
mitigación es no insertar HTML sin escapar (React escapa por defecto)
y no agregar scripts de terceros innecesarios. La alternativa más
segura (cookie `HttpOnly` emitida por el backend) queda fuera de
alcance.

El rewrite de `vercel.json` hacia `index.html` permite acceder
directamente o recargar cualquiera de estas rutas en el despliegue.

## Cliente HTTP

Todas las llamadas al backend pasan por el cliente de `src/api/`
(`fetch` nativo, sin dependencias):

```ts
import { apiClient, ApiRequestError } from '../api'

const ferias = await apiClient.get<Feria[]>('/ferias')
await apiClient.post('/auth/login', credenciales, { auth: false })
```

- La URL base se toma de `VITE_API_URL`, que ya incluye `/api/v1`. Las
  rutas se escriben relativas y sin ese prefijo (`/auth/login`).
- Si hay un token guardado (`tokenStorage`), se envía como
  `Authorization: Bearer <token>`. Con `{ auth: false }` no se adjunta.
- Cualquier respuesta no exitosa lanza `ApiRequestError`, con `status`,
  `message` (el mensaje del backend, en español), `apiError` (el cuerpo
  `ApiError` completo) y `fieldErrors` (errores de validación por
  campo). Sin conexión con el servidor, `status` es `0`.
- Un `401` en una petición que llevaba token se considera sesión
  expirada: se borra el token y se notifica a los callbacks registrados
  con `onUnauthorized(callback)`, que devuelve la función para
  desuscribirse. El `AuthContext` usa este evento para cerrar la sesión
  y redirigir al login.

## Convenciones

- Componentes y páginas: PascalCase, por ejemplo `FeriaCard.tsx`.
- Hooks personalizados: camelCase con el prefijo `use`, por ejemplo
  `useFiltros.ts`.
- Carpetas de módulos: minúsculas, por ejemplo `ferias`.
- TypeScript utiliza `strict: true`, compartido mediante herencia entre
  las configuraciones de la aplicación y de Vite.
- ESLint marca el uso explícito de `any` como error. Cualquier excepción
  debe justificarse.
- El formato se define en `.prettierrc.json`.
- Se utiliza pnpm y se incluye `pnpm-lock.yaml` en el repositorio.

## Verificación de cambios

Antes de crear un commit, ejecutar:

```bash
pnpm format:check
pnpm lint
pnpm build
```

Si se detectan diferencias de formato, ejecutar `pnpm format` y volver
a comprobarlo.

Revisar también los cambios preparados en Git antes de confirmar el commit.

## Despliegue en Vercel

El frontend se despliega desde este repositorio mediante la integración
de GitHub con Vercel.

| Entorno            | URL                                            |
| ------------------ | ---------------------------------------------- |
| Frontend           | https://feria-conecta.vercel.app               |
| API (backend, VPS) | https://api.feriaconecta.isierra93.site/api/v1 |

Configuración del proyecto:

| Campo             | Valor      |
| ----------------- | ---------- |
| Framework Preset  | Vite       |
| Root Directory    | frontend   |
| Build Command     | pnpm build |
| Output Directory  | dist       |
| Production Branch | main       |
| Node.js Version   | 24.x       |

Cada cambio integrado en `main` genera un despliegue de producción. Los
pull requests generan despliegues de preview con una URL propia.

El archivo `vercel.json` reescribe todas las rutas hacia `index.html` para
permitir el acceso directo y la recarga de rutas de la aplicación (por
ejemplo `/ferias`). Los archivos que existen en `dist` (JS, CSS, imágenes)
se sirven antes de aplicar el rewrite.

### API y variables de entorno

En Vercel (entorno Production) se configura `VITE_API_URL` con la URL
pública del backend, incluyendo el prefijo `/api/v1`:

```dotenv
VITE_API_URL=https://api.feriaconecta.isierra93.site/api/v1
```

El valor local de desarrollo está documentado en `.env.example`.

Al modificar `VITE_API_URL` en Vercel, se necesita un nuevo despliegue
para incorporar el valor al build.

### CORS

El origen `https://feria-conecta.vercel.app` está registrado en las
variables `APP_FRONTEND_URL` y `CORS_ALLOWED_ORIGINS` del backend (stack
de Portainer). Solo se permite el dominio de producción: las URLs de
preview de Vercel no se agregan a CORS.

### Verificación del despliegue

- El build del deploy finaliza sin errores en el panel de Vercel.
- `https://feria-conecta.vercel.app/ferias` responde con la aplicación al
  recargar la página (sin 404 de Vercel).
