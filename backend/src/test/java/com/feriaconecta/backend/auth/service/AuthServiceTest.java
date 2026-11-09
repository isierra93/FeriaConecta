package com.feriaconecta.backend.auth.service;

import com.feriaconecta.backend.auth.dto.AuthResponse;
import com.feriaconecta.backend.auth.dto.CurrentUserResponse;
import com.feriaconecta.backend.auth.dto.LoginRequest;
import com.feriaconecta.backend.auth.dto.RegistroRequest;
import com.feriaconecta.backend.auth.model.Rol;
import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.repository.UsuarioRepository;
import com.feriaconecta.backend.common.exception.ConflictException;
import com.feriaconecta.backend.common.exception.UnauthorizedException;
import com.feriaconecta.backend.config.security.CurrentUser;
import com.feriaconecta.backend.config.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del registro de usuarios.
 * <p>
 * Utiliza Mockito para simular el repositorio, el encoder
 * de contraseñas y la generación de JWT, sin acceder a MySQL.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CurrentUser currentUser;

    /**
     * Verifica que el registro guarde los datos procesados
     * y devuelva un JWT con el ID y los roles del usuario.
     */
    @Test
    void debeRegistrarUsuarioYGenerarToken() {

        Set<Rol> roles = Set.of(
                Rol.ORGANIZADOR,
                Rol.EMPRENDEDOR
        );

        RegistroRequest request = new RegistroRequest(
                "  Juan@Ejemplo.com  ",
                "  Juan Pérez  ",
                "Clave123!",
                roles
        );

        // Simula el resultado de BCrypt sin aplicar el algoritmo real.
        when(passwordEncoder.encode("Clave123!"))
                .thenReturn("hash-de-prueba");

        // Simula la entidad recuperada después de guardarse.
        Usuario guardado = mock(Usuario.class);
        when(guardado.getId()).thenReturn(15);
        when(guardado.getRoles()).thenReturn(roles);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(guardado);

        when(jwtService.generarToken(eq(15), anyCollection()))
                .thenReturn("jwt-de-prueba");

        // Ejecuta la operación que estamos probando.
        AuthResponse respuesta = authService.registrar(request);

        // Verifica la respuesta de autenticación.
        assertEquals("jwt-de-prueba", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());

        // Captura el usuario enviado al repositorio
        // para comprobar que sus datos fueron procesados.
        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuario = captor.getValue();

        assertEquals("juan@ejemplo.com", usuario.getEmail());
        assertEquals("Juan Pérez", usuario.getNombre());
        assertEquals("hash-de-prueba", usuario.getContrasenaHash());
        assertEquals(roles, usuario.getRoles());

        // El JWT debe contener el ID y ambos roles.
        verify(jwtService).generarToken(
                eq(15),
                argThat(valores ->
                        valores.size() == 2
                                && valores.containsAll(
                                Set.of("ORGANIZADOR", "EMPRENDEDOR")
                        )
                )
        );
    }

    /**
     * Verifica que un email registrado provoque ConflictException
     * y detenga el proceso antes de guardar o generar el JWT.
     */
    @Test
    void debeRechazarEmailDuplicado() {

        RegistroRequest request = new RegistroRequest(
                "Repetido@Ejemplo.com",
                "Usuario de prueba",
                "Clave123!",
                Set.of(Rol.ORGANIZADOR)
        );

        when(usuarioRepository.existsByEmailIgnoreCase(
                "repetido@ejemplo.com"
        )).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.registrar(request)
        );

        // No debe guardar ni generar credenciales.
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }


    /**
     * Verifica que un usuario con credenciales correctas
     * reciba un JWT con su identificador y sus roles.
     */
    @Test
    void debeIniciarSesionConCredencialesCorrectas() {

        LoginRequest request = new LoginRequest(
                "  JUAN@EJEMPLO.COM  ",
                "Clave123!"
        );

        // Simula un usuario registrado con ambos roles.
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(15);
        when(usuario.getContrasenaHash()).thenReturn("hash-de-prueba");
        when(usuario.getRoles()).thenReturn(
                Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR)
        );

        when(usuarioRepository.findByEmailIgnoreCase("juan@ejemplo.com"))
                .thenReturn(Optional.of(usuario));

        // Simula una contraseña que coincide con el hash almacenado.
        when(passwordEncoder.matches("Clave123!", "hash-de-prueba"))
                .thenReturn(true);

        when(jwtService.generarToken(eq(15), anyCollection()))
                .thenReturn("jwt-de-prueba");

        // Ejecuta el inicio de sesión.
        AuthResponse respuesta = authService.login(request);

        assertEquals("jwt-de-prueba", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());

        // Verifica que el JWT se genere con ambos roles.
        verify(jwtService).generarToken(
                eq(15),
                argThat(roles ->
                        roles.size() == 2
                                && roles.containsAll(
                                Set.of("ORGANIZADOR", "EMPRENDEDOR")
                        )
                )
        );
    }

    /**
     * Comprueba que una contraseña incorrecta impida
     * generar un token y produzca un error de autenticación.
     */
    @Test
    void debeRechazarContrasenaIncorrecta() {

        LoginRequest request = new LoginRequest(
                "juan@ejemplo.com",
                "ClaveIncorrecta"
        );

        Usuario usuario = mock(Usuario.class);
        when(usuario.getContrasenaHash()).thenReturn("hash-de-prueba");

        when(usuarioRepository.findByEmailIgnoreCase("juan@ejemplo.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "ClaveIncorrecta", "hash-de-prueba"
        )).thenReturn(false);

        UnauthorizedException excepcion = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales incorrectas", excepcion.getMessage());

        // No debe generar un JWT si la contraseña es incorrecta.
        verifyNoInteractions(jwtService);
    }

    /**
     * Comprueba que un email inexistente produzca el mismo
     * mensaje que una contraseña incorrecta.
     * <p>
     * De esta manera no se revela si una cuenta está registrada.
     */
    @Test
    void debeRechazarEmailNoRegistrado() {

        LoginRequest request = new LoginRequest(
                "inexistente@ejemplo.com",
                "Clave123!"
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "inexistente@ejemplo.com"
        )).thenReturn(Optional.empty());

        UnauthorizedException excepcion = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales incorrectas", excepcion.getMessage());

        // No verifica contraseñas ni genera tokens para cuentas inexistentes.
        verifyNoInteractions(passwordEncoder, jwtService);
    }


    /**
     * Verifica que se recuperen los datos del usuario autenticado.
     *
     * El identificador utilizado para consultar la cuenta
     * debe provenir de CurrentUser, que obtiene la identidad
     * desde el contexto de seguridad.
     */
    @Test
    void debeObtenerDatosDelUsuarioAutenticado() {

        // Simula que el JWT identifica al usuario con ID 15.
        when(currentUser.getId()).thenReturn(15);

        // Simula los datos de ese usuario almacenado.
        Usuario usuario = mock(Usuario.class);

        when(usuario.getNombre()).thenReturn("Juan Pérez");
        when(usuario.getEmail()).thenReturn("juan@ejemplo.com");
        when(usuario.getRoles()).thenReturn(
                Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR)
        );

        when(usuarioService.buscarPorId(15))
                .thenReturn(Optional.of(usuario));

        // Ejecuta la consulta del usuario autenticado.
        CurrentUserResponse respuesta = authService.obtenerUsuarioActual();

        // Verifica que se devuelvan únicamente los datos esperados.
        assertEquals("Juan Pérez", respuesta.nombre());
        assertEquals("juan@ejemplo.com", respuesta.email());
        assertEquals(
                Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR),
                respuesta.roles()
        );

        // Confirma que se consultó el ID obtenido del JWT.
        verify(currentUser).getId();
        verify(usuarioService).buscarPorId(15);
    }

    /**
     * Comprueba que no se devuelvan datos cuando el usuario
     * identificado por el JWT ya no existe en la base de datos.
     */
    @Test
    void debeRechazarUsuarioAutenticadoInexistente() {

        // Simula un JWT cuyo usuario ya no existe.
        when(currentUser.getId()).thenReturn(15);

        when(usuarioService.buscarPorId(15))
                .thenReturn(Optional.empty());

        // El servicio debe rechazar la solicitud.
        UnauthorizedException excepcion = assertThrows(
                UnauthorizedException.class,
                () -> authService.obtenerUsuarioActual()
        );

        assertEquals(
                "Usuario no autenticado",
                excepcion.getMessage()
        );

        verify(usuarioService).buscarPorId(15);
    }
}
