package com.feriaconecta.backend.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class JwtConfig {

    /**
     * Obtiene la clave secreta desde la configuración.
     *
     * @param secret clave secreta configurada
     * @return clave que se utilizará para firmar y validar JWT
     * @Value permite leer la propiedad definida en application.yml.
     * La clave se convierte a bytes UTF-8, sin decodificar Base64.
     */
    @Bean
    public SecretKey jwtSecretKey(
            @Value("${feriaconecta.jwt.secret}") String secret) {
        //se convierte el secret a bytes usando UTF-8
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        //se verifica que la clave no sea demasiado corta
        // HS256 necesita una clave de al menos 256 bits (32 bytes)
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes UTF-8");
        }

        //crea una clave compatible con HS256
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Configura el componente encargado de generar tokens JWT.
     * El módulo de autenticación lo utilizará para emitir
     * tokens cuando un usuario inicie sesión correctamente.
     *
     * @param jwtSecretKey clave utilizada para firmar tokens
     * @return componente que genera JWT
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Configura el componente encargado de validar tokens JWT.
     * Comprueba la firma del token utilizando la misma clave
     * y el algoritmo HS256.
     * Spring Security utilizará este componente para
     * autenticar las peticiones que reciban un JWT.
     *
     * @param jwtSecretKey clave utilizada para verificar tokens
     * @return componente que valida JWT
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Configura y valida la duración de los tokens JWT.
     * Lee la cantidad de minutos desde application.yml.
     * La duración debe ser mayor que cero.
     * Este bean será utilizado posteriormente por JwtService.
     */
    @Bean("jwtExpiration")
    public Duration jwtExpiration(@Value("${feriaconecta.jwt.expiration-minutes}") long minutes) {

        //evita generar tokens con una duracion invalida
        if (minutes <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION debe ser mayor a cero.");
        }
        //se convierte los minutos a una duration de Java
        return Duration.ofMinutes(minutes);
    }

}
