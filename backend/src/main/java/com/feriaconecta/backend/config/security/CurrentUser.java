package com.feriaconecta.backend.config.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Util para obtener los datos del usuario autenticado.
 * Recupera la información desde Spring Security,
 * sin consultar la base de datos ni validar nuevamente el JWT.
 */
@Component
public class CurrentUser {
    /**
     * Obtiene el JWT del usuario autenticado.
     * Spring Security guarda la autenticación de la petición
     * en SecurityContextHolder.
     *
     * @return JWT del usuario autenticado
     */
    private Jwt obtenerJwt() {
        //se recupera la autentificacion actual
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        //se verifica que exista un usuario autenticado con JWT
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "No hay un usuario autenticado mediante JWT"
            );
        }
        //se castea y retorna el token
        return ((JwtAuthenticationToken) authentication).getToken();
    }

    /**
     * Obtiene el identificador del usuario autenticado.
     * El ID se encuentra en el claim "sub" del JWT.
     *
     * @return identificador del usuario
     */
    public Long getId() {
        String subject = obtenerJwt().getSubject();

        try {
            long id = Long.parseLong(subject);

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("El JWT no contiene un ID de usuario valido.", ex);
        }
    }

    /**
     * Obtiene los roles del usuario autenticado.

     * Los roles se encuentran en el claim "roles".
     * Si no están presentes, devuelve una lista vacía.
     *
     * @return lista de roles del usuario
     */
    public List<String> getRoles() {

        List<String> roles =
                obtenerJwt().getClaimAsStringList("roles");

        return roles == null ? List.of() : List.copyOf(roles);
    }
}
