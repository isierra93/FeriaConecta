package com.feriaconecta.backend.feria.dto;

import java.math.BigDecimal;

/**
 * Resultado de la normalización de una dirección mediante GeoRef.
 *
 * Si la dirección no puede normalizarse, conserva los datos
 * originales y devuelve las coordenadas en null.
 *
 * No expone identificadores internos de GeoRef.
 */
public record DireccionNormalizadaResponse(
        boolean normalizada,
        String provincia,
        String municipio,
        String localidad,
        String calle,
        String altura,
        BigDecimal latitud,
        BigDecimal longitud,
        String aviso
) {
}
