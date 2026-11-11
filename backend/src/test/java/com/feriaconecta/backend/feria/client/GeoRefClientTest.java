package com.feriaconecta.backend.feria.client;

import com.feriaconecta.backend.feria.dto.GeoRefApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas del cliente HTTP utilizado para consultar la API GeoRef.
 * <p>
 * Simula las respuestas del servicio externo para verificar
 * la construcción de solicitudes, la conversión del JSON
 * y el comportamiento ante errores HTTP.
 */
class GeoRefClientTest {

    private MockRestServiceServer mockServer;
    private GeoRefClient geoRefClient;

    /**
     * Configura el cliente y el servidor HTTP simulado
     * antes de ejecutar cada prueba.
     */
    @BeforeEach
    void configurar() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://georef.prueba.local/api/v2.0");

        mockServer = MockRestServiceServer.bindTo(builder).build();

        geoRefClient = new GeoRefClient(builder.build());
    }

    /**
     * Verifica que el cliente construya la solicitud GET
     * y convierta correctamente la respuesta JSON de GeoRef.
     * <p>
     * Incluye campos adicionales para comprobar que los
     * identificadores internos sean ignorados.
     */
    @Test
    void debeConsultarYMapearDireccion() {

        String json = """
                {
                  "cantidad": 1,
                  "total": 1,
                  "direcciones": [
                    {
                      "provincia": {
                        "id": "06",
                        "nombre": "Buenos Aires"
                      },
                      "localidad_censal": {
                        "id": "06001",
                        "nombre": "Lanús"
                      },
                      "calle": {
                        "id": "12345",
                        "nombre": "SAN MARTIN",
                        "categoria": "CALLE"
                      },
                      "altura": {
                        "valor": 1234
                      },
                      "ubicacion": {
                        "lat": -34.70,
                        "lon": -58.40
                      },
                      "nomenclatura": "SAN MARTIN 1234"
                    }
                  ]
                }
                """;

        mockServer.expect(request -> {
                    assertEquals(HttpMethod.GET, request.getMethod());

                    assertEquals(
                            "/api/v2.0/direcciones",
                            request.getURI().getPath());

                    String query = request.getURI().getQuery();

                    assertNotNull(query);
                    assertTrue(query.contains("direccion=San Martin 1234"));
                    assertTrue(query.contains("provincia=Buenos Aires"));
                    assertTrue(query.contains("max=5"));
                })
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        GeoRefApiResponse respuesta =
                geoRefClient.buscarDirecciones(
                        "San Martin 1234", "Buenos Aires");

        assertNotNull(respuesta);
        assertEquals(1, respuesta.cantidad());
        assertEquals(1, respuesta.direcciones().size());

        GeoRefApiResponse.Direccion direccion =
                respuesta.direcciones().get(0);

        assertEquals("Buenos Aires", direccion.provincia().nombre());
        assertEquals("Lanús", direccion.localidadCensal().nombre());
        assertEquals("SAN MARTIN", direccion.calle().nombre());
        assertEquals(1234, direccion.altura().valor());

        assertEquals(0,
                new BigDecimal("-34.70")
                        .compareTo(direccion.ubicacion().lat()));

        assertEquals(0,
                new BigDecimal("-58.40")
                        .compareTo(direccion.ubicacion().lon()));

        mockServer.verify();
    }

    /**
     * Verifica que un error HTTP del servicio externo
     * se propague al servicio encargado de aplicar el fallback.
     */
    @Test
    void debePropagarErrorHttpDeGeoRef() {

        mockServer.expect(request -> {
                    assertEquals(
                            "/api/v2.0/direcciones",
                            request.getURI().getPath());
                    assertEquals(HttpMethod.GET, request.getMethod());
                })
                .andRespond(withServerError());

        assertThrows(
                RestClientResponseException.class,
                () -> geoRefClient.buscarDirecciones(
                        "San Martin 1234", "Buenos Aires")
        );

        mockServer.verify();
    }
}
