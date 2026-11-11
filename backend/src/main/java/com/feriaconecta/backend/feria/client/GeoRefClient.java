package com.feriaconecta.backend.feria.client;

import com.feriaconecta.backend.feria.dto.GeoRefApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP encargado de consumir la API externa GeoRef.
 *
 * Realiza consultas de direcciones utilizando el RestClient
 * configurado para GeoRef y convierte las respuestas recibidas
 * al DTO interno de FeriaConecta.
 */
@Component
public class GeoRefClient {

    private final RestClient restClient;
    /**
     * Inicializa el cliente utilizando la configuración específica
     * de GeoRef registrada en la aplicación.
     *
     * @param restClient cliente REST configurado para GeoRef
     */
    public GeoRefClient(
            @Qualifier("geoRefRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Consulta direcciones en GeoRef según los datos ingresados.
     *
     * Realiza una solicitud GET al endpoint /direcciones,
     * filtrando por dirección y provincia, con un máximo
     * de cinco resultados.
     *
     * @param direccion calle y altura que se desean consultar
     * @param provincia provincia utilizada para filtrar la búsqueda
     * @return respuesta de GeoRef con las direcciones encontradas,
     *         o null si la respuesta no contiene un cuerpo
     */
    public GeoRefApiResponse buscarDirecciones(String direccion, String provincia) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/direcciones")
                        .queryParam("direccion", direccion)
                        .queryParam("provincia", provincia)
                        .queryParam("max", 5)
                        .build())
                .retrieve()
                .body(GeoRefApiResponse.class);
    }
}
