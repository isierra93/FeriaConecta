package com.feriaconecta.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la configuración CORS aplicada por Spring Security.
 * <p>
 * Comprueba que el backend acepte solicitudes preflight
 * desde los orígenes autorizados.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debePermitirPreflightDesdeFrontendLocal() throws Exception {

        // Simula una petición preflight enviada por el navegador
        // desde el frontend local antes de realizar una solicitud GET.
        mockMvc.perform(
                        options("/api/v1/actuator/health")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "Authorization, Content-Type")
                )

                // El preflight debe responder correctamente sin JWT.
                .andExpect(status().isOk())

                // Confirma que el backend autoriza el origen local.
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                ))

                // Verifica que el método GET esté permitido.
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        containsString("GET")
                ))

                // Verifica que se admita el encabezado Authorization.
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsString("Authorization")
                ));
    }


    @Test
    void debeRechazarPreflightDesdeOrigenNoAutorizado() throws Exception {

        // Simula una petición preflight desde una URL de preview
        // de Vercel que no está incluida entre los orígenes permitidos.
        mockMvc.perform(
                        options("/api/v1/actuator/health")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.ORIGIN,
                                        "https://feria-conecta-git-prueba.vercel.app")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "Authorization")
                )

                // Spring rechaza la petición porque el origen
                // no pertenece a la lista de orígenes autorizados.
                .andExpect(status().isForbidden())

                // Confirma que no se devuelve el encabezado
                // que autorizaría al navegador a acceder a la API.
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }
}
