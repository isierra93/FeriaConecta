package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/** La operación entra en conflicto con el estado actual (409), por ejemplo un duplicado. */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
