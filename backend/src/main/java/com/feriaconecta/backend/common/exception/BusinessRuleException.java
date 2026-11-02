package com.feriaconecta.backend.common.exception;

import org.springframework.http.HttpStatus;

/** Se violó una regla de negocio, por ejemplo una transición de estado inválida (422). */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
