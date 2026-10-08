# Diseño y Módulos

**Fecha de entrega:** 27/09/2026

**Integrantes:** Victor Ivan Sierra · Facundo Miguel Archiria · Nahuel Alfredo Ayala


**Tutor:** Sebastián Bruselario

---

## 1. Esquema de base de datos

- Diagrama Entidad-Relación: [`database/diagrama-er.md`](../database/diagrama-er.md)
- Script DDL: [`database/schema.sql`](../database/schema.sql)
- Stack: MySQL 8, ejecutado en contenedor sobre el VPS Oracle (ver [README sección 4.1](../README.md)).

Entidades: **USUARIO · FERIA · UBICACION · POSTULACION · PAGO · CREDENCIAL · PUESTO**.

Las claves del modelo conceptual son:

- `USUARIO`: identificado por `id`; `email` es único.
- `FERIA`: identificado por `id`; se relaciona 1 a 1 con `UBICACION`.
- `UBICACION`: identificada por `feria_id`.
- `POSTULACION`: identificada por `id`; la combinación `(feria_id, emprendedor_id)` es única.
- `PAGO`: entidad débil de `POSTULACION`, identificada por `postulacion_id`; `external_reference` es única.
- `CREDENCIAL`: entidad débil de `POSTULACION`, identificada por `postulacion_id`; `token_uuid` es único.
- `PUESTO`: identificado por su clave natural `(feria_id, codigo)`.

Los valores de `estado` replican literalmente los estados definidos en
[README sección 4.5](../README.md).

## 2. Listado de módulos a desarrollar

Cada fila corresponde a un módulo funcional del sistema. La columna
"Descripción" resume qué resuelve el módulo; la columna "Doc." apunta al
alcance original en `README.md` sección 2; la columna "Entidades" indica las entidades
involucradas en el modelo conceptual.

| # | Módulo | Descripción | Doc. (sección) | Entidades |
|---|---|---|---|---|
| 1 | Autenticación y registro de usuarios | Registro, inicio de sesión y gestión de roles con JWT; un usuario puede tener rol organizador, emprendedor o ambos. | 2.1 | USUARIO |
| 2 | Gestión de ferias (CRUD + estados) | Creación, edición, publicación y finalización de ferias con datos de ubicación validados por GeoRef, categoría permitida, costo y cantidad de puestos. | 2.3, 2.11 | FERIA, UBICACION |
| 3 | Postulaciones de emprendedores | Envío y evaluación de solicitudes de participación; el organizador aprueba o rechaza y las aprobadas pasan a pendiente de pago. | 2.4 | POSTULACION |
| 4 | Pagos con Mercado Pago (Checkout Pro / Orders) | Generación de órdenes mediante Checkout Pro/Orders API, validación segura por webhook y confirmación automática de la participación. | 2.5, 2.6, 4.3 | PAGO, POSTULACION |
| 5 | Puestos y asignación | Creación de puestos dentro de una feria y asignación a participantes confirmados, evitando duplicados. | 2.9 | PUESTO, POSTULACION |
| 6 | Credenciales QR (generación y descarga) | Generación bajo demanda de un QR/SVG por participante confirmado, usando un token UUID sin datos personales. | 2.7, 4.4 | CREDENCIAL, POSTULACION |
| 7 | Acreditación por QR (escaneo) | Escaneo y validación del QR el día del evento para registrar asistencia y rechazar credenciales ya utilizadas o inválidas. | 2.8, 4.4 | CREDENCIAL, POSTULACION |
| 8 | Panel y reportes del organizador | Vista resumen con estadísticas del evento: postulaciones, pagos, acreditaciones y ocupación de los puestos. | 2.10 | FERIA, UBICACION, POSTULACION, PAGO, CREDENCIAL, PUESTO |
| 9 | Listado público de ferias | Consulta pública de las ferias publicadas, sin necesidad de autenticación. | 2.10 | FERIA, UBICACION |

**Total: 9 módulos.**

### Fuera de alcance de la v1 (ver [README sección 8](../README.md))

- Notificaciones automáticas por correo.
- Plano visual de distribución de puestos.
- Mapa interactivo.
- Mensajería interna o chat entre usuarios.

## 3. Resumen de decisiones de diseño relevantes

- **Categorías por feria (v1):** una sola categoría principal por feria
  (`feria.categoria_permitida`). Documentado en [README sección 2.3](../README.md).
- **Credencial QR:** el SVG contiene sólo el token UUID + URL;
  ningún dato personal. ([README sección 4.4](../README.md))
- **Pagos:** fuente de verdad = `GET /v1/orders/{id}` validado por webhook;
  jamás se confía en la URL de retorno del navegador. ([README sección 4.3](../README.md))
- **GeoRef:** integración exclusiva desde el backend; si falla, se permite
  guardar con aviso (degradar, no fallar). ([README sección 4.2](../README.md))
- **Estados del dominio:** definidos como `CHECK` en `schema.sql` y replicados
  en [README.md sección 4.5](../README.md) y [database/diagrama-er.md](../database/diagrama-er.md).
- **Ubicación separada:** los datos geográficos de una feria se modelan como
  entidad `UBICACION` separada, que persiste strings, coordenadas e IDs
  devueltos por GeoRef.
- **Roles multivaluados:** un usuario puede tener rol `ORGANIZADOR`,
  `EMPRENDEDOR` o ambos; en el modelo conceptual es un atributo multivaluado
  de `USUARIO` y en el DDL se implementa como tabla `usuario_rol`. No es una
  entidad propia.