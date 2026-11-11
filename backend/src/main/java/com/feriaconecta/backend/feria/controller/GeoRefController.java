package com.feriaconecta.backend.feria.controller;

import com.feriaconecta.backend.config.OpenApiConfig;
import com.feriaconecta.backend.feria.dto.DireccionNormalizadaResponse;
import com.feriaconecta.backend.feria.service.GeoRefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar sugerencias de direcciones mediante GeoRef.
 * <p>
 * Expone un endpoint exclusivo para organizadores autenticados
 * y delega la normalización de direcciones en GeoRefService.
 * <p>
 * La comunicación con GeoRef se realiza únicamente desde el backend.
 */
@RestController
@RequestMapping("/ferias/direcciones")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_FERIA)
public class GeoRefController {

    private final GeoRefService geoRefService;

    /**
     * Consulta sugerencias de direcciones normalizadas.
     * <p>
     * Recibe los datos ingresados por el organizador y devuelve
     * las coincidencias encontradas mediante GeoRef.
     * <p>
     * Si no es posible normalizar la dirección, devuelve los datos
     * originales con un aviso, sin generar un error HTTP.
     *
     * @param provincia provincia ingresada por el organizador
     * @param municipio municipio ingresado por el organizador
     * @param localidad localidad ingresada por el organizador
     * @param calle     calle ingresada por el organizador
     * @param altura    altura ingresada por el organizador
     * @return lista de sugerencias o dirección no normalizada
     */
    @GetMapping("/sugerencias")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Consultar sugerencias de direcciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta procesada, con o sin normalización"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR")
    })
    public List<DireccionNormalizadaResponse> buscarSugerencias(
            @RequestParam @NotBlank String provincia,
            @RequestParam @NotBlank String municipio,
            @RequestParam @NotBlank String localidad,
            @RequestParam @NotBlank String calle,
            @RequestParam @NotBlank String altura) {

        return geoRefService.buscarSugerencias(
                provincia, municipio, localidad, calle, altura);
    }
}
