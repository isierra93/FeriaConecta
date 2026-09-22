# Diagrama Entidad-Relación — sincronizado con `schema.sql` (Etapa 3)

Basado en las entidades y estados del `database/schema.sql`, sincronizado con la sección 4.5 del `README.md`.

```mermaid
erDiagram
    USUARIO ||--o{ FERIA : "organiza"
    USUARIO ||--o{ POSTULACION : "presenta (como emprendedor)"
    FERIA ||--o{ POSTULACION : "recibe"
    FERIA ||--o{ PUESTO : "define"
    POSTULACION ||--o| PAGO : "genera"
    POSTULACION ||--o| CREDENCIAL : "obtiene al confirmarse"
    PUESTO ||--o| POSTULACION : "se asigna a"

    USUARIO {
        bigint id PK
        string nombre
        string email
        string password_hash
        string rol "ORGANIZADOR, EMPRENDEDOR"
        datetime creado_en
    }

    FERIA {
        bigint id PK
        bigint organizador_id FK
        string nombre
        date fecha
        string provincia
        string municipio
        string localidad
        string calle
        string categoria_permitida
        int cupos
        decimal costo_participacion
        string estado "BORRADOR, PUBLICADA, FINALIZADA"
        datetime creado_en
    }

    POSTULACION {
        bigint id PK
        bigint feria_id FK
        bigint emprendedor_id FK
        string rubro
        string descripcion_emprendimiento
        string estado "PENDIENTE, APROBADA_PENDIENTE_PAGO, CONFIRMADA, RECHAZADA, VENCIDA"
        datetime creado_en
    }

    PAGO {
        bigint id PK
        bigint postulacion_id FK
        string mp_order_id
        string external_reference
        decimal monto
        string estado "PENDIENTE, APROBADO, RECHAZADO, CANCELADO, REEMBOLSADO"
        datetime creado_en
        datetime actualizado_en
    }

    CREDENCIAL {
        bigint id PK
        bigint postulacion_id FK
        string token_uuid
        string estado "ACTIVA, UTILIZADA"
        datetime creado_en
        datetime utilizada_en
    }

    PUESTO {
        bigint id PK
        bigint feria_id FK
        bigint postulacion_id FK "nullable hasta que se asigna"
        string codigo
        string estado "LIBRE, ASIGNADO"
        datetime creado_en
    }
```

## Notas

- `PAGO` y `CREDENCIAL` son 1 a 1 (o 1 a 0) con `POSTULACION`, siguiendo el flujo descripto en el README: una postulación confirmada tiene un pago aprobado y genera exactamente una credencial.
- `PUESTO` pertenece a una `FERIA` y se vincula opcionalmente a una `POSTULACION` una vez asignado (sección 3, paso 8 del README).
- Los valores de `estado` reflejan exactamente los definidos en la sección 4.5 del README, para mantener consistencia entre documentación y modelo de datos.
- Este diagrama está sincronizado con `database/schema.sql` al cierre de la Etapa 3. Las claves únicas (`uq_postulacion_feria_emprendedor`, `uq_puesto_feria_codigo`, `UNIQUE` en `email`, `external_reference`, `token_uuid` y `postulacion_id` de pago/credencial) están modeladas como restricciones en el propio DDL.
- Decisión de alcance (v1): cada feria admite una sola categoría principal (`categoria_permitida`). El rubro del emprendedor se coteja contra esa categoría. Si en una versión futura se requieren múltiples categorías por feria, se migrará a una tabla `feria_categoria`.
