package com.feriaconecta.backend.auth.controller;

import com.feriaconecta.backend.auth.model.Rol;
import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.repository.UsuarioRepository;
import com.feriaconecta.backend.config.MySqlTestcontainersConfig;
import com.feriaconecta.backend.config.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración HTTP del módulo de autenticación.
 * <p>
 * Utiliza MockMvc para ejecutar las solicitudes contra
 * los controladores reales y MySQL Testcontainers para
 * verificar la persistencia mediante el esquema original.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
@Transactional
class AuthControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JsonMapper jsonMapper;

    /**
     * Verifica que el registro mediante HTTP cree una cuenta,
     * almacene la contraseña hasheada y devuelva un JWT válido
     * con el identificador y los dos roles del usuario.
     */
    @Test
    void debeRegistrarUsuarioYDevolverJwt() throws Exception {

        // Simula los datos enviados por el frontend.
        String request = """
                {
                  "email": "registro31@ejemplo.com",
                  "nombre": "Usuario de prueba",
                  "contrasena": "Clave123!",
                  "roles": ["ORGANIZADOR", "EMPRENDEDOR"]
                }
                """;

        // Ejecuta el endpoint real y comprueba la respuesta HTTP.
        var resultado = mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Recupera el usuario persistido en MySQL.
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase("registro31@ejemplo.com")
                .orElseThrow();

        // Comprueba que la contraseña no se guardó en texto plano.
        assertNotEquals("Clave123!", usuario.getContrasenaHash());
        assertTrue(passwordEncoder.matches(
                "Clave123!",
                usuario.getContrasenaHash()
        ));

        // Comprueba que ambos roles fueron registrados.
        assertEquals(
                Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR),
                usuario.getRoles()
        );

        // Recupera y valida el JWT devuelto por el controlador.
        JsonNode respuesta = jsonMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        String token = respuesta.path("token").asString();
        Jwt jwt = jwtService.validarToken(token);

        // Confirma que el JWT pertenece al usuario creado.
        assertEquals(
                usuario.getId().toString(),
                jwt.getSubject()
        );

        assertEquals(
                Set.of("ORGANIZADOR", "EMPRENDEDOR"),
                Set.copyOf(jwt.getClaimAsStringList("roles"))
        );
    }


    /**
     * Verifica que no se puedan registrar dos cuentas
     * con el mismo correo electrónico.
     * <p>
     * También comprueba que el error HTTP 409 respete
     * el formato ApiError del manejador global.
     */
    @Test
    void debeResponder409CuandoElEmailYaEstaRegistrado() throws Exception {

        // Primera solicitud: registra correctamente al usuario.
        String primerRegistro = """
                {
                  "email": "duplicado31@ejemplo.com",
                  "nombre": "Usuario de prueba",
                  "contrasena": "Clave123!",
                  "roles": ["ORGANIZADOR"]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primerRegistro))
                .andExpect(status().isCreated());

        // Segunda solicitud: utiliza el mismo email en mayúsculas.
        // Debe detectarse como duplicado.
        String segundoRegistro = """
                {
                  "email": "DUPLICADO31@EJEMPLO.COM",
                  "nombre": "Otro usuario",
                  "contrasena": "OtraClave123!",
                  "roles": ["EMPRENDEDOR"]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segundoRegistro))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "El correo electrónico ya está registrado"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/auth/registro"
                ));
    }


    /**
     * Verifica que una solicitud de registro con datos inválidos
     * sea rechazada mediante Jakarta Validation.
     * <p>
     * El manejador global debe responder HTTP 400 utilizando
     * el formato común ApiError, sin registrar al usuario.
     */
    @Test
    void debeResponder400ConDatosDeRegistroInvalidos() throws Exception {

        // Solicitud con email inválido, nombre vacío,
        // contraseña vacía y sin roles seleccionados.
        String request = """
                {
                  "email": "correo-invalido",
                  "nombre": "   ",
                  "contrasena": "",
                  "roles": []
                }
                """;

        // Las validaciones del DTO deben rechazar la solicitud.
        mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/auth/registro"
                ));

        // Comprueba que no se haya creado una cuenta.
        assertFalse(
                usuarioRepository.existsByEmailIgnoreCase("correo-invalido")
        );
    }


    /**
     * Verifica que un usuario registrado pueda iniciar sesión
     * con sus credenciales y recibir un JWT válido.
     * <p>
     * Comprueba la integración entre el controlador,
     * AuthService, BCrypt, MySQL y JwtService.
     */
    @Test
    void debeIniciarSesionYDevolverJwt() throws Exception {

        // Primero registra una cuenta para utilizarla en el login.
        String registro = """
                {
                  "email": "login31@ejemplo.com",
                  "nombre": "Usuario Login",
                  "contrasena": "Clave123!",
                  "roles": ["ORGANIZADOR", "EMPRENDEDOR"]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registro))
                .andExpect(status().isCreated());

        // Inicia sesión utilizando el email con otra capitalización.
        // La contraseña debe coincidir con el hash BCrypt almacenado.
        String login = """
                {
                  "email": "LOGIN31@EJEMPLO.COM",
                  "contrasena": "Clave123!"
                }
                """;

        var resultado = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Recupera y valida el JWT generado durante el login.
        JsonNode respuesta = jsonMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        Jwt jwt = jwtService.validarToken(
                respuesta.path("token").asString()
        );

        // Busca al usuario persistido para comprobar su identidad.
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase("login31@ejemplo.com")
                .orElseThrow();

        // El token debe identificar al usuario que inició sesión.
        assertEquals(
                usuario.getId().toString(),
                jwt.getSubject()
        );

        // Verifica que el JWT conserve ambos roles.
        assertEquals(
                Set.of("ORGANIZADOR", "EMPRENDEDOR"),
                Set.copyOf(jwt.getClaimAsStringList("roles"))
        );
    }


    /**
     * Verifica que una contraseña incorrecta impida
     * iniciar sesión y genere una respuesta HTTP 401.
     * <p>
     * El error debe utilizar el formato común ApiError,
     * sin revelar información sobre las credenciales.
     */
    @Test
    void debeResponder401ConContrasenaIncorrecta() throws Exception {

        // Registra una cuenta con credenciales válidas.
        String registro = """
                {
                  "email": "loginincorrecto31@ejemplo.com",
                  "nombre": "Usuario de prueba",
                  "contrasena": "Clave123!",
                  "roles": ["ORGANIZADOR"]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registro))
                .andExpect(status().isCreated());

        // Intenta iniciar sesión con una contraseña incorrecta.
        String login = """
                {
                  "email": "loginincorrecto31@ejemplo.com",
                  "contrasena": "ClaveEquivocada"
                }
                """;

        // Verifica el estado HTTP y la estructura de ApiError.
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(login))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(
                        "Credenciales incorrectas"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/auth/login"
                ))
                .andExpect(jsonPath("$.token").doesNotExist());
    }


    /**
     * Verifica que el login con un email inexistente
     * devuelva el mismo error que una contraseña incorrecta.
     * <p>
     * De esta manera, la API no revela si una cuenta
     * está registrada en la plataforma.
     */
    @Test
    void debeResponder401ConEmailInexistente() throws Exception {

        // Intenta iniciar sesión con un correo no registrado.
        String login = """
                {
                  "email": "inexistente31@ejemplo.com",
                  "contrasena": "Clave123!"
                }
                """;

        // Comprueba que el manejador global devuelva
        // el mismo error genérico de autenticación.
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(login))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(
                        "Credenciales incorrectas"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/auth/login"
                ))
                .andExpect(jsonPath("$.token").doesNotExist());

        // Confirma que el email utilizado no pertenece
        // a ninguna cuenta registrada.
        assertFalse(
                usuarioRepository.existsByEmailIgnoreCase(
                        "inexistente31@ejemplo.com"
                )
        );
    }


    /**
     * Verifica que el endpoint /auth/me devuelva los datos
     * correspondientes al usuario identificado por su JWT.
     * <p>
     * El ID se obtiene del token validado por Spring Security,
     * sin recibir identificadores desde el cliente.
     */
    @Test
    void debeObtenerDatosDelUsuarioAutenticado() throws Exception {

        // Registra una cuenta y obtiene su JWT.
        String registro = """
                {
                  "email": "usuarioactual31@ejemplo.com",
                  "nombre": "Usuario Autenticado",
                  "contrasena": "Clave123!",
                  "roles": ["ORGANIZADOR", "EMPRENDEDOR"]
                }
                """;

        var resultadoRegistro = mockMvc.perform(
                        post("/api/v1/auth/registro")
                                .contextPath("/api/v1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registro))
                .andExpect(status().isCreated())
                .andReturn();

        // Extrae el token generado durante el registro.
        JsonNode respuesta = jsonMapper.readTree(
                resultadoRegistro.getResponse().getContentAsString()
        );

        String token = respuesta.path("token").asString();

        // Consulta /auth/me utilizando el JWT recibido.
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .contextPath("/api/v1")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre")
                        .value("Usuario Autenticado"))
                .andExpect(jsonPath("$.email")
                        .value("usuarioactual31@ejemplo.com"))

                // Comprueba ambos roles sin depender de su orden.
                .andExpect(jsonPath("$.roles")
                        .value(containsInAnyOrder(
                                "ORGANIZADOR",
                                "EMPRENDEDOR"
                        )))

                // No deben exponerse datos sensibles o internos.
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

}
