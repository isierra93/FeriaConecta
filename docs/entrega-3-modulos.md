# Entrega 3 — Diseño y Módulos

**Trabajo Final Integrador — Tecnicatura en Programación (UTN)**
**Fecha de entrega:** 27/09/2026
**Integrantes:** Victor Ivan Sierra · Facundo Miguel Archiria · Nahuel Alfredo Ayala
**Tutor:** Sebastián Bruselario

---

## 1. Esquema de base de datos

- Diagrama Entidad-Relación: [`database/diagrama-er.md`](../database/diagrama-er.md)
- Script DDL: [`database/schema.sql`](../database/schema.sql)
- Stack: MySQL 8, ejecutado en contenedor sobre el VPS Oracle (ver [README §4.1](../README.md)).

Entidades: **USUARIO · FERIA · POSTULACION · PAGO · CREDENCIAL · PUESTO**.

Las claves únicas (`uq_postulacion_feria_emprendedor`, `uq_puesto_feria_codigo`,
`UNIQUE` en `email`, `external_reference`, `token_uuid`, `postulacion_id` en
`pago` y `credencial`) están materializadas en el DDL como `CONSTRAINT`s.
Los valores de `estado` se validan por `CHECK` y replican literalmente los
estados definidos en [README §4.5](../README.md).

## 2. Listado de módulos a desarrollar

Cada fila corresponde a un módulo funcional del sistema. La columna
"Documentación" apunta al alcance original en `README.md` §2; la columna
"Entidades" indica las tablas involucradas.

| # | Módulo | Doc. (README §) | Entidades | Estado |
|---|---|---|---|---|
| 1 | Autenticación y registro de usuarios | §2.1 | USUARIO | Pendiente |
| 2 | Gestión de ferias (CRUD + estados) | §2.3, §2.11 | FERIA | Pendiente |
| 3 | Postulaciones de emprendedores | §2.4 | POSTULACION | Pendiente |
| 4 | Pagos con Mercado Pago (Checkout Pro / Orders) | §2.5, §2.6, §4.3 | PAGO, POSTULACION | Pendiente |
| 5 | Puestos y asignación | §2.9 | PUESTO, POSTULACION | Pendiente |
| 6 | Credenciales QR (generación y descarga) | §2.7, §4.4 | CREDENCIAL, POSTULACION | Pendiente |
| 7 | Acreditación por QR (escaneo) | §2.8, §4.4 | CREDENCIAL, PUESTO | Pendiente |
| 8 | Panel y reportes del organizador | §2.10 | FERIA, POSTULACION, PAGO, CREDENCIAL | Pendiente |
| 9 | Listado público de ferias | §2.10 | FERIA | Pendiente |

**Total: 9 módulos.**

### Fuera de alcance de la v1 (ver README §8)

- Notificaciones automáticas por correo.
- Plano visual de distribución de puestos.
- Mapa interactivo.

## 3. Resumen de decisiones de diseño relevantes

- **Categorías por feria (v1):** una sola categoría principal por feria
  (`feria.categoria_permitida`). Documentado en [README §2.3](../README.md).
- **Credencial QR:** el SVG contiene sólo el token UUID + URL;
  ningún dato personal. (README §4.4)
- **Pagos:** fuente de verdad = `GET /v1/orders/{id}` validado por webhook;
  jamás se confía en la URL de retorno del navegador. (README §4.3)
- **GeoRef:** integración exclusiva desde el backend; si falla, se permite
  guardar con aviso (degradar, no fallar). (README §4.2)
- **Estados del dominio:** definidos como `CHECK` en `schema.sql` y replicados
  en `README.md` §4.5 y `database/diagrama-er.md`.

## 4. Pendiente de aprobación

Este documento queda a la espera del visto bueno del tutor y, posteriormente,
del comité de trabajo final, condición necesaria para acceder a la condición
de **Regular** (según cronograma oficial de la cátedra).
