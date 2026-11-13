package com.feriaconecta.backend.feria.model;

/**
 * Representa los estados posibles de una feria.
 * <p>
 * Las transiciones entre estados se validan en la lógica
 * de negocio del módulo de gestión de ferias.
 */
public enum EstadoFeria {

    //feria creada y pendiente de publicacion
    BORRADOR,
    //feria publicada y disponible para postulaciones
    PUBLICADA,
    //feria que se encuentra en desarrollo
    EN_CURSO,
    //feria finalizada que no admite nuevas modificaciones
    FINALIZADA,
    //feria cancelada que no admite modificaciones
    CANCELADA
}
