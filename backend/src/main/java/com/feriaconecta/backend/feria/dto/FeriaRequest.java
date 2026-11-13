package com.feriaconecta.backend.feria.dto;

import com.feriaconecta.backend.common.model.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos necesarios para crear y editar una feria.
 * <p>
 * La feria se crea en estado BORRADOR y el organizador
 * se obtiene del usuario autenticado.
 *
 * @param nombre             nombre de la feria
 * @param fecha              fecha de realización
 * @param categoriaPermitida categoría principal permitida
 * @param costoParticipacion costo para participar en la feria
 * @param ubicacion          dirección donde se realizará la feria
 */
public record FeriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
        String nombre,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede ser anterior a la actual")
        LocalDate fecha,

        @NotNull(message = "La categoría es obligatoria")
        Categoria categoriaPermitida,

        @NotNull(message = "El costo de participación es obligatorio")
        @DecimalMin(value = "0.00", message = "El costo de participación no puede ser negativo")
        @Digits(integer = 8, fraction = 2,
                message = "El costo debe tener hasta 8 dígitos enteros y 2 decimales")
        BigDecimal costoParticipacion,

        @NotNull(message = "La ubicación es obligatoria")
        @Valid
        UbicacionRequest ubicacion

) {
}
