package com.feriaconecta.backend.feria.service;

import com.feriaconecta.backend.feria.client.GeoRefClient;
import com.feriaconecta.backend.feria.dto.DireccionNormalizadaResponse;
import com.feriaconecta.backend.feria.dto.GeoRefApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Servicio encargado de consultar y normalizar direcciones mediante GeoRef.
 *
 * Procesa las respuestas del servicio externo y las transforma
 * al formato utilizado por FeriaConecta.
 *
 * Si GeoRef no encuentra resultados o falla la consulta HTTP,
 * devuelve los datos originales ingresados por el organizador,
 * sin impedir que continúe el proceso de creación de la feria.
 */
@Service
public class GeoRefService {

    private final GeoRefClient geoRefClient;
    /**
     * Inicializa el servicio con el cliente encargado de consultar GeoRef.
     *
     * @param geoRefClient cliente HTTP de la API GeoRef
     */
    public GeoRefService(GeoRefClient geoRefClient) {
        this.geoRefClient = geoRefClient;
    }

    /**
     * Busca sugerencias de direcciones normalizadas.
     *
     * Consulta GeoRef utilizando la calle, altura y provincia.
     * Si encuentra resultados válidos, devuelve las sugerencias
     * normalizadas conservando el municipio ingresado.
     *
     * Ante fallos HTTP o ausencia de resultados válidos, devuelve
     * una dirección no normalizada con los datos originales.
     *
     * @param provincia provincia ingresada por el organizador
     * @param municipio municipio ingresado por el organizador
     * @param localidad localidad ingresada por el organizador
     * @param calle calle ingresada por el organizador
     * @param altura altura ingresada por el organizador
     * @return lista de sugerencias o resultado no normalizado
     */
    public List<DireccionNormalizadaResponse> buscarSugerencias(
            String provincia,
            String municipio,
            String localidad,
            String calle,
            String altura) {

        String direccion = calle + " " + altura;

        try {
            GeoRefApiResponse respuesta =
                    geoRefClient.buscarDirecciones(direccion, provincia);

            if (respuesta == null || respuesta.direcciones() == null) {
                return List.of(noNormalizada(
                        provincia, municipio, localidad, calle, altura));
            }

            List<DireccionNormalizadaResponse> sugerencias =
                    respuesta.direcciones().stream()
                            .filter(Objects::nonNull)
                            .filter(this::tieneDatosMinimos)
                            .map(direccionGeoRef -> normalizar(
                                    direccionGeoRef,
                                    municipio,
                                    localidad))
                            .toList();

            if (sugerencias.isEmpty()) {
                return List.of(noNormalizada(
                        provincia, municipio, localidad, calle, altura));
            }

            return sugerencias;

        } catch (RestClientException e) {
            return List.of(noNormalizada(
                    provincia, municipio, localidad, calle, altura));
        }
    }

    /**
     * Verifica que una dirección contenga los datos mínimos
     * necesarios para considerarla normalizada.
     *
     * Requiere provincia, calle y altura reconocidas por GeoRef.
     *
     * @param direccion dirección recibida desde GeoRef
     * @return true si contiene los datos mínimos; false en caso contrario
     */
    private boolean tieneDatosMinimos(
            GeoRefApiResponse.Direccion direccion) {

        return direccion.provincia() != null
                && tieneTexto(direccion.provincia().nombre())
                && direccion.calle() != null
                && tieneTexto(direccion.calle().nombre())
                && direccion.altura() != null
                && direccion.altura().valor() != null;
    }
    /**
     * Comprueba que un texto no sea nulo ni esté vacío.
     *
     * @param valor texto que se desea validar
     * @return true si contiene caracteres distintos de espacios
     */
    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Convierte una dirección de GeoRef al formato de FeriaConecta.
     *
     * Utiliza los datos normalizados disponibles y conserva
     * el municipio original, ya que GeoRef no garantiza ese campo.
     * Si no informa localidad censal, mantiene la localidad ingresada.
     *
     * Las coordenadas quedan en null cuando no están disponibles.
     *
     * @param direccion dirección recibida desde GeoRef
     * @param municipio municipio ingresado originalmente
     * @param localidad localidad ingresada originalmente
     * @return dirección normalizada con los datos disponibles
     */
    private DireccionNormalizadaResponse normalizar(
            GeoRefApiResponse.Direccion direccion,
            String municipio,
            String localidad) {

        String localidadNormalizada = localidad;

        if (direccion.localidadCensal() != null
                && tieneTexto(direccion.localidadCensal().nombre())) {
            localidadNormalizada =
                    direccion.localidadCensal().nombre();
        }

        BigDecimal latitud = null;
        BigDecimal longitud = null;

        if (direccion.ubicacion() != null) {
            latitud = direccion.ubicacion().lat();
            longitud = direccion.ubicacion().lon();
        }

        return new DireccionNormalizadaResponse(
                true,
                direccion.provincia().nombre(),
                municipio,
                localidadNormalizada,
                direccion.calle().nombre(),
                String.valueOf(direccion.altura().valor()),
                latitud,
                longitud,
                null
        );
    }

    /**
     * Construye una respuesta cuando no se puede normalizar la dirección.
     *
     * Conserva los datos originales del organizador, establece
     * las coordenadas en null e incorpora un aviso informativo.
     *
     * @param provincia provincia original
     * @param municipio municipio original
     * @param localidad localidad original
     * @param calle calle original
     * @param altura altura original
     * @return dirección no normalizada con un aviso
     */
    private DireccionNormalizadaResponse noNormalizada(
            String provincia,
            String municipio,
            String localidad,
            String calle,
            String altura) {

        return new DireccionNormalizadaResponse(
                false,
                provincia,
                municipio,
                localidad,
                calle,
                altura,
                null,
                null,
                "No se pudo normalizar la dirección. Se conservarán los datos ingresados."
        );
    }
}
