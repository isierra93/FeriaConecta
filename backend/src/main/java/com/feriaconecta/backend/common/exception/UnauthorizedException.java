package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/** El usuario no está autenticado o sus credenciales son inválidas (401). */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
