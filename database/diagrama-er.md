# Diagrama Entidad-Relación Conceptual (UML)

Modelo conceptual del dominio de FeriaConecta, independiente de cualquier tecnología de base de datos.

```mermaid
classDiagram
    class Usuario {
        +id
        +nombre
        +email
        +contrasena
        +roles: Rol[1..*]
    }

    class Rol {
        <<enumeration>>
        ORGANIZADOR
        EMPRENDEDOR
    }

    class Feria {
        +id
        +nombre
        +fecha
        +categoriaPermitida
        +cupos
        +costoParticipacion
        +estado
    }

    class Ubicacion {
        +provincia
        +provinciaId
        +municipio
        +municipioId
        +localidad
        +localidadId
        +calle
        +calleId
        +latitud
        +longitud
    }

    class Postulacion {
        +id
        +rubro
        +descripcionEmprendimiento
        +estado
        +fechaPostulacion
    }

    class Pago {
        +monto
        +estado
        +fecha
        +referenciaExterna
        +idOrdenExterno
    }

    class Credencial {
        +token
        +estado
        +fechaGeneracion
        +fechaUso
    }

    class Puesto {
        +codigo
        +estado
    }

    Usuario "1" --> "0..*" Feria : organiza
    Usuario "1" --> "0..*" Postulacion : presenta
    Feria "1" *-- "1" Ubicacion : se realiza en
    Feria "1" --> "0..*" Postulacion : recibe
    Postulacion "1" *-- "0..1" Pago : genera
    Postulacion "1" *-- "0..1" Credencial : obtiene
    Feria "1" *-- "1..*" Puesto : contiene
    Puesto "0..1" --> "0..1" Postulacion : asignado a
```

## Descripción de relaciones

| Relación | Cardinalidad | Significado |
|---|---|---|
| Usuario organiza Feria | 1 a 0..* | Un organizador puede crear cero o muchas ferias. |
| Usuario presenta Postulacion | 1 a 0..* | Un emprendedor puede presentar cero o muchas postulaciones. |
| Feria se realiza en Ubicacion | 1 a 1 | Cada feria tiene exactamente una ubicación. La ubicación no existe independientemente de la feria. |
| Feria recibe Postulacion | 1 a 0..* | Una feria recibe cero o muchas postulaciones. |
| Postulacion genera Pago | 1 a 0..1 | Una postulación aprobada genera exactamente un pago. |
| Postulacion obtiene Credencial | 1 a 0..1 | Una postulación confirmada obtiene exactamente una credencial. |
| Feria contiene Puesto | 1 a 1..* | Una feria tiene uno o más puestos. |
| Puesto asignado a Postulacion | 0..1 a 0..1 | Un puesto puede asignarse a una postulación confirmada como máximo. |

## Decisiones de diseño

- **Rol**: se modela como enumeración multivaluada porque un mismo usuario puede ser organizador y emprendedor.
- **Pago y Credencial**: son entidades débiles de Postulacion. No tienen existencia independiente.
- **Puesto**: se identifica por su código dentro de una feria (clave natural). No requiere identificador propio.
- **Ubicacion**: es un value object asociado a Feria. Agrupa los datos devueltos por GeoRef: nombres, IDs del servicio y coordenadas.
