package com.feriaconecta.backend.feria.controller;

import com.feriaconecta.backend.common.exception.BusinessRuleException;
import com.feriaconecta.backend.common.exception.ConflictException;
import com.feriaconecta.backend.common.exception.ResourceNotFoundException;
import com.feriaconecta.backend.common.model.Categoria;
import com.feriaconecta.backend.config.security.JwtService;
import com.feriaconecta.backend.feria.dto.CambiarEstadoRequest;
import com.feriaconecta.backend.feria.dto.FeriaRequest;
import com.feriaconecta.backend.feria.dto.FeriaResponse;
import com.feriaconecta.backend.feria.model.EstadoFeria;
import com.feriaconecta.backend.feria.service.FeriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP de los endpoints de ferias.
 *
 * <p>Utiliza Spring Security real y simula el servicio para comprobar
 * las rutas, las validaciones y las respuestas HTTP, sin acceder a MySQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class FeriaControllerTest {

    private static final String URL = "/api/v1/ferias";
    private static final String CONTEXTO = "/api/v1";
    private static final String JSON_ESTADO = "{\"estado\":\"CANCELADA\"}";

    private static final String JSON_VALIDO = """
            {
              "nombre": "Feria Artesanal",
              "fecha": "2099-12-15",
              "categoriaPermitida": "ARTESANIAS",
              "costoParticipacion": 15000.00,
              "ubicacion": {
                "provincia": "Buenos Aires",
                "municipio": "Lanús",
                "localidad": "Lanús Oeste",
                "calle": "San Martín",
                "altura": "1234",
                "latitud": -34.70000000,
                "longitud": -58.40000000
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private FeriaService feriaService;

    // Creación

    /**
     * Verifica la creación de una feria en estado BORRADOR.
     */
    @Test
    void debeCrearFeriaSiEsOrganizador() throws Exception {
        when(feriaService.crear(any(FeriaRequest.class)))
                .thenReturn(crearRespuesta("Feria Artesanal", EstadoFeria.BORRADOR));

        mockMvc.perform(conOrganizador(post(URL)
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.ubicacion.latitud").value(-34.7));

        verify(feriaService).crear(any(FeriaRequest.class));
    }

    /**
     * Comprueba la validación del nombre obligatorio.
     */
    @Test
    void debeRechazarNombreVacio() throws Exception {
        String json = JSON_VALIDO.replace(
                "\"nombre\": \"Feria Artesanal\"", "\"nombre\": \"   \"");

        mockMvc.perform(conOrganizador(post(URL)
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("nombre"));

        verifyNoInteractions(feriaService);
    }

    /**
     * Comprueba que también se validen los datos internos de ubicación.
     */
    @Test
    void debeRechazarCalleVacia() throws Exception {
        String json = JSON_VALIDO.replace(
                "\"calle\": \"San Martín\"", "\"calle\": \"   \"");

        mockMvc.perform(conOrganizador(post(URL)
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("ubicacion.calle"));

        verifyNoInteractions(feriaService);
    }

    /**
     * Comprueba que el conflicto de una feria duplicada devuelva HTTP 409.
     */
    @Test
    void debeResponder409CuandoLaFeriaEstaDuplicada() throws Exception {
        when(feriaService.crear(any(FeriaRequest.class)))
                .thenThrow(new ConflictException("Ya existe una feria con ese nombre y fecha."));

        mockMvc.perform(conOrganizador(post(URL)
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // Consultas

    /**
     * Comprueba que el listado se devuelva como un arreglo JSON.
     */
    @Test
    void debeListarFeriasDelOrganizador() throws Exception {
        when(feriaService.listarMisFerias())
                .thenReturn(List.of(crearRespuesta("Feria Artesanal", EstadoFeria.BORRADOR)));

        mockMvc.perform(conOrganizador(get(URL + "/me").contextPath(CONTEXTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Feria Artesanal"));
    }

    /**
     * Comprueba la respuesta cuando el organizador no tiene ferias.
     */
    @Test
    void debeDevolverListaVaciaSiNoTieneFerias() throws Exception {
        when(feriaService.listarMisFerias()).thenReturn(List.of());

        mockMvc.perform(conOrganizador(get(URL + "/me").contextPath(CONTEXTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Comprueba la consulta del detalle de una feria.
     */
    @Test
    void debeObtenerDetalleDeFeria() throws Exception {
        when(feriaService.obtenerDetalle(1))
                .thenReturn(crearRespuesta("Feria Artesanal", EstadoFeria.BORRADOR));

        mockMvc.perform(conOrganizador(get(URL + "/1").contextPath(CONTEXTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ubicacion.municipio").value("Lanús"));

        verify(feriaService).obtenerDetalle(1);
    }

    /**
     * Comprueba que una feria no accesible devuelva HTTP 404.
     */
    @Test
    void debeResponder404SiFeriaNoExiste() throws Exception {
        when(feriaService.obtenerDetalle(99))
                .thenThrow(new ResourceNotFoundException("la feria", 99));

        mockMvc.perform(conOrganizador(get(URL + "/99").contextPath(CONTEXTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // Edición

    /**
     * Comprueba que el endpoint PUT devuelva los datos actualizados.
     */
    @Test
    void debeEditarFeriaSiEsOrganizador() throws Exception {
        when(feriaService.editar(eq(1), any(FeriaRequest.class)))
                .thenReturn(crearRespuesta("Feria Actualizada", EstadoFeria.BORRADOR));

        mockMvc.perform(conOrganizador(put(URL + "/1")
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Feria Actualizada"));

        verify(feriaService).editar(eq(1), any(FeriaRequest.class));
    }

    /**
     * Comprueba que PUT aplique las validaciones de FeriaRequest.
     */
    @Test
    void debeRechazarEdicionConNombreVacio() throws Exception {
        String json = JSON_VALIDO.replace(
                "\"nombre\": \"Feria Artesanal\"", "\"nombre\": \"   \"");

        mockMvc.perform(conOrganizador(put(URL + "/1")
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(feriaService);
    }

    // Cambio de estado

    /**
     * Comprueba la respuesta del endpoint PATCH.
     */
    @Test
    void debeCambiarEstadoSiEsOrganizador() throws Exception {
        when(feriaService.cambiarEstado(eq(1), any(CambiarEstadoRequest.class)))
                .thenReturn(crearRespuesta("Feria Artesanal", EstadoFeria.CANCELADA));

        mockMvc.perform(conOrganizador(patch(URL + "/1/estado")
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_ESTADO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        verify(feriaService).cambiarEstado(eq(1), any(CambiarEstadoRequest.class));
    }

    /**
     * Comprueba que el estado sea obligatorio en PATCH.
     */
    @Test
    void debeRechazarCambioDeEstadoSinEstado() throws Exception {
        mockMvc.perform(conOrganizador(patch(URL + "/1/estado")
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(feriaService);
    }

    /**
     * Comprueba que una transición rechazada devuelva HTTP 422.
     */
    @Test
    void debeResponder422SiLaTransicionNoEsPermitida() throws Exception {
        when(feriaService.cambiarEstado(eq(1), any(CambiarEstadoRequest.class)))
                .thenThrow(new BusinessRuleException("No se permite realizar esta transición."));

        mockMvc.perform(conOrganizador(patch(URL + "/1/estado")
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_ESTADO)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    // Seguridad

    /**
     * Comprueba que una solicitud sin JWT sea rechazada.
     */
    @Test
    void debeRechazarCreacionSinAutenticacion() throws Exception {
        mockMvc.perform(post(URL)
                        .contextPath(CONTEXTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(feriaService);
    }

    /**
     * Comprueba que los cinco endpoints exijan rol ORGANIZADOR.
     * Se prueban por separado porque cada uno tiene su propia autorización.
     */
    @ParameterizedTest
    @CsvSource({
            "POST, /api/v1/ferias",
            "GET, /api/v1/ferias/me",
            "GET, /api/v1/ferias/1",
            "PUT, /api/v1/ferias/1",
            "PATCH, /api/v1/ferias/1/estado"
    })
    void debeRechazarEmprendedorEnTodosLosEndpoints(String metodo, String ruta)
            throws Exception {
        HttpMethod metodoHttp = HttpMethod.valueOf(metodo);
        MockHttpServletRequestBuilder solicitud = request(metodoHttp, ruta)
                .contextPath(CONTEXTO)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtService.generarToken(16, List.of("EMPRENDEDOR")));

        if (HttpMethod.POST.equals(metodoHttp) || HttpMethod.PUT.equals(metodoHttp)) {
            solicitud.contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO);
        } else if (HttpMethod.PATCH.equals(metodoHttp)) {
            solicitud.contentType(MediaType.APPLICATION_JSON).content(JSON_ESTADO);
        }

        mockMvc.perform(solicitud)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(feriaService);
    }

    // Datos comunes de prueba

    /**
     * Agrega un JWT de organizador a la solicitud.
     */
    private MockHttpServletRequestBuilder conOrganizador(MockHttpServletRequestBuilder solicitud) {
        return solicitud.header(HttpHeaders.AUTHORIZATION,
                "Bearer " + jwtService.generarToken(15, List.of("ORGANIZADOR")));
    }

    /**
     * Crea una respuesta de ejemplo para las pruebas HTTP.
     */
    private FeriaResponse crearRespuesta(String nombre, EstadoFeria estado) {
        return new FeriaResponse(
                1,
                nombre,
                LocalDate.of(2099, 12, 15),
                Categoria.ARTESANIAS,
                new BigDecimal("15000.00"),
                estado,
                new FeriaResponse.UbicacionDetalle(
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "San Martín",
                        "1234",
                        new BigDecimal("-34.70000000"),
                        new BigDecimal("-58.40000000")
                ),
                null
        );
    }
}
