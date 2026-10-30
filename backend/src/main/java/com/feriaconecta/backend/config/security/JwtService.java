package com.feriaconecta.backend.config.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Servicio encargado de generar tokens JWT.
 * Recibe el identificador y los roles del usuario,
 * y genera un token firmado mediante HS256.
 * No consulta la base de datos ni depende del módulo auth.
 */
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final Duration jwtExpiration;
    private final JwtDecoder jwtDecoder;

    /**
     * Recibe los componentes configurados en JwtConfig.
     *
     * @param jwtEncoder    componente que firma los tokens
     * @param jwtDecoder    decodifica y valida tokens JWT
     * @param jwtExpiration duración configurada del token
     */
    public JwtService(JwtEncoder jwtEncoder, JwtDecoder JwtDecoder, @Qualifier("jwtExpiration") Duration jwtExpiration) {
        this.jwtEncoder = jwtEncoder;
        this.jwtExpiration = jwtExpiration;
        this.jwtDecoder = JwtDecoder;
    }

    /**
     * Genera un JWT para un usuario autenticado.
     *
     * @param userId identificador del usuario
     * @param roles  roles asignados al usuario
     * @return token JWT firmado
     */
    public String generarToken(long userId, Collection<String> roles) {

        //se verifica que el identificador sea valido
        if (userId <= 0) {
            throw new IllegalArgumentException("El usuario no puede ser menor que 0");
        }

        //el usuario debe tener almenos un rol
        if (roles == null || roles.isEmpty() || roles.stream().anyMatch(rol -> rol == null || rol.isBlank())) {
            throw new IllegalArgumentException("El usuario debe tener roles validos");
        }

        //se obtiene el momento actual
        Instant now = Instant.now();

        //se define los dato incluidos en el token
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(Long.toString(userId))
                .claim("role", List.copyOf(roles))
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration))
                .build();

        //se indica que el token debe firmarse con HS256
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        //se firma el token y retorna su representacion
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Valida un token JWT utilizando Spring Security.
     * JwtDecoder verifica la firma y aplica las validaciones
     * configuradas, incluyendo la expiración del token.
     *
     * @param token JWT que se desea validar
     * @return token decodificado con sus claims
     * @throws JwtException si el token es inválido o expiró
     */
    public Jwt validarToken(String token) {

        // Delega la validación al componente de Spring Security.
        return jwtDecoder.decode(token);
    }
}
