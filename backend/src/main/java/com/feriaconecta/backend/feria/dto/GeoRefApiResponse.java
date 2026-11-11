package com.feriaconecta.backend.feria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representa la respuesta recibida desde la API GeoRef.
 *
 * Permite deserializar las direcciones consultadas utilizando
 * únicamente los campos necesarios para FeriaConecta.
 *
 * Los campos adicionales de GeoRef se ignoran para evitar
 * depender de información que no utiliza la aplicación.
 *
 * @param cantidad cantidad de resultados informada por GeoRef
 * @param direcciones lista de direcciones encontradas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoRefApiResponse(
        Integer cantidad,
        List<Direccion> direcciones
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Direccion(
            Nombre provincia,
            @JsonProperty("localidad_censal") Nombre localidadCensal,
            Nombre calle,
            Altura altura,
            Ubicacion ubicacion
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nombre(
            String nombre
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Altura(
            Integer valor
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ubicacion(
            BigDecimal lat,
            BigDecimal lon
    ) {
    }
}
