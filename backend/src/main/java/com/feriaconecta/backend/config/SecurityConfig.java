package com.feriaconecta.backend.config;

import com.feriaconecta.backend.config.security.ApiAccessDeniedHandler;
import com.feriaconecta.backend.config.security.ApiAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter, Environment environment, ApiAuthenticationEntryPoint apiAuthenticationEntryPoint, ApiAccessDeniedHandler apiAccessDeniedHandler, CorsConfigurationSource corsConfigurationSource) throws Exception {

        //H2 solo se habilita en desarrollo
        boolean habilitarH2 = environment.matchesProfiles("dev") && !environment.matchesProfiles("prod");

        http
                //habilita cors utilizando los origenes permitidos en la configuracion
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                //La api utiliza JWT, por lo que no necesita proteccion CSRF
                .csrf(AbstractHttpConfigurer::disable)
                //no crea ni utiliza sesiones HTTP para recuperar autenticacion
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
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
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                        .jwt(jwt -> jwt
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

    /**
     * Configura los orígenes, métodos y encabezados HTTP permitidos
     * para las peticiones realizadas desde el frontend.
     *
     * Los orígenes se obtienen desde la propiedad
     * feriaconecta.cors.allowed-origins, asociada a la variable
     * de entorno CORS_ALLOWED_ORIGINS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${feriaconecta.cors.allowed-origins}") String allowedOriginsProperty
    ) {
        // Convierte la lista de orígenes separados por comas
        // en una lista de valores sin espacios adicionales.
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        // Evita configuraciones vacías o que permitan
        // solicitudes desde cualquier origen mediante "*".
        if (allowedOrigins.isEmpty() || allowedOrigins.contains("*")) {
            throw new IllegalArgumentException("CORS requiere orígenes explícitos y no permite '*'");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriza únicamente los orígenes configurados.
        // En desarrollo se permite localhost:5173 y en producción
        // se utiliza la URL definida mediante CORS_ALLOWED_ORIGINS.
        configuration.setAllowedOrigins(allowedOrigins);

        // Define los métodos HTTP que puede utilizar el frontend
        // para realizar solicitudes a la API.
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        // Permite los encabezados necesarios para enviar datos
        // y utilizar tokens JWT mediante Authorization: Bearer.
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );
        // No habilita el envío de credenciales del navegador,
        // como cookies, porque la autenticación utiliza JWT.
        configuration.setAllowCredentials(false);
        // Permite que el navegador almacene durante una hora
        // el resultado de una petición preflight válida.
        configuration.setMaxAge(3600L);
        // Aplica la configuración CORS a todos los endpoints
        // de la aplicación.
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
