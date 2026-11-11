package com.feriaconecta.backend.feria.service;

import com.feriaconecta.backend.feria.client.GeoRefClient;
import com.feriaconecta.backend.feria.dto.DireccionNormalizadaResponse;
import com.feriaconecta.backend.feria.dto.GeoRefApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de normalización de direcciones.
 *
 * Utiliza un cliente GeoRef simulado para verificar la transformación
 * de resultados y el comportamiento ante búsquedas sin coincidencias
 * o fallos de comunicación con la API externa.
 */
@ExtendWith(MockitoExtension.class)
class GeoRefServiceTest {

    @Mock
    private GeoRefClient geoRefClient;

    @InjectMocks
    private GeoRefService geoRefService;

    /**
     * Verifica que una dirección reconocida por GeoRef
     * se transforme correctamente al DTO de FeriaConecta.
     */
    @Test
    void debeDevolverDireccionNormalizada() {

        GeoRefApiResponse.Direccion direccion =
                new GeoRefApiResponse.Direccion(
                        new GeoRefApiResponse.Nombre("Buenos Aires"),
                        new GeoRefApiResponse.Nombre("Lanús"),
                        new GeoRefApiResponse.Nombre("San Martín"),
                        new GeoRefApiResponse.Altura(1234),
                        new GeoRefApiResponse.Ubicacion(
                                new BigDecimal("-34.70"),
                                new BigDecimal("-58.40"))
                );

        when(geoRefClient.buscarDirecciones(
                "San Martín 1234", "Buenos Aires"))
                .thenReturn(new GeoRefApiResponse(
                        1, List.of(direccion)));

        List<DireccionNormalizadaResponse> resultado =
                geoRefService.buscarSugerencias(
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "San Martín",
                        "1234");

        assertEquals(1, resultado.size());

        DireccionNormalizadaResponse sugerencia = resultado.get(0);

        assertTrue(sugerencia.normalizada());
        assertEquals("Buenos Aires", sugerencia.provincia());
        assertEquals("Lanús", sugerencia.municipio());
        assertEquals("Lanús", sugerencia.localidad());
        assertEquals("San Martín", sugerencia.calle());
        assertEquals("1234", sugerencia.altura());
        assertEquals(new BigDecimal("-34.70"), sugerencia.latitud());
        assertEquals(new BigDecimal("-58.40"), sugerencia.longitud());
        assertNull(sugerencia.aviso());
    }

    /**
     * Verifica que se conserven los datos originales cuando
     * GeoRef no encuentra direcciones coincidentes.
     */
    @Test
    void debeConservarDatosCuandoNoHayResultados() {

        when(geoRefClient.buscarDirecciones(
                "Calle Inexistente 999", "Buenos Aires"))
                .thenReturn(new GeoRefApiResponse(0, List.of()));

        List<DireccionNormalizadaResponse> resultado =
                geoRefService.buscarSugerencias(
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "Calle Inexistente",
                        "999");

        assertEquals(1, resultado.size());

        DireccionNormalizadaResponse sugerencia = resultado.get(0);

        assertFalse(sugerencia.normalizada());
        assertEquals("Buenos Aires", sugerencia.provincia());
        assertEquals("Lanús", sugerencia.municipio());
        assertEquals("Lanús Oeste", sugerencia.localidad());
        assertEquals("Calle Inexistente", sugerencia.calle());
        assertEquals("999", sugerencia.altura());
        assertNull(sugerencia.latitud());
        assertNull(sugerencia.longitud());
        assertNotNull(sugerencia.aviso());
    }

    /**
     * Verifica que un fallo de comunicación con GeoRef
     * produzca una respuesta no normalizada sin lanzar errores.
     */
    @Test
    void debeAplicarFallbackCuandoGeoRefFalla() {

        when(geoRefClient.buscarDirecciones(
                "San Martín 1234", "Buenos Aires"))
                .thenThrow(new ResourceAccessException(
                        "Timeout simulado"));

        List<DireccionNormalizadaResponse> resultado =
                geoRefService.buscarSugerencias(
                        "Buenos Aires",
                        "Lanús",
                        "Lanús Oeste",
                        "San Martín",
                        "1234");

        assertEquals(1, resultado.size());

        DireccionNormalizadaResponse sugerencia = resultado.get(0);

        assertFalse(sugerencia.normalizada());
        assertEquals("San Martín", sugerencia.calle());
        assertEquals("1234", sugerencia.altura());
        assertNull(sugerencia.latitud());
        assertNull(sugerencia.longitud());
        assertNotNull(sugerencia.aviso());
    }
}
