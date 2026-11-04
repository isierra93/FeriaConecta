package com.feriaconecta.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que la configuración CORS restrinja el acceso
 * al origen oficial del frontend desplegado en Vercel.
 * <p>
 * Utiliza el perfil test para evitar conexiones a producción
 * y sobrescribe únicamente la propiedad de orígenes permitidos.
 */
@SpringBootTest(properties =
        "feriaconecta.cors.allowed-origins=https://feria-conecta.vercel.app")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsProduccionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debePermitirOrigenOficialDeVercel() throws Exception {

        // Simula una petición preflight desde el frontend
        // oficial que estará autorizado en producción.
        mockMvc.perform(
                        options("/api/v1/actuator/health")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.ORIGIN,
                                        "https://feria-conecta.vercel.app")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                )
                // El origen oficial debe recibir una respuesta válida.
                .andExpect(status().isOk())

                // Confirma que Spring Security autoriza ese origen.
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://feria-conecta.vercel.app"
                ));
    }

    @Test
    void debeRechazarPreviewDeVercel() throws Exception {

        // Simula una petición desde una URL de preview
        // que no debe estar autorizada en producción.
        mockMvc.perform(
                        options("/api/v1/actuator/health")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.ORIGIN,
                                        "https://feria-conecta-git-prueba.vercel.app")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                )
                // El origen de preview debe ser rechazado.
                .andExpect(status().isForbidden())

                // No se devuelve autorización CORS para ese origen.
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }

    @Test
    void debeRechazarLocalhostEnConfiguracionDeProduccion() throws Exception {

        // Comprueba que el origen local tampoco está permitido
        // cuando se utiliza exclusivamente la URL de producción.
        mockMvc.perform(
                        options("/api/v1/actuator/health")
                                .contextPath("/api/v1")
                                .header(HttpHeaders.ORIGIN,
                                        "http://localhost:5173")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                )
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }
}
