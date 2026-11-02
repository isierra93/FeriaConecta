package com.feriaconecta.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class MockitoEjemploTest {

    // Clase sencilla utilizada únicamente para demostrar
    // cómo probar una unidad con una dependencia simulada.
    private static class ServicioEjemplo {

        private final Supplier<String> proveedor;

        ServicioEjemplo(Supplier<String> proveedor) {
            this.proveedor = proveedor;
        }

        String obtenerNombre() {
            return proveedor.get().trim();
        }
    }

    @Test
    void debeObtenerYNormalizarElNombre() {
        // Simulamos la dependencia.
        @SuppressWarnings("unchecked") Supplier<String> proveedor = mock(Supplier.class);

        when(proveedor.get()).thenReturn(" FeriaConecta ");

        // Ejecutamos el método que queremos probar.
        ServicioEjemplo servicio = new ServicioEjemplo(proveedor);
        String resultado = servicio.obtenerNombre();

        // Verificamos el resultado y la interacción.
        assertEquals("FeriaConecta", resultado);
        verify(proveedor).get();
    }
}