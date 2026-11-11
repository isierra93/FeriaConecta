package com.feriaconecta.backend.publico.dto;

import com.feriaconecta.backend.common.model.Categoria;

/**
 * Representa una categoría disponible.
 * <p>
 * El código es la constante del enum (se serializa con su nombre, por
 * ejemplo {@code GASTRONOMIA}) y es el valor que el frontend debe enviar;
 * la etiqueta es su descripción legible para mostrar.
 *
 * @param codigo   constante del catálogo
 * @param etiqueta texto visible de la categoría
 */
public record CategoriaResponse(
        Categoria codigo,
        String etiqueta
) {
}
