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
- pnpm 10.33.0, declarado en `package.json`.

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
