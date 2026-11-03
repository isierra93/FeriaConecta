package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/** El usuario está autenticado pero no puede acceder al recurso (403). */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
