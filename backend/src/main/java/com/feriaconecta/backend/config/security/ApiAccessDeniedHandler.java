package com.feriaconecta.backend.config.security;

import com.feriaconecta.backend.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Devuelve errores 403 con el formato comun de la API
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private final JsonMapper jsonMapper;
    private final BearerTokenAccessDeniedHandler  bearerDeniedHandler =  new BearerTokenAccessDeniedHandler();

    public ApiAccessDeniedHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        //Conserva el comportamiento estandar del protocolo Bearer
        bearerDeniedHandler.handle(request, response, accessDeniedException);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        //reutiliza el formato y mensaje definidos
        ApiError error = ApiError.of(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para acceder a este recurso",
                request.getRequestURI()
        );

        response.getWriter().write(jsonMapper.writeValueAsString(error));
    }
}
