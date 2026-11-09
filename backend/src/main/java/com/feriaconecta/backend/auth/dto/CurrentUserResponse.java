package com.feriaconecta.backend.auth.dto;

import com.feriaconecta.backend.auth.model.Rol;

import java.util.Set;

/**
 * Datos del usuario que ha iniciado sesión.
 * <p>
 * Se utiliza para responder la consulta /auth/me.
 * No expone la contraseña hasheada ni información
 * interna de persistencia.
 *
 * @param nombre nombre del usuario autenticado
 * @param email  correo electrónico registrado
 * @param roles  roles asignados al usuario
 */
public record CurrentUserResponse(
        String nombre,
        String email,
        Set<Rol> roles
) {
}
