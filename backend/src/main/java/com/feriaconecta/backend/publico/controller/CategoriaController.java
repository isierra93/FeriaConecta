package com.feriaconecta.backend.publico.controller;

import com.feriaconecta.backend.common.model.Categoria;
import com.feriaconecta.backend.config.OpenApiConfig;
import com.feriaconecta.backend.publico.dto.CategoriaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Controlador público del catálogo de categorías.
 * <p>
 * Expone los rubros disponibles para utilizar
 * los mismos códigos en ferias y postulaciones.
 */
@RestController
@RequestMapping("/public/categorias")
@Tag(name = OpenApiConfig.TAG_PUBLICO)
@SecurityRequirements()
public class CategoriaController {

    /**
     * Devuelve todas las categorías definidas en el enum,
     * incluyendo su código y nombre
     */
    @GetMapping
    @Operation(summary = "Listar categorías disponibles")
    @ApiResponse(responseCode = "200", description = "Listado de categorias")
    public List<CategoriaResponse> listarCategorias() {
        return Arrays.stream(Categoria.values())
                .map(categoria -> new CategoriaResponse(
                        categoria.name(), categoria.getNombre()
                ))
                .toList();
    }
}
