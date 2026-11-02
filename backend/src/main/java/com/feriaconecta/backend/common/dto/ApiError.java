package com.feriaconecta.backend.common.dto;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Cuerpo de error común de toda la API.
 *
 * <p>Es un contrato con el frontend: cambiar sus campos obliga a actualizar el
 * cliente HTTP. Lo usan el {@code GlobalExceptionHandler} y los manejadores de
 * Spring Security (401/403).
 *
 * @param timestamp momento del error (UTC, ISO-8601)
 * @param status    código HTTP
 * @param error     frase estándar del código HTTP (por ejemplo, "Not Found")
 * @param message   mensaje legible en español
 * @param path      URI de la petición (incluye {@code /api/v1})
 * @param details   errores por campo; solo presente en errores de validación
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details) {

    /**
     * Detalle de un error de validación sobre un campo.
     *
     * @param field   nombre del campo (o del objeto, para errores globales)
     * @param message mensaje de validación
     */
    public record FieldErrorDetail(String field, String message) {
    }

    public ApiError {
        details = details == null ? List.of() : List.copyOf(details);
    }

    /** Crea un error sin detalle por campo. */
    public static ApiError of(HttpStatus status, String message, String path) {
        return of(status, message, path, List.of());
    }

    /** Crea un error con detalle por campo (errores de validación). */
    public static ApiError of(HttpStatus status, String message, String path,
            List<FieldErrorDetail> details) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                message, path, details);
    }
}
