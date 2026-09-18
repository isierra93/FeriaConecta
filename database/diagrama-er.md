# Diagrama Entidad-Relación (borrador inicial)

Basado en las entidades y estados mencionados en el README (secciones 3, 4.5). A revisar y ajustar por el equipo antes de generar el script DDL definitivo.

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
        datetime actualizado_en
    }

    CREDENCIAL {
        bigint id PK
        bigint postulacion_id FK
        string token_uuid
        string estado "ACTIVA, UTILIZADA"
        datetime utilizada_en
    }

    PUESTO {
        bigint id PK
        bigint feria_id FK
        bigint postulacion_id FK "nullable hasta que se asigna"
        string codigo
        string estado "LIBRE, ASIGNADO"
    }
```

## Notas

- `PAGO` y `CREDENCIAL` son 1 a 1 (o 1 a 0) con `POSTULACION`, siguiendo el flujo descripto en el README: una postulación confirmada tiene un pago aprobado y genera exactamente una credencial.
- `PUESTO` pertenece a una `FERIA` y se vincula opcionalmente a una `POSTULACION` una vez asignado (sección 3, paso 8 del README).
- Los valores de `estado` reflejan exactamente los definidos en la sección 4.5 del README, para mantener consistencia entre documentación y modelo de datos.
- Este diagrama es un punto de partida: falta validar tipos de dato definitivos, índices y claves únicas (por ejemplo, `email` en `USUARIO`, `token_uuid` en `CREDENCIAL`) antes de convertirlo en el script DDL final.
