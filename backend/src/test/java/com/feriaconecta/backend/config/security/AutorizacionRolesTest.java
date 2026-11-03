package com.feriaconecta.backend.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(AutorizacionRolesTest.ConfiguracionPrueba.class)
class AutorizacionRolesTest {

    @Autowired
    private ServicioProtegido servicio;
    @Autowired
    private MockMvc mockMvc;

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
                        .contextPath("/api/v1")
        ).andExpect(status().isUnauthorized());
    }


    @Test
    void debeRechazarBearerTokenInvalido() throws Exception {

        // Una ruta protegida debe rechazar un JWT inválido.
        mockMvc.perform(
                get("/api/v1/auth/me")
                        .contextPath("/api/v1")
                        .header("Authorization", "Bearer token-invalido")
        ).andExpect(status().isUnauthorized());
    }
}