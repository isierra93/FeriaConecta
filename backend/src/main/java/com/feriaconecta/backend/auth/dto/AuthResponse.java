package com.feriaconecta.backend.auth.dto;

/**
 * Respuesta de autenticación utilizada por registro y login.
 *
 * Contiene el token JWT generado para el usuario autenticado
 * y el tipo de autenticación que debe utilizar el frontend.
 *
 * @param token token JWT firmado por el backend
 * @param tipo esquema de autenticación Bearer
 */
public record AuthResponse(
        String token,
        String tipo
) {
}
