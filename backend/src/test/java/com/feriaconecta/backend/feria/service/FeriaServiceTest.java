package com.feriaconecta.backend.feria.service;

import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.service.UsuarioService;
import com.feriaconecta.backend.common.exception.BusinessRuleException;
import com.feriaconecta.backend.common.exception.ConflictException;
import com.feriaconecta.backend.common.exception.ResourceNotFoundException;
import com.feriaconecta.backend.common.model.Categoria;
import com.feriaconecta.backend.config.security.CurrentUser;
import com.feriaconecta.backend.feria.dto.CambiarEstadoRequest;
import com.feriaconecta.backend.feria.dto.FeriaRequest;
import com.feriaconecta.backend.feria.dto.FeriaResponse;
import com.feriaconecta.backend.feria.dto.UbicacionRequest;
import com.feriaconecta.backend.feria.model.EstadoFeria;
import com.feriaconecta.backend.feria.model.Feria;
import com.feriaconecta.backend.feria.model.Ubicacion;
import com.feriaconecta.backend.feria.repository.FeriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de las reglas de negocio de ferias.
 *
 * <p>Utiliza Mockito para comprobar validaciones y permisos de propietario
 * sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class FeriaServiceTest {

    @Mock
    private FeriaRepository feriaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private FeriaService feriaService;

    // Creación

    /**
     * Verifica el estado inicial, el nombre y las coordenadas de una feria nueva.
     */
    @Test
    void debeCrearFeriaEnBorradorConUbicacion() {
        FeriaRequest request = crearRequest(
                new BigDecimal("-34.70000000"), new BigDecimal("-58.40000000"));

        Usuario organizador = mock(Usuario.class);
        when(currentUser.getId()).thenReturn(15);
        when(usuarioService.buscarPorId(15)).thenReturn(Optional.of(organizador));
        when(feriaRepository.saveAndFlush(any(Feria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        FeriaResponse respuesta = feriaService.crear(request);

        ArgumentCaptor<Feria> captor = ArgumentCaptor.forClass(Feria.class);
        verify(feriaRepository).saveAndFlush(captor.capture());

        Feria feria = captor.getValue();
        assertEquals("Feria Artesanal", feria.getNombre());
        assertEquals(EstadoFeria.BORRADOR, feria.getEstado());
        assertSame(organizador, feria.getOrganizador());
        assertSame(feria, feria.getUbicacion().getFeria());
        assertEquals(new BigDecimal("-34.70000000"), feria.getUbicacion().getLatitud());
        assertEquals(new BigDecimal("-58.40000000"), feria.getUbicacion().getLongitud());
        assertEquals(EstadoFeria.BORRADOR, respuesta.estado());
        assertNull(respuesta.aviso());
    }

    /**
     * Verifica que se admita una dirección manual y se informe un aviso.
     */
    @Test
    void debeGuardarDireccionManualConAviso() {
        prepararCreacion();
        when(feriaRepository.saveAndFlush(any(Feria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        FeriaResponse respuesta = feriaService.crear(crearRequest(null, null));

        assertNull(respuesta.ubicacion().latitud());
        assertNull(respuesta.ubicacion().longitud());
        assertNotNull(respuesta.aviso());
    }

    /**
     * Verifica que no se cree otra feria con el mismo nombre y fecha.
     */
    @Test
    void debeRechazarFeriaDuplicada() {
        prepararCreacion();
        FeriaRequest request = crearRequest(null, null);
        when(feriaRepository.existsByOrganizador_IdAndNombreAndFecha(
                15, "Feria Artesanal", request.fecha())).thenReturn(true);

        assertThrows(ConflictException.class, () -> feriaService.crear(request));
        verify(feriaRepository, never()).saveAndFlush(any(Feria.class));
    }

    /**
     * Verifica que las coordenadas se informen juntas o se omitan ambas.
     */
    @Test
    void debeRechazarCoordenadasIncompletas() {
        prepararCreacion();
        FeriaRequest request = crearRequest(new BigDecimal("-34.70000000"), null);

        assertThrows(BusinessRuleException.class, () -> feriaService.crear(request));
        verify(feriaRepository, never()).saveAndFlush(any(Feria.class));
    }

    // Consulta de detalle
    // El listado y su orden se prueban directamente con MySQL en FeriaIntegracionTest.

    /**
     * Verifica que un organizador consulte una feria propia.
     */
    @Test
    void debeObtenerDetalleDeFeriaPropia() {
        prepararFeria(15, EstadoFeria.BORRADOR);

        FeriaResponse respuesta = feriaService.obtenerDetalle(1);

        assertEquals("Feria Artesanal", respuesta.nombre());
        assertEquals("Lanús", respuesta.ubicacion().municipio());
        assertEquals(EstadoFeria.BORRADOR, respuesta.estado());
    }

    /**
     * Verifica que una feria inexistente no pueda consultarse.
     */
    @Test
    void debeRechazarDetalleDeFeriaInexistente() {
        when(currentUser.getId()).thenReturn(15);
        when(feriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feriaService.obtenerDetalle(99));
    }

    /**
     * Verifica que no se pueda consultar una feria de otro organizador.
     */
    @Test
    void debeRechazarDetalleDeFeriaAjena() {
        prepararFeria(25, EstadoFeria.BORRADOR);

        assertThrows(ResourceNotFoundException.class,
                () -> feriaService.obtenerDetalle(1));
    }

    // Edición

    /**
     * Verifica que una feria en BORRADOR pueda actualizar sus datos y ubicación.
     */
    @Test
    void debeEditarFeriaEnBorrador() {
        Feria feria = prepararFeria(15, EstadoFeria.BORRADOR);
        Ubicacion ubicacionOriginal = feria.getUbicacion();
        FeriaRequest request = crearRequestEdicion(
                "  Feria Actualizada  ", new BigDecimal("12000.00"),
                new BigDecimal("-34.70000000"), new BigDecimal("-58.40000000"));

        FeriaResponse respuesta = feriaService.editar(1, request);

        assertEquals("Feria Actualizada", respuesta.nombre());
        assertEquals(new BigDecimal("12000.00"), respuesta.costoParticipacion());
        assertEquals(request.fecha(), respuesta.fecha());
        assertEquals("Avenida de Mayo", respuesta.ubicacion().calle());
        assertEquals(new BigDecimal("-58.40000000"), respuesta.ubicacion().longitud());
        assertSame(ubicacionOriginal, feria.getUbicacion());
        assertEquals(EstadoFeria.BORRADOR, feria.getEstado());
    }

    /**
     * Verifica que una feria PUBLICADA permita editar datos sin cambiar el costo.
     */
    @Test
    void debeEditarFeriaPublicadaSinCambiarCosto() {
        prepararFeria(15, EstadoFeria.PUBLICADA);
        FeriaRequest request = crearRequestEdicion(
                "Feria Publicada Actualizada", new BigDecimal("15000.0"), null, null);

        FeriaResponse respuesta = feriaService.editar(1, request);

        assertEquals("Feria Publicada Actualizada", respuesta.nombre());
        assertEquals(EstadoFeria.PUBLICADA, respuesta.estado());
        assertEquals("Avenida de Mayo", respuesta.ubicacion().calle());
        assertNotNull(respuesta.aviso());
    }

    /**
     * Verifica que no se pueda cambiar el costo de una feria PUBLICADA.
     */
    @Test
    void debeRechazarCambioDeCostoEnFeriaPublicada() {
        Feria feria = prepararFeria(15, EstadoFeria.PUBLICADA);
        FeriaRequest request = crearRequestEdicion(
                "Feria Actualizada", new BigDecimal("20000.00"), null, null);

        assertThrows(BusinessRuleException.class,
                () -> feriaService.editar(1, request));

        assertEquals("Feria Artesanal", feria.getNombre());
        assertEquals(new BigDecimal("15000.00"), feria.getCostoParticipacion());
    }

    /**
     * Verifica los estados que no admiten edición de datos.
     */
    @ParameterizedTest
    @EnumSource(value = EstadoFeria.class,
            names = {"EN_CURSO", "FINALIZADA", "CANCELADA"})
    void debeRechazarEdicionEnEstadoNoPermitido(EstadoFeria estado) {
        prepararFeria(15, estado);

        assertThrows(BusinessRuleException.class,
                () -> feriaService.editar(1, crearRequest(null, null)));
    }

    /**
     * Verifica que un organizador no pueda editar ferias ajenas.
     */
    @Test
    void debeRechazarEdicionDeFeriaAjena() {
        prepararFeria(25, EstadoFeria.BORRADOR);

        assertThrows(ResourceNotFoundException.class,
                () -> feriaService.editar(1, crearRequest(null, null)));
    }

    /**
     * Verifica el control de duplicados al editar, excluyendo la feria actual.
     */
    @Test
    void debeRechazarDuplicadoAlEditar() {
        Feria feria = prepararFeria(15, EstadoFeria.BORRADOR);
        FeriaRequest request = crearRequest(null, null);
        when(feriaRepository.existsByOrganizador_IdAndNombreAndFechaAndIdNot(
                15, "Feria Artesanal", request.fecha(), 1)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> feriaService.editar(1, request));

        assertEquals("Feria Artesanal", feria.getNombre());
    }

    /**
     * Verifica que no se pueda editar una feria cuya fecha ya pasó.
     */
    @Test
    void debeRechazarEdicionDeFeriaConFechaPasada() {
        Feria feria = prepararFeria(15, EstadoFeria.BORRADOR);
        ReflectionTestUtils.setField(feria, "fecha", LocalDate.now().minusDays(1));

        assertThrows(BusinessRuleException.class,
                () -> feriaService.editar(1, crearRequest(null, null)));
    }

    /**
     * Verifica las coordenadas opcionales también durante la edición.
     */
    @Test
    void debeRechazarCoordenadasIncompletasAlEditar() {
        prepararFeria(15, EstadoFeria.BORRADOR);
        FeriaRequest request = crearRequestEdicion(
                "Feria Actualizada", new BigDecimal("12000.00"),
                new BigDecimal("-34.70000000"), null);

        assertThrows(BusinessRuleException.class,
                () -> feriaService.editar(1, request));
    }

    // Cambio de estado

    /**
     * Verifica el avance de estados y las cancelaciones permitidas.
     */
    @ParameterizedTest
    @CsvSource({
            "BORRADOR, CANCELADA",
            "PUBLICADA, EN_CURSO",
            "PUBLICADA, CANCELADA",
            "EN_CURSO, FINALIZADA",
            "EN_CURSO, CANCELADA"
    })
    void debeCambiarEstadoCuandoLaTransicionEsValida(
            EstadoFeria estadoActual, EstadoFeria nuevoEstado) {
        Feria feria = prepararFeria(15, estadoActual);

        FeriaResponse respuesta = feriaService.cambiarEstado(
                1, new CambiarEstadoRequest(nuevoEstado));

        assertEquals(nuevoEstado, respuesta.estado());
        assertEquals(nuevoEstado, feria.getEstado());
    }

    /**
     * Verifica que las transiciones fuera del flujo sean rechazadas.
     */
    @ParameterizedTest
    @CsvSource({
            "BORRADOR, FINALIZADA",
            "PUBLICADA, BORRADOR",
            "FINALIZADA, CANCELADA",
            "CANCELADA, PUBLICADA"
    })
    void debeRechazarTransicionNoPermitida(
            EstadoFeria estadoActual, EstadoFeria nuevoEstado) {
        Feria feria = prepararFeria(15, estadoActual);

        assertThrows(BusinessRuleException.class,
                () -> feriaService.cambiarEstado(
                        1, new CambiarEstadoRequest(nuevoEstado)));

        assertEquals(estadoActual, feria.getEstado());
    }

    /**
     * Comprueba el bloqueo provisional de publicación hasta integrar Puestos.
     */
    @Test
    void debeRechazarPublicacionSinValidacionDePuestos() {
        Feria feria = prepararFeria(15, EstadoFeria.BORRADOR);

        assertThrows(BusinessRuleException.class,
                () -> feriaService.cambiarEstado(
                        1, new CambiarEstadoRequest(EstadoFeria.PUBLICADA)));

        assertEquals(EstadoFeria.BORRADOR, feria.getEstado());
    }

    /**
     * Verifica que un organizador no pueda cambiar el estado de una feria ajena.
     */
    @Test
    void debeRechazarCambioDeEstadoDeFeriaAjena() {
        Feria feria = prepararFeria(25, EstadoFeria.BORRADOR);

        assertThrows(ResourceNotFoundException.class,
                () -> feriaService.cambiarEstado(
                        1, new CambiarEstadoRequest(EstadoFeria.CANCELADA)));

        assertEquals(EstadoFeria.BORRADOR, feria.getEstado());
    }

    /**
     * Verifica que no se pueda cambiar el estado de una feria inexistente.
     */
    @Test
    void debeRechazarCambioDeEstadoDeFeriaInexistente() {
        when(currentUser.getId()).thenReturn(15);
        when(feriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feriaService.cambiarEstado(
                        99, new CambiarEstadoRequest(EstadoFeria.CANCELADA)));
    }

    // Datos comunes de prueba

    /**
     * Prepara la autenticación y el organizador para crear una feria.
     */
    private void prepararCreacion() {
        when(currentUser.getId()).thenReturn(15);
        when(usuarioService.buscarPorId(15))
                .thenReturn(Optional.of(mock(Usuario.class)));
    }

    /**
     * Prepara una feria recuperada del repositorio, con propietario y estado indicados.
     */
    private Feria prepararFeria(Integer organizadorId, EstadoFeria estado) {
        when(currentUser.getId()).thenReturn(15);
        Feria feria = crearFeriaParaPrueba(organizadorId);
        ReflectionTestUtils.setField(feria, "estado", estado);
        when(feriaRepository.findById(1)).thenReturn(Optional.of(feria));
        return feria;
    }

    /**
     * Construye una feria con su ubicación.
     */
    private Feria crearFeriaParaPrueba(Integer organizadorId) {
        Usuario organizador = mock(Usuario.class);
        when(organizador.getId()).thenReturn(organizadorId);

        Feria feria = Feria.builder()
                .organizador(organizador)
                .nombre("Feria Artesanal")
                .fecha(LocalDate.now().plusDays(30))
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

    /**
     * Construye los datos estándar de una feria.
     */
    private FeriaRequest crearRequest(BigDecimal latitud, BigDecimal longitud) {
        return new FeriaRequest(
                "  Feria Artesanal  ",
                LocalDate.now().plusDays(30),
                Categoria.ARTESANIAS,
                new BigDecimal("15000.00"),
                new UbicacionRequest(
                        "Buenos Aires", "Lanús", "Lanús Oeste", "San Martín", "1234",
                        latitud, longitud)
        );
    }

    /**
     * Construye datos de edición con una dirección distinta.
     */
    private FeriaRequest crearRequestEdicion(
            String nombre, BigDecimal costo, BigDecimal latitud, BigDecimal longitud) {
        return new FeriaRequest(
                nombre,
                LocalDate.now().plusDays(40),
                Categoria.ARTESANIAS,
                costo,
                new UbicacionRequest(
                        "Buenos Aires", "Lanús", "Lanús Oeste", "Avenida de Mayo", "500",
                        latitud, longitud)
        );
    }
}
