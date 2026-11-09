package com.feriaconecta.backend.auth.controller;

import com.feriaconecta.backend.auth.dto.AuthResponse;
import com.feriaconecta.backend.auth.dto.CurrentUserResponse;
import com.feriaconecta.backend.auth.dto.LoginRequest;
import com.feriaconecta.backend.auth.dto.RegistroRequest;
import com.feriaconecta.backend.auth.service.AuthService;
import com.feriaconecta.backend.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el registro y la autenticación.
 * <p>
 * Expone los endpoints del módulo auth y delega las operaciones
 * de negocio en AuthService.
 * <p>
 * Los errores son procesados por el manejador global
 * y devueltos mediante el formato común ApiError.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_AUTH)
public class AuthController {

    private final AuthService authService;

    /**
     * Registra una cuenta y devuelve un JWT para iniciar sesión.
     */
    @PostMapping("/registro")
    @SecurityRequirements()
    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<AuthResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        AuthResponse respuesta = authService.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Valida las credenciales y devuelve un JWT.
     */
    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    /**
     * Devuelve los datos de la cuenta identificada por el JWT.
     * <p>
     * No recibe un ID desde el cliente para evitar
     * consultas arbitrarias de otros usuarios.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Consultar el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public CurrentUserResponse obtenerUsuarioActual() {

        return authService.obtenerUsuarioActual();
    }
}
