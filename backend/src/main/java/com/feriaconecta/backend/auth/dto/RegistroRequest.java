
package com.feriaconecta.backend.auth.dto;

import com.feriaconecta.backend.auth.model.Rol;
import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * Datos recibidos para registrar un usuario en FeriaConecta.
 * <p>
 * Las validaciones se ejecutan mediante @Valid en el controlador.
 *
 * @param email      correo electrónico del usuario
 * @param nombre     nombre del usuario
 * @param contrasena contraseña sin hashear recibida en el registro
 * @param roles      uno o ambos roles disponibles
 */
public record RegistroRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena,

        // Se exige al menos un rol y no se aceptan elementos nulos.
        @NotEmpty(message = "Debe seleccionar al menos un rol")
        Set<@NotNull Rol> roles

) {
}
