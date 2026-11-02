package com.feriaconecta.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {
    /**
     * Configuracion de Mysql para las pruebas de integracion
     * utiliza el esquema sql original del proyecto
     */
    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {

        //ruta al esquema original
        Path schema = Path.of("..", "database", "schema.sql")
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(schema)) {
            throw new IllegalStateException("Not a file: " + schema);
        }

        return new MySQLContainer("mysql:8.4")
                .withDatabaseName("feriaconecta_test")
                .withUsername("test")
                .withPassword("test")
                //Mysql ejecuta el script al inicializar el contenedor
                .withCopyFileToContainer(
                        MountableFile.forHostPath(schema.toString()),
                        "/docker-entrypoint-initdb.d/01-schema.sql"
                );
    }
}