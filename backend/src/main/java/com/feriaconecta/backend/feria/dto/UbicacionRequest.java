package com.feriaconecta.backend.feria.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Datos de ubicación recibidos al crear o editar una feria.
 * <p>
 * Las coordenadas son opcionales y pueden provenir de una
 * dirección seleccionada mediante las sugerencias de GeoRef.
 * Si no se dispone de ellas, se permite ingresar la dirección manualmente.
 *
 * @param provincia provincia donde se realiza la feria
 * @param municipio municipio de la ubicación
 * @param localidad localidad de la ubicación
 * @param calle     nombre de la calle
 * @param altura    numeración de la dirección
 * @param latitud   coordenada de latitud, opcional
 * @param longitud  coordenada de longitud, opcional
 */
public record UbicacionRequest(

        @NotBlank(message = "La provincia es obligatoria")
        @Size(max = 100, message = "La provincia no puede superar los 100 caracteres")
        String provincia,

        @NotBlank(message = "El municipio es obligatorio")
        @Size(max = 100, message = "El municipio no puede superar los 100 caracteres")
        String municipio,

        @NotBlank(message = "La localidad es obligatoria")
        @Size(max = 100, message = "La localidad no puede superar los 100 caracteres")
        String localidad,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 200, message = "La calle no puede superar los 200 caracteres")
        String calle,

        @NotBlank(message = "La altura es obligatoria")
        @Size(max = 20, message = "La altura no puede superar los 20 caracteres")
        String altura,

        @DecimalMin(value = "-90.0", message = "La latitud no puede ser menor a -90")
        @DecimalMax(value = "90.0", message = "La latitud no puede ser mayor a 90")
        @Digits(integer = 2, fraction = 8,
                message = "La latitud debe tener hasta 2 dígitos enteros y 8 decimales")
        BigDecimal latitud,

        @DecimalMin(value = "-180.0", message = "La longitud no puede ser menor a -180")
        @DecimalMax(value = "180.0", message = "La longitud no puede ser mayor a 180")
        @Digits(integer = 3, fraction = 8,
                message = "La longitud debe tener hasta 3 dígitos enteros y 8 decimales")
        BigDecimal longitud
) {
}
