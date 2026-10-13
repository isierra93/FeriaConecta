# FeriaConecta — Backend

API REST de FeriaConecta en Java 21 + Spring Boot. Todos los endpoints viven bajo
el context-path `/api/v1`.

## Cómo ejecutar

Desde la carpeta `backend/`:

```bash
./mvnw spring-boot:run      # arranca con el perfil dev (H2 en memoria)
./mvnw test                 # ejecuta los tests
./mvnw clean package        # genera el .jar
```

Health check:

```bash
curl http://localhost:8080/api/v1/actuator/health   # {"status":"UP"}
```

### Perfiles

| Perfil | Base de datos | Uso |
|--------|---------------|-----|
| `dev` (por defecto) | H2 en memoria, consola en `/api/v1/h2-console` | Desarrollo local. Arranca sin `.env` gracias a valores por defecto de desarrollo. |
| `prod` | MySQL (`ddl-auto=validate`, esquema en `database/schema.sql`) | Despliegue. Todas las variables obligatorias deben estar definidas. |

Para usar `prod` localmente: `SPRING_PROFILES_ACTIVE=prod` en el `.env` o en el entorno.

## Configuración por variables de entorno

La configuración sensible **nunca** se escribe en el código ni en los `.yml`:
se lee desde variables de entorno.

- **Local:** copiar el ejemplo y completar los valores.
  ```bash
  cp .env.example .env
  ```
  Spring lee automáticamente `backend/.env` (si existe) al ejecutar desde esta carpeta.
  El archivo `.env` está ignorado por git.
- **Producción:** las variables se cargan en el stack de Portainer, no en el repositorio.

### Variables

| Variable | Propiedad Spring | Obligatoria en prod | Default en dev | Descripción | Dónde se obtiene |
|----------|------------------|---------------------|----------------|-------------|------------------|
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active` | Sí (`prod`) | `dev` | Perfil activo de Spring. | Se define en el entorno. |
| `DB_URL` | `spring.datasource.url` | Sí | H2 en memoria | URL JDBC de MySQL, p. ej. `jdbc:mysql://host:3306/feriaconecta`. | Contenedor MySQL (red Docker en el VPS o Docker local). |
| `DB_USERNAME` | `spring.datasource.username` | Sí | `sa` | Usuario de la base de datos. | Se define al crear el usuario de MySQL. |
| `DB_PASSWORD` | `spring.datasource.password` | Sí | vacío | Contraseña de la base de datos. | Se define al crear el usuario de MySQL. |
| `JWT_SECRET` | `feriaconecta.jwt.secret` | Sí | secreto de desarrollo | Clave para firmar los JWT (HS256, mínimo 256 bits). | Generar con `openssl rand -base64 32`. |
| `JWT_EXPIRATION_MINUTES` | `feriaconecta.jwt.expiration-minutes` | No | `60` | Vigencia del token en minutos. | Decisión del equipo. |
| `MP_ACCESS_TOKEN` | `feriaconecta.mercadopago.access-token` | Sí (desde la integración de pagos) | vacío | Access token privado de la API de Mercado Pago. | [Mercado Pago Developers](https://www.mercadopago.com.ar/developers/panel/app) → Tus integraciones → aplicación → Credenciales **de prueba**. |
| `MP_WEBHOOK_SECRET` | `feriaconecta.mercadopago.webhook-secret` | Sí (desde la integración de pagos) | vacío | Clave secreta para validar el header `x-signature` del webhook. | Mercado Pago Developers → aplicación → Webhooks → clave secreta. |
| `APP_BACKEND_URL` | `feriaconecta.urls.backend` | Sí | `http://localhost:8080/api/v1` | URL pública del backend (incluye `/api/v1`). Se usa como URL de notificación del webhook. | Dominio configurado en Nginx Proxy Manager. |
| `APP_FRONTEND_URL` | `feriaconecta.urls.frontend` | Sí | `http://localhost:5173` | URL base del frontend (URLs de retorno de Mercado Pago, enlaces de la credencial). | URL del deploy en Vercel. |
| `CORS_ALLOWED_ORIGINS` | `feriaconecta.cors.allowed-origins` | Sí | `http://localhost:5173` | Orígenes permitidos para CORS, separados por coma. | URL del deploy en Vercel. |
| `PAGO_PLAZO_HORAS` | `feriaconecta.pago.plazo-horas` | No | `72` | Horas que tiene el emprendedor para pagar tras la aprobación; luego la postulación pasa a `VENCIDA`. | Regla de negocio del proyecto. |

> Las propiedades `feriaconecta.*` quedan definidas desde ahora; las consumen los
> issues de Spring Security + JWT, CORS y Mercado Pago a medida que se implementan.

### Reglas

- Nunca commitear secretos ni claves de API.
- Usar únicamente credenciales **de prueba** de Mercado Pago (nunca productivas).
- Si un issue agrega una variable nueva, actualizar `.env.example` **y esta tabla** en el mismo PR.
