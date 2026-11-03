package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/** El recurso solicitado no existe (404). */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Ejemplo: {@code new ResourceNotFoundException("la feria", 12)} →
     * "No se encontró la feria con id 12".
     */
    public ResourceNotFoundException(String resource, Object id) {
        this("No se encontró " + resource + " con id " + id);
    }
}
