package com.feriaconecta.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.mysql.MySQLContainer;

import com.feriaconecta.backend.config.MySqlTestcontainersConfig;

/**
 * Verifica la conexión a MySQL y la inicialización
 * del esquema real mediante Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
class MySqlIntegracionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MySQLContainer mysqlContainer;

    @Test
    void debeConectarAMySqlYEncontrarElEsquema()
            throws SQLException {
        // Obtiene una conexión a la base temporal.
        try (Connection conexion = dataSource.getConnection();
             Statement consulta = conexion.createStatement()) {

            // Comprueba que utilizamos la base de pruebas.
            assertEquals(
                    "feriaconecta_test",
                    conexion.getCatalog()
            );

            // Comprueba que schema.sql creó la tabla usuario.
            try (ResultSet resultado = consulta.executeQuery(
                    "SELECT COUNT(*) FROM usuario")) {

                assertTrue(resultado.next());
            }
        }
    }
}
