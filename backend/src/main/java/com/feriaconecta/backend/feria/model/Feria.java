package com.feriaconecta.backend.feria.model;

import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.common.model.Categoria;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una feria organizada por un usuario.
 * <p>
 * Cada feria tiene una categoría principal, una ubicación
 * y un estado que determina su disponibilidad.
 * <p>
 * El identificador y las fechas de auditoría son
 * administrados por la base de datos.
 */
@Entity
@Table(
        name = "feria",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_feria_organizador_nombre_fecha",
                columnNames = {"organizador_id", "nombre", "fecha"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_permitida", nullable = false, length = 100)
    private Categoria categoriaPermitida;

    @Column(
            name = "costo_participacion",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal costoParticipacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoFeria estado;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    @OneToOne(
            mappedBy = "feria",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            optional = false
    )
    private Ubicacion ubicacion;

    /**
     * Construye una feria nueva en estado BORRADOR.
     * <p>
     * El organizador debe provenir del usuario autenticado
     * y no de los datos enviados por el cliente.
     *
     * @param organizador        usuario propietario de la feria
     * @param nombre             nombre de la feria
     * @param fecha              fecha de realización
     * @param categoriaPermitida categoría principal permitida
     * @param costoParticipacion costo para participar
     */
    @Builder
    public Feria(
            Usuario organizador,
            String nombre,
            LocalDate fecha,
            Categoria categoriaPermitida,
            BigDecimal costoParticipacion
    ) {
        this.organizador = organizador;
        this.nombre = nombre;
        this.fecha = fecha;
        this.categoriaPermitida = categoriaPermitida;
        this.costoParticipacion = costoParticipacion;
        this.estado = EstadoFeria.BORRADOR;
    }

    /**
     * Asocia la ubicación obligatoria de la feria.
     * <p>
     * Mantiene sincronizados ambos lados de la relación
     * para permitir su persistencia mediante JPA.
     *
     * @param ubicacion ubicación que pertenece a la feria
     */
    public void asignarUbicacion(Ubicacion ubicacion) {
        this.ubicacion = Objects.requireNonNull(ubicacion);
        ubicacion.asignarFeria(this);
    }

    /**
     * Actualiza los datos de una feria existente.
     * <p>
     * Las restricciones de edición según el estado, la fecha
     * y las postulaciones se validan previamente en FeriaService.
     *
     * @param nombre             nuevo nombre de la feria
     * @param fecha              nueva fecha de realización
     * @param categoriaPermitida categoría principal permitida
     * @param costoParticipacion nuevo costo de participación
     */
    public void actualizarDatos(String nombre, LocalDate fecha, Categoria categoriaPermitida, BigDecimal costoParticipacion) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.categoriaPermitida = categoriaPermitida;
        this.costoParticipacion = costoParticipacion;
    }

    /**
     * Modifica el estado de una feria.
     * <p>
     * Las transiciones permitidas y sus condiciones
     * se validan previamente en FeriaService.
     *
     * @param nuevoEstado estado al que pasará la feria
     */
    public void cambiarEstado(EstadoFeria nuevoEstado) {
        this.estado = nuevoEstado;
    }
}
