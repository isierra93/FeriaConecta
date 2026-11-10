package com.feriaconecta.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos necesarios para iniciar sesión en FeriaConecta.
 *
 * @param email      correo electrónico registrado
 * @param contrasena contraseña ingresada por el usuario
 */
public record LoginRequest(

        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena

) {
}
