
package com.feriaconecta.backend.auth.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Construye un nuevo usuario a partir de los datos de registro.
 *
 * El identificador y las fechas no se reciben porque
 * son administrados por la base de datos.
 *
 * @param email correo electrónico del usuario
 * @param nombre nombre del usuario
 * @param contrasenaHash contraseña previamente hasheada
 * @param roles roles asignados al usuario
 */
@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    // Los roles se almacenan en la tabla usuario_rol.
    // LAZY evita cargarlos hasta que sean necesarios.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Set<Rol> roles = new HashSet<>();

    /**
     * Construye un nuevo usuario con los datos del registro.
     */
    @Builder
    public Usuario(
            String email,
            String nombre,
            String contrasenaHash,
            Set<Rol> roles
    ) {
        this.email = email;
        this.nombre = nombre;
        this.contrasenaHash = contrasenaHash;
        this.roles = new HashSet<>(roles);
    }
}
