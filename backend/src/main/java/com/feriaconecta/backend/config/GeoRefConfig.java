package com.feriaconecta.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuración del cliente HTTP utilizado para consumir la API GeoRef.
 *
 * Define la URL base y los tiempos máximos de conexión y lectura
 * mediante propiedades configurables de la aplicación.
 */
@Configuration
public class GeoRefConfig {

    /**
     * Crea el cliente HTTP que utilizará la integración con GeoRef.
     *
     * Configura los tiempos de espera para evitar que una solicitud
     * quede bloqueada indefinidamente si el servicio externo no responde.
     *
     * @param builder constructor de clientes REST proporcionado por Spring
     * @param baseUrl URL base de la API GeoRef
     * @param connectTimeout tiempo máximo para establecer la conexión
     * @param readTimeout tiempo máximo de espera de lectura
     * @return cliente REST configurado para consultar GeoRef
     */
    @Bean("geoRefRestClient")
    public RestClient geoRefRestClient(
            RestClient.Builder builder,
            @Value("${feriaconecta.georef.base-url}") String baseUrl,
            @Value("${feriaconecta.georef.connect-timeout}") Duration connectTimeout,
            @Value("${feriaconecta.georef.read-timeout}") Duration readTimeout
            ) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

    }
}
