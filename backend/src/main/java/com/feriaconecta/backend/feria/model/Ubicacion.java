package com.feriaconecta.backend.feria.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "ubicacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ubicacion {
    @Id
    @Column(name = "feria_id")
    private Integer feriaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "feria_id", nullable = false)
    private Feria feria;

    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "localidad", nullable = false, length = 100)
    private String localidad;

    @Column(name = "calle", nullable = false, length = 200)
    private String calle;

    @Column(name = "altura", nullable = false, length = 20)
    private String altura;

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    /**
     * Construye una ubicación con los datos de dirección.
     * <p>
     * Las coordenadas pueden ser nulas si GeoRef no logra
     * normalizar la dirección ingresada.
     *
     * @param provincia provincia de la ubicación
     * @param municipio municipio de la ubicación
     * @param localidad localidad de la ubicación
     * @param calle     nombre de la calle
     * @param altura    numeración de la dirección
     * @param latitud   coordenada de latitud, opcional
     * @param longitud  coordenada de longitud, opcional
     */
    @Builder
    public Ubicacion(
            String provincia,
            String municipio,
            String localidad,
            String calle,
            String altura,
            BigDecimal latitud,
            BigDecimal longitud
    ) {
        this.provincia = provincia;
        this.municipio = municipio;
        this.localidad = localidad;
        this.calle = calle;
        this.altura = altura;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /**
     * Asocia esta ubicación con su feria propietaria.
     * <p>
     * El identificador se obtiene automáticamente de
     * la feria mediante la anotación MapsId.
     *
     * @param feria feria a la que pertenece la ubicación
     */
    void asignarFeria(Feria feria) {
        this.feria = Objects.requireNonNull(feria);
    }

    /**
     * Actualiza los datos de ubicación de una feria.
     * <p>
     * La relación con la feria y su identificador
     * se mantienen sin modificaciones.
     *
     * @param provincia provincia de la ubicación
     * @param municipio municipio de la ubicación
     * @param localidad localidad de la ubicación
     * @param calle     nombre de la calle
     * @param altura    numeración de la dirección
     * @param latitud   coordenada de latitud, opcional
     * @param longitud  coordenada de longitud, opcional
     */
    public void actualizarDatos(
            String provincia,
            String municipio,
            String localidad,
            String calle,
            String altura,
            BigDecimal latitud,
            BigDecimal longitud
    ) {
        this.provincia = provincia;
        this.municipio = municipio;
        this.localidad = localidad;
        this.calle = calle;
        this.altura = altura;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}
