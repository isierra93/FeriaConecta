package com.feriaconecta.backend.common.exception;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Pruebas del manejador global con MockMvc standalone: no levanta el contexto
 * de Spring ni la seguridad, solo el controller de prueba y el advice.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PruebaController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNoEncontradoDevuelve404() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/no-encontrado")), 404, "Not Found",
                "No se encontró la feria con id 7", "/prueba/no-encontrado");
    }

    @Test
    void conflictoDevuelve409() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/conflicto")), 409, "Conflict",
                "Ya existe una postulación para esta feria", "/prueba/conflicto");
    }

    @Test
    void reglaDeNegocioDevuelve422() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/regla-negocio")), 422, "Unprocessable Content",
                "La feria no admite postulaciones", "/prueba/regla-negocio");
    }

    @Test
    void noAutorizadoDevuelve401() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/no-autorizado")), 401, "Unauthorized",
                "Credenciales inválidas", "/prueba/no-autorizado");
    }

    @Test
    void prohibidoDevuelve403() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/prohibido")), 403, "Forbidden",
                "La feria no le pertenece", "/prueba/prohibido");
    }

    @Test
    void accesoDenegadoDeSpringSecurityDevuelve403YNo500() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/acceso-denegado")), 403, "Forbidden",
                "No tiene permisos para acceder a este recurso", "/prueba/acceso-denegado");
    }

    @Test
    void errorDeValidacionDevuelve400ConDetallePorCampo() throws Exception {
        mockMvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"cupo\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud tiene datos inválidos"))
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.details[*].field").value(containsInAnyOrder("nombre", "cupo")))
                .andExpect(jsonPath("$.details[0].message").isNotEmpty());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud es inválido o está mal formado"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void parametroConTipoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/ferias/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un valor inválido"));
    }

    @Test
    void parametroObligatorioFaltanteDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/buscar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Falta el parámetro obligatorio 'q'"));
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/no-existe")), 404, "Not Found",
                "Recurso no encontrado", "/prueba/no-existe");
    }

    @Test
    void metodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(post("/prueba/conflicto"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void violacionDeIntegridadDevuelve409SinDetalleSql() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/integridad")), 409, "Conflict",
                "La operación entra en conflicto con datos existentes", "/prueba/integridad")
                .andExpect(content().string(not(containsString("uk_postulacion"))));
    }

    @Test
    void errorNoControladoDevuelve500SinExponerDetalles() throws Exception {
        esperarError(mockMvc.perform(get("/prueba/inesperado")), 500, "Internal Server Error",
                "Ocurrió un error inesperado", "/prueba/inesperado")
                .andExpect(content().string(not(containsString("secreto"))))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    private ResultActions esperarError(ResultActions resultado, int status, String error, String message,
            String path) throws Exception {
        return resultado
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    record PruebaRequest(@NotBlank String nombre, @Positive int cupo) {
    }

    // Clase interna NO estática a propósito: el escaneo de componentes de los
    // tests con contexto completo la ignora, así no se registra como controller real.
    @RestController
    @RequestMapping("/prueba")
    class PruebaController {

        @GetMapping("/no-encontrado")
        void noEncontrado() {
            throw new ResourceNotFoundException("la feria", 7);
        }

        @GetMapping("/conflicto")
        void conflicto() {
            throw new ConflictException("Ya existe una postulación para esta feria");
        }

        @GetMapping("/regla-negocio")
        void reglaNegocio() {
            throw new BusinessRuleException("La feria no admite postulaciones");
        }

        @GetMapping("/no-autorizado")
        void noAutorizado() {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        @GetMapping("/prohibido")
        void prohibido() {
            throw new ForbiddenException("La feria no le pertenece");
        }

        @GetMapping("/acceso-denegado")
        void accesoDenegado() {
            throw new AccessDeniedException("Access Denied");
        }

        @PostMapping("/validacion")
        void validacion(@Valid @RequestBody PruebaRequest request) {
        }

        @GetMapping("/ferias/{id}")
        void feria(@PathVariable Long id) {
        }

        @GetMapping("/buscar")
        void buscar(@RequestParam String q) {
        }

        @GetMapping("/integridad")
        void integridad() {
            throw new DataIntegrityViolationException("Duplicate entry for key 'uk_postulacion'");
        }

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalle secreto interno");
        }
    }
}
