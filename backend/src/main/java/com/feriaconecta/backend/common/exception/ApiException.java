package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la API. Cada subclase define el código HTTP con el que
 * {@link GlobalExceptionHandler} responde. El mensaje se envía al cliente, por
 * lo que debe estar en español y no contener datos sensibles.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
