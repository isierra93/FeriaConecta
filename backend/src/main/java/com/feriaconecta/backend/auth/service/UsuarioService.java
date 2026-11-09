package com.feriaconecta.backend.auth.service;

import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio público del módulo de usuarios.
 *
 * Centraliza el acceso a los usuarios registrados para
 * que los demás módulos no dependan directamente
 * del repositorio de autenticación.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return usuario encontrado o un Optional vacío
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por email sin distinguir
     * entre mayúsculas y minúsculas.
     *
     * Se utilizará durante el inicio de sesión.
     *
     * @param email correo electrónico del usuario
     * @return usuario encontrado o un Optional vacío
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Verifica si un correo electrónico ya está registrado.
     *
     * Se utilizará para detectar posibles duplicados
     * antes de intentar crear una nueva cuenta.
     *
     * @param email correo electrónico a comprobar
     * @return true si existe una cuenta con ese email
     */
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }
}
