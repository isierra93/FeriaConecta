package com.feriaconecta.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter, Environment environment) throws Exception {

        //H2 solo se habilita en desarrollo
        boolean habilitarH2 = environment.matchesProfiles("dev") && !environment.matchesProfiles("dev");

        http
                //La apu utiliza JWT, por lo que no necesita proteccion CSRF
                .csrf(AbstractHttpConfigurer::disable)
                //no crea ni utiliza sesiones HTTP para recuperar autenticacion
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {
                    //registro e inicio de sesion
                    auth.requestMatchers(
                            "/auth/registro",
                            "/auth/login"
                    ).permitAll();
                    // Endpoints públicos de la aplicación.
                    auth.requestMatchers("/public/**").permitAll();
                    // Endpoint de salud para monitoreo.
                    auth.requestMatchers("/actuator/health").permitAll();
                    // Documentación Swagger / OpenAPI.
                    auth.requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**"
                    ).permitAll();
                    // Webhook de Mercado Pago.
                    // No requiere JWT, pero debe validar x-signature.
                    auth.requestMatchers("/webhooks/mercadopago").permitAll();
                    // La consola H2 se permite únicamente en dev.
                    if (habilitarH2) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    } else {
                        auth.requestMatchers("/h2-console/**").denyAll();
                    }

                    // Todas las demás rutas requieren autenticación.
                    auth.anyRequest().authenticated();
                })
                //se utiliza el filtro jwt que proporciona spring security
                //el token se valida mediante el jwtDecoder configurado
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        // Utiliza el convertidor definido en JwtConfig.
                        // Transforma ORGANIZADOR en ROLE_ORGANIZADOR
                        // y EMPRENDEDOR en ROLE_EMPRENDEDOR.
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        if (habilitarH2) {
            // sameOrigin permite embeber la consola H2 (solo activa en el perfil dev)
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        }

        return http.build();
    }

    /**
     * Configura el codificador de contraseñas de la aplicación.
     * BCrypt genera un hash seguro para almacenar contraseñas,
     * evitando guardar su contenido en texto plano.
     * <p>
     * util para autenticación para registrar y validar usuarios.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
