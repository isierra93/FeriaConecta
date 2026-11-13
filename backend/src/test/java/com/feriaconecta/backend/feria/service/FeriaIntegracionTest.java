package com.feriaconecta.backend.feria.service;

import com.feriaconecta.backend.auth.model.Rol;
import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.repository.UsuarioRepository;
import com.feriaconecta.backend.common.model.Categoria;
import com.feriaconecta.backend.config.MySqlTestcontainersConfig;
import com.feriaconecta.backend.config.security.CurrentUser;
import com.feriaconecta.backend.feria.dto.CambiarEstadoRequest;
import com.feriaconecta.backend.feria.dto.FeriaRequest;
import com.feriaconecta.backend.feria.dto.FeriaResponse;
import com.feriaconecta.backend.feria.dto.UbicacionRequest;
import com.feriaconecta.backend.feria.model.EstadoFeria;
import com.feriaconecta.backend.feria.model.Feria;
import com.feriaconecta.backend.feria.model.Ubicacion;
import com.feriaconecta.backend.feria.repository.FeriaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integración de ferias con MySQL Testcontainers.
 *
 * <p>Usa el esquema SQL del proyecto para comprobar restricciones,
 * relaciones y cambios persistidos. Cada prueba revierte su transacción.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
class FeriaIntegracionTest {

    @Autowired
    private FeriaService feriaService;

    @Autowired
    private FeriaRepository feriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private CurrentUser currentUser;

    /**
     * Verifica la creación real de Feria y Ubicacion con clave primaria compartida.
     */
    @Test
    @Transactional
    void debePersistirFeriaYUbicacionConMismoId() {
        Usuario organizador = crearOrganizador("organizador-integracion@ejemplo.com");
        when(currentUser.getId()).thenReturn(organizador.getId());

        FeriaRequest request = new FeriaRequest(
                "Feria de Integración",
                LocalDate.now().plusDays(30),
                Categoria.ARTESANIAS,
                new BigDecimal("15000.00"),
                new UbicacionRequest(
                        "Buenos Aires", "Lanús", "Lanús Oeste", "San Martín", "1234",
                        new BigDecimal("-34.70000000"),
                        new BigDecimal("-58.40000000"))
        );

        FeriaResponse respuesta = feriaService.crear(request);
        assertNotNull(respuesta.id());
        assertEquals(EstadoFeria.BORRADOR, respuesta.estado());

        // Vuelve a consultar desde MySQL, no desde las entidades en memoria.
        entityManager.flush();
        entityManager.clear();

        Feria feriaGuardada = feriaRepository.findById(respuesta.id()).orElseThrow();
        Ubicacion ubicacionGuardada = feriaGuardada.getUbicacion();

        assertEquals("Feria de Integración", feriaGuardada.getNombre());
        assertEquals(organizador.getId(), feriaGuardada.getOrganizador().getId());
        assertEquals(feriaGuardada.getId(), ubicacionGuardada.getFeriaId());
        assertEquals(feriaGuardada.getId(), ubicacionGuardada.getFeria().getId());
        assertEquals("San Martín", ubicacionGuardada.getCalle());
        assertEquals(new BigDecimal("-34.70000000"), ubicacionGuardada.getLatitud());
        assertEquals(new BigDecimal("-58.40000000"), ubicacionGuardada.getLongitud());
    }

    /**
     * Verifica que MySQL aplique la restricción UNIQUE de organizador, nombre y fecha.
     */
    @Test
    @Transactional
    void debeRechazarFeriaDuplicadaEnMySql() {
        Usuario organizador = crearOrganizador("organizador-duplicados@ejemplo.com");
        LocalDate fecha = LocalDate.now().plusDays(30);

        feriaRepository.saveAndFlush(crearFeriaDePrueba(organizador, fecha));
        Feria duplicada = crearFeriaDePrueba(organizador, fecha);

        assertThrows(DataIntegrityViolationException.class,
                () -> feriaRepository.saveAndFlush(duplicada));
    }

    /**
     * Verifica que el repositorio liste solo ferias propias, ordenadas por fecha.
     */
    @Test
    @Transactional
    void debeListarSoloFeriasDelOrganizadorOrdenadasPorFecha() {
        Usuario organizadorA = crearOrganizador("organizador-a@ejemplo.com");
        Usuario organizadorB = crearOrganizador("organizador-b@ejemplo.com");
        LocalDate fecha = LocalDate.now();

        Feria primera = feriaRepository.saveAndFlush(
                crearFeriaDePrueba(organizadorA, fecha.plusDays(10)));
        Feria segunda = feriaRepository.saveAndFlush(
                crearFeriaDePrueba(organizadorA, fecha.plusDays(30)));
        feriaRepository.saveAndFlush(
                crearFeriaDePrueba(organizadorB, fecha.plusDays(20)));

        entityManager.flush();
        entityManager.clear();
        when(currentUser.getId()).thenReturn(organizadorA.getId());

        List<FeriaResponse> respuestas = feriaService.listarMisFerias();

        assertEquals(2, respuestas.size());
        assertEquals(segunda.getId(), respuestas.get(0).id());
        assertEquals(primera.getId(), respuestas.get(1).id());
    }

    /**
     * Verifica que la edición persista sin reemplazar la ubicación existente.
     */
    @Test
    @Transactional
    void debeActualizarFeriaYUbicacionEnMySql() {
        Usuario organizador = crearOrganizador("organizador-edicion@ejemplo.com");
        Feria feria = feriaRepository.saveAndFlush(
                crearFeriaDePrueba(organizador, LocalDate.now().plusDays(10)));
        Integer feriaId = feria.getId();

        entityManager.flush();
        entityManager.clear();
        when(currentUser.getId()).thenReturn(organizador.getId());

        FeriaRequest request = new FeriaRequest(
                "Feria Actualizada",
                LocalDate.now().plusDays(20),
                Categoria.ARTESANIAS,
                new BigDecimal("25000.00"),
                new UbicacionRequest(
                        "Buenos Aires", "Lanús", "Lanús Oeste", "Avenida de Mayo", "500",
                        new BigDecimal("-34.70000000"),
                        new BigDecimal("-58.40000000"))
        );

        FeriaResponse respuesta = feriaService.editar(feriaId, request);
        assertEquals(feriaId, respuesta.id());

        // Comprueba los cambios guardados mediante dirty checking.
        entityManager.flush();
        entityManager.clear();

        Feria actualizada = feriaRepository.findById(feriaId).orElseThrow();
        Ubicacion ubicacion = actualizada.getUbicacion();

        assertEquals("Feria Actualizada", actualizada.getNombre());
        assertEquals(request.fecha(), actualizada.getFecha());
        assertEquals(new BigDecimal("25000.00"), actualizada.getCostoParticipacion());
        assertEquals(EstadoFeria.BORRADOR, actualizada.getEstado());
        assertEquals("Avenida de Mayo", ubicacion.getCalle());
        assertEquals("500", ubicacion.getAltura());
        assertEquals(new BigDecimal("-34.70000000"), ubicacion.getLatitud());
        assertEquals(new BigDecimal("-58.40000000"), ubicacion.getLongitud());
        assertEquals(feriaId, ubicacion.getFeriaId());
        assertEquals(feriaId, ubicacion.getFeria().getId());
    }

    /**
     * Verifica que el cambio de estado persista sin alterar la relación con Ubicacion.
     */
    @Test
    @Transactional
    void debePersistirCambioDeEstadoEnMySql() {
        Usuario organizador = crearOrganizador("organizador-estados@ejemplo.com");
        Feria feria = feriaRepository.saveAndFlush(
                crearFeriaDePrueba(organizador, LocalDate.now().plusDays(30)));
        Integer feriaId = feria.getId();
        assertEquals(EstadoFeria.BORRADOR, feria.getEstado());

        entityManager.flush();
        entityManager.clear();
        when(currentUser.getId()).thenReturn(organizador.getId());

        FeriaResponse respuesta = feriaService.cambiarEstado(
                feriaId, new CambiarEstadoRequest(EstadoFeria.CANCELADA));
        assertEquals(EstadoFeria.CANCELADA, respuesta.estado());

        entityManager.flush();
        entityManager.clear();

        Feria actualizada = feriaRepository.findById(feriaId).orElseThrow();
        assertEquals(EstadoFeria.CANCELADA, actualizada.getEstado());
        assertEquals(feriaId, actualizada.getUbicacion().getFeriaId());
    }

    // Datos comunes de prueba

    /**
     * Registra un organizador real en MySQL para cada prueba.
     */
    private Usuario crearOrganizador(String email) {
        return usuarioRepository.saveAndFlush(Usuario.builder()
                .email(email)
                .nombre("Organizador de prueba")
                .contrasenaHash("hash-de-prueba")
                .roles(Set.of(Rol.ORGANIZADOR))
                .build());
    }

    /**
     * Crea una feria con ubicación para las pruebas de persistencia.
     */
    private Feria crearFeriaDePrueba(Usuario organizador, LocalDate fecha) {
        Feria feria = Feria.builder()
                .organizador(organizador)
                .nombre("Feria de prueba")
                .fecha(fecha)
                .categoriaPermitida(Categoria.ARTESANIAS)
                .costoParticipacion(new BigDecimal("15000.00"))
                .build();

        Ubicacion ubicacion = Ubicacion.builder()
                .provincia("Buenos Aires")
                .municipio("Lanús")
                .localidad("Lanús Oeste")
                .calle("San Martín")
                .altura("1234")
                .build();

        feria.asignarUbicacion(ubicacion);
        return feria;
    }
}
