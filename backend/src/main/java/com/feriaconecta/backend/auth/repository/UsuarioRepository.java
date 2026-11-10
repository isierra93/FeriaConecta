package com.feriaconecta.backend.auth.repository;

import com.feriaconecta.backend.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a los usuarios registrados.
 *
 * Spring Data JPA proporciona las operaciones básicas
 * de persistencia y genera las consultas declaradas.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su correo electrónico,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param email correo electrónico a buscar
     * @return usuario encontrado o un Optional vacío
     */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /**
     * Verifica si ya existe un usuario registrado
     * con el correo electrónico indicado.
     *
     * @param email correo electrónico a verificar
     * @return true si el correo ya está registrado
     */
    boolean existsByEmailIgnoreCase(String email);
}
