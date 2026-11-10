package com.feriaconecta.backend.publico.dto;

/**
 * Representa una categoría disponible.
 *
 * El código es el identificador estable del enum,
 * mientras que el nombre es su descripción legible.
 */
public record CategoriaResponse(
        String codigo,
        String nombre
) {
}
