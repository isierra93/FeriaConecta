package com.feriaconecta.backend.feria.dto;

import com.feriaconecta.backend.common.model.Categoria;
import com.feriaconecta.backend.feria.model.EstadoFeria;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Respuesta con los datos de una feria.
 * <p>
 * Incluye la ubicación registrada y un aviso opcional
 * cuando no fue posible normalizar la dirección.
 *
 * @param id                 identificador de la feria
 * @param nombre             nombre de la feria
 * @param fecha              fecha de realización
 * @param categoriaPermitida categoría principal permitida
 * @param costoParticipacion costo de participación
 * @param estado             estado actual de la feria
 * @param ubicacion          ubicación registrada
 * @param aviso              mensaje informativo opcional
 */
public record FeriaResponse(
        Integer id,
        String nombre,
        LocalDate fecha,
        Categoria categoriaPermitida,
        BigDecimal costoParticipacion,
        EstadoFeria estado,
        UbicacionDetalle ubicacion,
        String aviso
) {

    /**
     * Datos de ubicación que se devuelven al cliente.
     * <p>
     * Las coordenadas pueden ser nulas cuando GeoRef
     * no logró obtenerlas.
     *
     * @param provincia provincia de la ubicación
     * @param municipio municipio de la ubicación
     * @param localidad localidad de la ubicación
     * @param calle     nombre de la calle
     * @param altura    numeración de la dirección
     * @param latitud   coordenada de latitud
     * @param longitud  coordenada de longitud
     */
    public record UbicacionDetalle(
            String provincia,
            String municipio,
            String localidad,
            String calle,
            String altura,
            BigDecimal latitud,
            BigDecimal longitud
    ) {
    }
}
