package com.feriaconecta.backend.auth.service;

import com.feriaconecta.backend.auth.dto.AuthResponse;
import com.feriaconecta.backend.auth.dto.CurrentUserResponse;
import com.feriaconecta.backend.auth.dto.LoginRequest;
import com.feriaconecta.backend.auth.dto.RegistroRequest;
import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.repository.UsuarioRepository;
import com.feriaconecta.backend.common.exception.ConflictException;
import com.feriaconecta.backend.common.exception.UnauthorizedException;
import com.feriaconecta.backend.config.security.CurrentUser;
import com.feriaconecta.backend.config.security.JwtService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

/**
 * Servicio encargado de la autenticación y el registro de usuarios.
 *
 * Centraliza las operaciones de creación de cuentas y, posteriormente,
 * la validación de credenciales y generación de tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final CurrentUser currentUser;

    /**
     * Registra un nuevo usuario con los datos recibidos.
     *
     * El email se normaliza para evitar diferencias entre
     * mayúsculas y minúsculas, y la contraseña se almacena
     * únicamente después de aplicar BCrypt.
     *
     * La solicitud debe haber sido validada mediante @Valid
     * en el controlador.
     *
     * @param request datos de registro del usuario
     * @return respuesta con el JWT para iniciar sesión
     * @throws ConflictException si el email ya está registrado
     */

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {

        //normaliza el correo antes de consultarlo y guardarlo
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        //se verifica que el correo no este registrado anteriormente
        if(usuarioRepository.existsByEmailIgnoreCase(email)){
            throw new ConflictException("El correo electrónico ya está registrado");
        }

        //se construye el usuario con contraseña hasheada
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .email(email)
                .contrasenaHash(passwordEncoder.encode(request.contrasena()))
                .roles(request.roles())
                .build();

        //guarda el usuario y devuelve su id
        Usuario newUser = usuarioRepository.save(usuario);

        //genera el JWT con id y roles
        //los roles se convierten del enum a String
        String token = jwtService.generarToken(
                newUser.getId(),
                newUser.getRoles().stream()
                        .map(Enum::name)
                        .toList()
                );

        return new AuthResponse(token, "Bearer");
    }

    /**
     * Autentica a un usuario mediante email y contraseña.
     *
     * Busca la cuenta por email y compara la contraseña recibida
     * con el hash BCrypt almacenado en la base de datos.
     *
     * Si las credenciales son válidas, genera un JWT con el
     * identificador y los roles del usuario.
     *
     * @param request credenciales ingresadas por el usuario
     * @return respuesta con el token JWT y el tipo Bearer
     * @throws UnauthorizedException si las credenciales son incorrectas
     */

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        //normaliza el correo
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        //se verifica si el usuario esta registrado sin informarlo
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(()-> new UnauthorizedException("Credenciales incorrectas"));

        //compara la contraseña ingresada con el hash guardado
        if(!passwordEncoder.matches(request.contrasena(), usuario.getContrasenaHash())){
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        //genera un Jwt con el id y role del usuario autenticado
        String token = jwtService.generarToken(
                usuario.getId(),
                usuario.getRoles().stream()
                        .map(Enum::name)
                        .toList()
        );
        return new AuthResponse(token, "Bearer");
    }


    /**
     * Obtiene los datos del usuario autenticado.
     *
     * El identificador se recupera del JWT validado
     * por Spring Security, evitando que el usuario
     * pueda consultar otras cuentas mediante un ID.
     *
     * @return nombre, email y roles del usuario autenticado
     * @throws UnauthorizedException si la cuenta ya no existe
     */
    @Transactional(readOnly = true)
    public CurrentUserResponse obtenerUsuarioActual() {

        // Recupera la identidad del contexto de seguridad.
        Integer usuarioId = currentUser.getId();

        // Busca al usuario identificado por el JWT.
        Usuario usuario = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new UnauthorizedException(
                        "Usuario no autenticado"
                ));

        // Copia los roles mientras la transacción está activa,
        // evitando problemas con la carga LAZY de Hibernate.
        return new CurrentUserResponse(
                usuario.getNombre(),
                usuario.getEmail(),
                Set.copyOf(usuario.getRoles())
        );
    }

}
