# Despliegue del backend en el VPS

Procedimiento para publicar el backend de FeriaConecta en el VPS de Oracle Cloud
(Docker + Portainer + Nginx Proxy Manager), según la arquitectura de la sección 4.1
del [README](../README.md#41-arquitectura-de-despliegue).

## URLs públicas

| Componente | URL |
|------------|-----|
| Backend (API) | https://api.feriaconecta.isierra93.site/api/v1 |
| Frontend | https://feria-conecta.vercel.app |

El frontend se despliega en Vercel con CI/CD desde `main`; su configuración
está en [`frontend/README.md`](../frontend/README.md#despliegue-en-vercel).

## Topología

```
Internet ──443──► Nginx Proxy Manager ──red-backend──► feriaconecta-api:8080 ──red-backend──► mysql-central:3306
```

- El backend corre como el stack `feriaconecta` en Portainer ([`deploy/portainer-stack.yml`](../deploy/portainer-stack.yml)).
- Usa el MySQL ya operativo en el VPS (`mysql-central`), con una base y un usuario propios.
- Ni el 8080 del backend ni el 3306 de MySQL se publican en el host: todo el tráfico
  entra por Nginx Proxy Manager con HTTPS.
- El VPS es ARM (`arm64`): la imagen se construye para esa plataforma.

## Primer despliegue

### 1. DNS

Crear un registro `A` para `api.<dominio>` que apunte a la IP pública del VPS.

### 2. Puertos 80 y 443

- **Oracle Cloud:** VCN → Security List → reglas de ingreso TCP `80` y `443` desde `0.0.0.0/0`.
- **Sistema operativo:** abrir los mismos puertos en el firewall (`iptables`/`ufw`).
- Verificar que `3306` y `8080` **no** estén abiertos a internet.

### 3. Backup de MySQL

Antes de tocar `mysql-central`, por SSH en el VPS con el usuario habitual (`ubuntu`/`opc`;
anteponer `sudo` si no pertenece al grupo `docker`):

```bash
docker exec mysql-central sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --all-databases --single-transaction --routines --triggers' > ~/backup-$(date +%F).sql
ls -lh ~/backup-*.sql
tail -1 ~/backup-*.sql   # debe decir "-- Dump completed on ..."
```

La password se toma de la variable `MYSQL_ROOT_PASSWORD` del propio contenedor (las comillas
simples evitan que la expanda el shell del host); `-p` sin valor no funciona porque
`docker exec` sin TTY no puede pedirla.

### 4. Base de datos y usuario

Conectarse como root (`docker exec -it mysql-central mysql -uroot -p`) y ejecutar:

```sql
CREATE DATABASE feriaconecta CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'feriaconecta'@'%' IDENTIFIED BY '<password-fuerte>';
GRANT SELECT, INSERT, UPDATE, DELETE ON feriaconecta.* TO 'feriaconecta'@'%';
```

El usuario de la aplicación no tiene permisos de DDL: el esquema lo administra root
a partir de `database/schema.sql`.

### 5. Aplicar el esquema

Copiar `database/schema.sql` al VPS (`scp` o `git clone`) y aplicarlo sobre la base:

```bash
docker exec -i mysql-central mysql -uroot -p feriaconecta < schema.sql
docker exec -it mysql-central mysql -uroot -p -e "SHOW TABLES FROM feriaconecta;"
```

### 6. Imagen del backend

Desde la raíz del repo, construir para `arm64` y publicar en Docker Hub:

```bash
docker buildx build --platform linux/arm64 \
  -t isierra93/feriaconecta-backend:0.1.0 \
  -t isierra93/feriaconecta-backend:latest \
  --push backend/
```

### 7. Stack en Portainer

1. Stacks → **Add stack** → nombre `feriaconecta` → Web editor.
2. Pegar el contenido de [`deploy/portainer-stack.yml`](../deploy/portainer-stack.yml).
3. En **Environment variables** → *Advanced mode*, cargar:

   ```env
   IMAGE_TAG=0.1.0
   DB_USERNAME=feriaconecta
   DB_PASSWORD=<password del usuario feriaconecta>
   JWT_SECRET=<openssl rand -base64 32>
   JWT_EXPIRATION_MINUTES=60
   MP_ACCESS_TOKEN=<access token de PRUEBA de Mercado Pago>
   MP_WEBHOOK_SECRET=<clave secreta del webhook>
   APP_BACKEND_URL=https://api.feriaconecta.isierra93.site/api/v1
   APP_FRONTEND_URL=https://feria-conecta.vercel.app
   CORS_ALLOWED_ORIGINS=https://feria-conecta.vercel.app
   PAGO_PLAZO_HORAS=72
   ```

   Todas las variables deben tener valor. Si todavía no existe la URL de Vercel o las
   credenciales de Mercado Pago, usar un valor provisorio y actualizarlo después.
   El detalle de cada variable está en [`backend/README.md`](../backend/README.md#variables).
4. **Deploy the stack**.

### 8. Nginx Proxy Manager en la red del backend

NPM tiene que estar conectado a `red-backend` para resolver `feriaconecta-api` por nombre.
Si no lo está:

```bash
docker network connect red-backend <contenedor-de-npm>
```

(o agregar `red-backend` como red externa en el stack de NPM, para que sobreviva a un redeploy).

### 9. Proxy Host

En NPM → Hosts → **Add Proxy Host**:

| Campo | Valor |
|-------|-------|
| Domain Names | `api.feriaconecta.isierra93.site` |
| Scheme | `http` |
| Forward Hostname / IP | `feriaconecta-api` |
| Forward Port | `8080` |
| Block Common Exploits | Sí |
| SSL | Let's Encrypt, **Force SSL**, HTTP/2 |

### 10. Verificación

```bash
curl https://api.<dominio>/api/v1/actuator/health
# {"status":"UP"}
```

- El health incluye el context-path `/api/v1`.
- `http://<ip-del-vps>:8080` **no** debe responder.
- En los logs del contenedor (Portainer → Containers → `feriaconecta-api` → Logs) no
  debe haber errores de conexión a MySQL ni de validación de Hibernate.

## Redeploy

1. Construir y publicar la imagen con un tag nuevo (paso 6, p. ej. `0.2.0`).
2. Si el cambio modifica el esquema, aplicar el SQL correspondiente como root **antes**
   del redeploy: con `ddl-auto=validate` el backend no arranca si el esquema no coincide.
3. Portainer → Stacks → `feriaconecta` → cambiar `IMAGE_TAG` en las variables →
   **Update the stack** con *Re-pull image* activado.
4. Verificar el health (paso 10).

**Rollback:** volver `IMAGE_TAG` al tag anterior y actualizar el stack.

> Mejora futura: construir y publicar la imagen desde GitHub Actions.
