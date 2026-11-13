package com.feriaconecta.backend.feria.dto;

import com.feriaconecta.backend.feria.model.EstadoFeria;
import jakarta.validation.constraints.NotNull;

/**
 * Datos necesarios para solicitar un cambio de estado de una feria.
 *
 * @param estado nuevo estado solicitado para la feria
 */
public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoFeria estado

) {
}
