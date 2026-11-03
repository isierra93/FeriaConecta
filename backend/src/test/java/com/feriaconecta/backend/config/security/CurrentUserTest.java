package com.feriaconecta.backend.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class CurrentUserTest {

    @Test
    void debeObtenerIdYRolesDelUsuarioAutenticado() {

        List<String> roles = List.of(
                "ORGANIZADOR",
                "EMPRENDEDOR"
        );

        // JWT de prueba con los datos del usuario.
        Jwt jwt = Jwt.withTokenValue("token-de-prueba")
                .header("alg", "none")
                .subject("15")
                .claim("roles", roles)
                .build();

        // Simulamos una autenticación JWT ya validada.
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );

        try {
            CurrentUser currentUser = new CurrentUser();

            assertEquals(Long.valueOf(15L), currentUser.getId());
            assertEquals(roles, currentUser.getRoles());

        } finally {
            // Evita afectar otras pruebas.
            SecurityContextHolder.clearContext();
        }
    }
}