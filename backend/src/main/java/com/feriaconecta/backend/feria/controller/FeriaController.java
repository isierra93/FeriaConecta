package com.feriaconecta.backend.feria.controller;

import com.feriaconecta.backend.config.OpenApiConfig;
import com.feriaconecta.backend.feria.dto.CambiarEstadoRequest;
import com.feriaconecta.backend.feria.dto.FeriaRequest;
import com.feriaconecta.backend.feria.dto.FeriaResponse;
import com.feriaconecta.backend.feria.service.FeriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de ferias.
 *
 * <p>Expone los endpoints del módulo feria y delega
 * las operaciones de negocio en FeriaService.
 *
 * <p>Los errores son procesados por el manejador global
 * y devueltos mediante el formato común ApiError.
 */
@RestController
@RequestMapping("/ferias")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_FERIA)
public class FeriaController {

    private final FeriaService feriaService;

    /**
     * Crea una feria en estado BORRADOR.
     *
     * <p>La feria queda asociada al organizador autenticado
     * y se registra junto con su ubicación.
     *
     * @param request datos de la feria y su ubicación
     * @return feria creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Crear una feria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feria creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR"),
            @ApiResponse(responseCode = "409", description = "Feria duplicada"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio incumplida")
    })
    public ResponseEntity<FeriaResponse> crear(
            @Valid @RequestBody FeriaRequest request) {

        FeriaResponse respuesta = feriaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Listar mis ferias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR")
    })
    public ResponseEntity<List<FeriaResponse>> listarFerias() {
        return ResponseEntity.ok(feriaService.listarMisFerias());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Obtener detalle de una feria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feria obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR"),
            @ApiResponse(responseCode = "404", description = "Feria no encontrada")
    })
    public ResponseEntity<FeriaResponse> obtenerDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(feriaService.obtenerDetalle(id));
    }

    /**
     * Actualiza los datos de una feria del organizador autenticado.
     *
     * <p>Las modificaciones permitidas dependen del estado
     * y de la fecha de realización de la feria.
     *
     * @param id identificador de la feria
     * @param request datos actualizados de la feria y su ubicación
     * @return feria actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Actualizar una feria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feria actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR"),
            @ApiResponse(responseCode = "404", description = "Feria no encontrada"),
            @ApiResponse(responseCode = "409", description = "Feria duplicada"),
            @ApiResponse(responseCode = "422", description = "Edición no permitida")
    })
    public ResponseEntity<FeriaResponse> editar(
            @PathVariable Integer id,
            @Valid @RequestBody FeriaRequest request) {

        return ResponseEntity.ok(feriaService.editar(id, request));
    }

    /**
     * Cambia el estado de una feria del organizador autenticado.
     *
     * <p>La transición debe respetar los estados permitidos
     * y las reglas de negocio definidas en FeriaService.
     *
     * @param id identificador de la feria
     * @param request nuevo estado solicitado
     * @return feria con el estado actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Cambiar estado de una feria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o no informado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ORGANIZADOR"),
            @ApiResponse(responseCode = "404", description = "Feria no encontrada"),
            @ApiResponse(responseCode = "422", description = "Transición de estado no permitida")
    })
    public ResponseEntity<FeriaResponse> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return ResponseEntity.ok(feriaService.cambiarEstado(id, request));
    }
}
