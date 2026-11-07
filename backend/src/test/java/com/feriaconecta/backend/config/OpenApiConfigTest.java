package com.feriaconecta.backend.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la documentación OpenAPI / Swagger UI: acceso sin token, esquema
 * Bearer JWT, tags por módulo y su desactivación (como en producción).
 */
class OpenApiConfigTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class Habilitado {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void debeExponerEspecificacionSinToken() throws Exception {
            mockMvc.perform(get("/api/v1/v3/api-docs").contextPath("/api/v1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.info.title").value("FeriaConecta API"))
                    .andExpect(jsonPath("$.info.version").value("v1"));
        }

        @Test
        void debeDefinirEsquemaBearerJwt() throws Exception {
            mockMvc.perform(get("/api/v1/v3/api-docs").contextPath("/api/v1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                    .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                    .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                    .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
        }

        @Test
        void debeAgruparPorModulo() throws Exception {
            mockMvc.perform(get("/api/v1/v3/api-docs").contextPath("/api/v1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tags[*].name").value(hasItems(
                            "auth", "feria", "puesto", "postulacion", "pago",
                            "credencial", "acreditacion", "panel", "publico")));
        }

        @Test
        void debeRedirigirASwaggerUiSinToken() throws Exception {
            mockMvc.perform(get("/api/v1/swagger-ui.html").contextPath("/api/v1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            containsString("swagger-ui/index.html")));
        }
    }

    /** Simula la configuración de application-prod.yml sin levantar MySQL. */
    @Nested
    @SpringBootTest(properties = {
            "springdoc.api-docs.enabled=false",
            "springdoc.swagger-ui.enabled=false"
    })
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class Deshabilitado {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void noDebeExponerEspecificacion() throws Exception {
            mockMvc.perform(get("/api/v1/v3/api-docs").contextPath("/api/v1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void noDebeExponerSwaggerUi() throws Exception {
            mockMvc.perform(get("/api/v1/swagger-ui.html").contextPath("/api/v1"))
                    .andExpect(status().isNotFound());
        }
    }
}
