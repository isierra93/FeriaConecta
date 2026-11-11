package com.feriaconecta.backend.feria.controller;

import com.feriaconecta.backend.config.security.JwtService;
import com.feriaconecta.backend.feria.dto.DireccionNormalizadaResponse;
import com.feriaconecta.backend.feria.service.GeoRefService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP del controlador de sugerencias de direcciones.
 * <p>
 * Utiliza MockMvc y la configuración real de Spring Security
 * para verificar la autenticación, los permisos y las respuestas.
 * <p>
 * El servicio GeoRef se simula para evitar consultas externas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class GeoRefControllerTest {

    private static final String URL =
            "/api/v1/ferias/direcciones/sugerencias";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GeoRefService geoRefService;

    /**
     * Verifica que un organizador autenticado pueda consultar
     * sugerencias y recibir los datos normalizados.
     */
    @Test
    void debePermitirConsultaAOrganizador() throws Exception {

        String token = jwtService.generarToken(
                15, List.of("ORGANIZADOR"));

        DireccionNormalizadaResponse sugerencia =
                new DireccionNormalizadaResponse(
                        true,
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "San Martín",
                        "1234",
                        new BigDecimal("-34.70"),
                        new BigDecimal("-58.40"),
                        null
                );

        when(geoRefService.buscarSugerencias(
                "Buenos Aires",
                "Lanús",
                "Lanús Oeste",
                "San Martín",
                "1234"))
                .thenReturn(List.of(sugerencia));

        mockMvc.perform(solicitudValida()
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].normalizada").value(true))
                .andExpect(jsonPath("$[0].provincia").value("Buenos Aires"))
                .andExpect(jsonPath("$[0].municipio").value("Lanús"))
                .andExpect(jsonPath("$[0].calle").value("San Martín"))
                .andExpect(jsonPath("$[0].altura").value("1234"))
                .andExpect(jsonPath("$[0].latitud").value(-34.70))
                .andExpect(jsonPath("$[0].longitud").value(-58.40));
    }

    /**
     * Verifica que una dirección no normalizada se devuelva
     * con HTTP 200 y un aviso, sin generar un error.
     */
    @Test
    void debeResponder200CuandoNoSeNormaliza() throws Exception {

        String token = jwtService.generarToken(
                15, List.of("ORGANIZADOR"));

        DireccionNormalizadaResponse resultado =
                new DireccionNormalizadaResponse(
                        false,
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "San Martín",
                        "1234",
                        null,
                        null,
                        "No se pudo normalizar la dirección."
                );

        when(geoRefService.buscarSugerencias(
                "Buenos Aires",
                "Lanús",
                "Lanús Oeste",
                "San Martín",
                "1234"))
                .thenReturn(List.of(resultado));

        mockMvc.perform(solicitudValida()
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].normalizada").value(false))
                .andExpect(jsonPath("$[0].calle").value("San Martín"))
                .andExpect(jsonPath("$[0].altura").value("1234"))
                .andExpect(jsonPath("$[0].aviso").isNotEmpty());
    }

    /**
     * Verifica que el endpoint rechace solicitudes
     * que no incluyen un token JWT.
     */
    @Test
    void debeRechazarConsultaSinAutenticacion() throws Exception {

        mockMvc.perform(solicitudValida())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * Verifica que un usuario con rol EMPRENDEDOR
     * no pueda utilizar el endpoint del organizador.
     */
    @Test
    void debeRechazarConsultaDeEmprendedor() throws Exception {

        String token = jwtService.generarToken(
                16, List.of("EMPRENDEDOR"));

        mockMvc.perform(solicitudValida()
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /**
     * Verifica que se rechace una solicitud cuando
     * falta un parámetro obligatorio.
     */
    @Test
    void debeRechazarParametroFaltante() throws Exception {

        String token = jwtService.generarToken(
                15, List.of("ORGANIZADOR"));

        mockMvc.perform(get(URL)
                        .contextPath("/api/v1")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token)
                        .param("provincia", "Buenos Aires")
                        .param("municipio", "Lanús")
                        .param("localidad", "Lanús Oeste")
                        .param("calle", "San Martín"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /**
     * Verifica que un parámetro compuesto únicamente
     * por espacios sea rechazado por la validación.
     */
    @Test
    void debeRechazarCalleVacia() throws Exception {

        String token = jwtService.generarToken(
                15, List.of("ORGANIZADOR"));

        mockMvc.perform(get(URL)
                        .contextPath("/api/v1")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + token)
                        .param("provincia", "Buenos Aires")
                        .param("municipio", "Lanús")
                        .param("localidad", "Lanús Oeste")
                        .param("calle", "   ")
                        .param("altura", "1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /**
     * Construye una solicitud válida para reutilizar
     * los mismos parámetros en las pruebas HTTP.
     *
     * @return solicitud GET con los datos de dirección
     */
    private MockHttpServletRequestBuilder solicitudValida() {

        return get(URL)
                .contextPath("/api/v1")
                .param("provincia", "Buenos Aires")
                .param("municipio", "Lanús")
                .param("localidad", "Lanús Oeste")
                .param("calle", "San Martín")
                .param("altura", "1234");
    }
}
