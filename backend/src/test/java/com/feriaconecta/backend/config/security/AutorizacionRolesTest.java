package com.feriaconecta.backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import javax.crypto.SecretKey;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(AutorizacionRolesTest.ConfiguracionPrueba.class)
class AutorizacionRolesTest {

    @Autowired
    private ServicioProtegido servicio;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApiAccessDeniedHandler apiAccessDeniedHandler;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtEncoder jwtEncoder;


    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void organizadorSoloAccedeASuOperacion() {

        assertEquals("OK", servicio.soloOrganizador());

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.soloEmprendedor()
        );
    }

    @Test
    @WithMockUser(roles = "EMPRENDEDOR")
    void emprendedorSoloAccedeASuOperacion() {

        assertEquals("OK", servicio.soloEmprendedor());

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.soloOrganizador()
        );
    }

    // Configuración utilizada únicamente por esta prueba.
    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionPrueba {

        @Bean
        ServicioProtegido servicioProtegido() {
            return new ServicioProtegido();
        }

        @Bean
        ControladorProtegido controladorProtegido() {
            return new ControladorProtegido();
        }
    }

    static class ServicioProtegido {

        @PreAuthorize("hasRole('ORGANIZADOR')")
        public String soloOrganizador() {
            return "OK";
        }

        @PreAuthorize("hasRole('EMPRENDEDOR')")
        public String soloEmprendedor() {
            return "OK";
        }
    }

    @RestController
    static class ControladorProtegido {
        @GetMapping("/pruebas-seguridad/organizador")
        @PreAuthorize("hasRole('ORGANIZADOR')")
        public String soloOrganizador() {
            return "OK";
        }
    }


    @Test
    @WithMockUser(roles = {"ORGANIZADOR", "EMPRENDEDOR"})
    void usuarioConAmbosRolesAccedeAAmbasOperaciones() {

        // Un usuario con ambos roles puede ejecutar
        // las operaciones correspondientes a cada uno.
        assertEquals("OK", servicio.soloOrganizador());
        assertEquals("OK", servicio.soloEmprendedor());
    }


    @Test
    void debePermitirAccesoPublicoSinJwt() throws Exception {

        // Endpoint público existente: responde correctamente sin JWT.
        mockMvc.perform(
                get("/api/v1/actuator/health")
                        .contextPath("/api/v1")
        ).andExpect(status().isOk());

        // Ruta pública sin controlador implementado:
        // devuelve 404, no un error de autenticación.
        mockMvc.perform(
                get("/api/v1/public/prueba")
                        .contextPath("/api/v1")
        ).andExpect(status().isNotFound());
    }


    @Test
    void debeRechazarRutaProtegidaSinJwt() throws Exception {

        // /auth/me requiere autenticación.
        // Sin token JWT, debe responder HTTP 401.
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .contextPath("/api/v1"))
                //Verifica el estado HTTP
                .andExpect(status().isUnauthorized())
                //Verifica que la respuesta sea JSON
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                //verifica el formato comun ApiError
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Se requiere autenticación"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"))

                //conserva el protocolo de autenticacion bearer
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")));
    }


    @Test
    void debeRechazarBearerTokenInvalido() throws Exception {

        // Una ruta protegida debe rechazar un JWT inválido.
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .contextPath("/api/v1")
                                .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Se requiere autenticación"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"))
                .andExpect(header().string(
                        HttpHeaders.WWW_AUTHENTICATE,
                        containsString("Bearer")
                ));
    }

    @Test
    void debeResponder403ConApiError() throws Exception {
        // Simula una petición a un recurso protegido.
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/api/v1/auth/me"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Ejecuta el manejador de acceso denegado.
        apiAccessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("Acceso denegado")
        );

        // Comprueba el estado HTTP y el tipo de respuesta.
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType()
                .startsWith("application/json"));

        // Comprueba el formato ApiError de la issue #26.
        JsonNode cuerpo = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(403, cuerpo.path("status").asInt());
        assertEquals("Forbidden", cuerpo.path("error").asString());
        assertEquals(
                "No tiene permisos para acceder a este recurso",
                cuerpo.path("message").asString()
        );
        assertEquals(
                "/api/v1/auth/me",
                cuerpo.path("path").asString()
        );
    }


    @Test
    void organizadorPuedeAccederPorHttp() throws Exception {

        // Genera un JWT válido con el rol ORGANIZADOR.
        String token = jwtService.generarToken(
                15L,
                List.of("ORGANIZADOR")
        );

        // Envía el token a una ruta protegida por rol.
        mockMvc.perform(
                        get("/api/v1/pruebas-seguridad/organizador")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void emprendedorRecibe403PorHttp() throws Exception {

        // Genera un JWT válido con el rol EMPRENDEDOR.
        String token = jwtService.generarToken(
                16L,
                List.of("EMPRENDEDOR")
        );

        // Intenta acceder a una operación exclusiva del ORGANIZADOR.
        mockMvc.perform(
                        get("/api/v1/pruebas-seguridad/organizador")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(
                        "No tiene permisos para acceder a este recurso"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/pruebas-seguridad/organizador"));
    }


    @Test
    void debeResponder401ConJwtVencidoPorHttp() throws Exception {

        Instant ahora = Instant.now();

        // Crea un JWT cuya expiración ya pasó.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("15")
                .issuedAt(ahora.minus(Duration.ofHours(2)))
                .expiresAt(ahora.minus(Duration.ofHours(1)))
                .claim("roles", List.of("ORGANIZADOR"))
                .build();

        // Firma el token con la clave configurada en la aplicación.
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        // Verifica que Spring Security rechace el token vencido.
        mockMvc.perform(
                        get("/api/v1/pruebas-seguridad/organizador")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Se requiere autenticación"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/pruebas-seguridad/organizador"));
    }


    @Test
    void debeResponder401ConJwtDeFirmaIncorrectaPorHttp() throws Exception {

        // Genera una clave diferente de la configurada en la aplicación.
        JwtConfig config = new JwtConfig();

        SecretKey claveDistinta = config.jwtSecretKey(
                "otra-clave-de-prueba-para-jwt-de-mas-de-32-bytes"
        );

        JwtEncoder encoderDistinto = config.jwtEncoder(claveDistinta);

        // Crea un JWT vigente con el rol ORGANIZADOR.
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("15")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofHours(1)))
                .claim("roles", List.of("ORGANIZADOR"))
                .build();

        // Firma el JWT con una clave diferente.
        String token = encoderDistinto.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        // La API debe rechazar el token por su firma incorrecta.
        mockMvc.perform(
                        get("/api/v1/pruebas-seguridad/organizador")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Se requiere autenticación"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/pruebas-seguridad/organizador"));
    }
}