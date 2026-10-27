# FeriaConecta — Frontend

Frontend de FeriaConecta, una plataforma web para organizar ferias y
conectar emprendedores.

Forma parte del repositorio principal y se encuentra en `/frontend`.

## Tecnologías

- React y TypeScript.
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

> El backend todavía no aplica la configuración de CORS; se implementa en
> el issue "Configurar CORS en el backend". Hasta entonces, las peticiones
> desde el navegador hacia la API son bloqueadas.

### Verificación del despliegue

- El build del deploy finaliza sin errores en el panel de Vercel.
- `https://feria-conecta.vercel.app/ferias` responde con la aplicación al
  recargar la página (sin 404 de Vercel).
