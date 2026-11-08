package com.feriaconecta.backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @Test
    void debeGenerarYValidarTokenconAmbosRoles() {

        //Utiliza la configuracion real con una clave de prueba
        JwtConfig config = new JwtConfig();

        SecretKey clave = config.jwtSecretKey(
                "clave-de-prueba-para-jwt-de-mas-de-32-bytes"
        );

        JwtService service = new JwtService(
                config.jwtEncoder(clave),
                config.jwtDecoder(clave),
                Duration.ofMinutes(60)
        );

        //Genera un token para un usuario con ambos roles
        List<String> roles = List.of(
                "ORGANIZADOR",
                "EMPRENDEDOR"
        );

        String token = service.generarToken(15, roles);

        //Valida la firma y recupera los datos del token
        Jwt jwt = service.validarToken(token);

        assertEquals("15", jwt.getSubject());
        assertEquals(roles, jwt.getClaimAsStringList("roles"));

        //verifica las fechas de emision y expiracion
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
    }

    @Test
    void debeRechazarTokenConFirmaIncorrecta() {
        JwtConfig config = new JwtConfig();

        //dos claves diferentes
        SecretKey claveEmisora = config.jwtSecretKey("clave-de-prueba-para-jwt-de-mas-de-32-bytes");

        SecretKey claveDistinta = config.jwtSecretKey("otra-clave-de-prueba-para-jwt-de-mas-de-32-bytes");

        //genera el token con una clave, pero valida con otra
        JwtService service = new JwtService(
                config.jwtEncoder(claveEmisora),
                config.jwtDecoder(claveDistinta),
                Duration.ofMinutes(60)
        );

        String token = service.generarToken(15, List.of("ORGANIZADOR"));

        //la validacion debe rechazar la firma incorrecta
        assertThrows(
                JwtException.class, () -> service.validarToken(token)
        );
    }

    @Test
    void debeRechazarTokenExpirado() {

        JwtConfig config = new JwtConfig();
        SecretKey clave = config.jwtSecretKey(
                "clave-de-prueba-para-jwt-de-mas-de-32-bytes"
        );

        JwtService service = new JwtService(
                config.jwtEncoder(clave),
                config.jwtDecoder(clave),
                Duration.ofMinutes(60)
        );

        //se crea un token que expiro hace una hora
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("15")
                .issuedAt(now.minus(Duration.ofHours(2)))
                .expiresAt(now.minus((Duration.ofHours(1))))
                .claim("roles", List.of("ORGANIZADOR"))
                .build();

        //se firma el token expirado con la clave correcta
        String tokenExpirado = config.jwtEncoder(clave)
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                ))
                .getTokenValue();

        //aunque la firma sea valida, debe rechazarse
        assertThrows(JwtException.class, () -> service.validarToken(tokenExpirado));
    }

    @Test
    void debeConvertirRolesEnAutoridades() {
        JwtConfig config = new JwtConfig();

        SecretKey clave = config.jwtSecretKey(
                "clave-de-prueba-para-jwt-de-mas-de-32-bytes"
        );

        JwtService service = new JwtService(
                config.jwtEncoder(clave),
                config.jwtDecoder(clave),
                Duration.ofMinutes(60)
        );

        //genera un jwt con ambos roles
        String token = service.generarToken(15, List.of("ORGANIZADOR", "EMPRENDEDOR"));

        Jwt jwt = service.validarToken(token);

        //convierte los roles en autoridades de Spring security
        var authentication = config.jwtAuthenticationConverter().convert(jwt);

        assertNotNull(authentication);

        List<String> autoridades = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .toList();

        assertEquals(
                List.of("ROLE_EMPRENDEDOR", "ROLE_ORGANIZADOR"),
                autoridades
        );
    }
}
