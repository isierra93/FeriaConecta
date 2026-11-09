package com.feriaconecta.backend.auth.repository;

import com.feriaconecta.backend.auth.model.Rol;
import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.config.MySqlTestcontainersConfig;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica la persistencia de usuarios y sus roles en MySQL.
 * <p>
 * Utiliza Testcontainers con el esquema SQL original del proyecto
 * y Hibernate configurado para validar las tablas existentes.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
@Transactional
class UsuarioRepositoryIntegracionTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Comprueba que un usuario pueda almacenarse y recuperarse
     * con ambos roles utilizando las tablas usuario y usuario_rol.
     */
    @Test
    void debeGuardarYRecuperarUsuarioConAmbosRoles() {

        // Construye un usuario de prueba con los dos roles permitidos.
        Usuario usuario = Usuario.builder()
                .email("prueba31@ejemplo.com")
                .nombre("Usuario de prueba")
                .contrasenaHash("hash-de-prueba")
                .roles(Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR))
                .build();

        // Guarda el usuario y ejecuta las operaciones SQL pendientes.
        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        assertNotNull(guardado.getId());

        // Limpia el contexto de persistencia para obligar
        // a Hibernate a recuperar los datos desde MySQL.
        entityManager.clear();

        // Busca el usuario sin distinguir mayúsculas y minúsculas.
        Usuario recuperado = usuarioRepository
                .findByEmailIgnoreCase("PRUEBA31@EJEMPLO.COM")
                .orElseThrow();

        // Verifica los datos del usuario y los roles persistidos.
        assertEquals("Usuario de prueba", recuperado.getNombre());
        assertEquals(
                Set.of(Rol.ORGANIZADOR, Rol.EMPRENDEDOR),
                recuperado.getRoles()
        );

        assertTrue(
                usuarioRepository.existsByEmailIgnoreCase(
                        "PRUEBA31@EJEMPLO.COM"
                )
        );
    }
}
