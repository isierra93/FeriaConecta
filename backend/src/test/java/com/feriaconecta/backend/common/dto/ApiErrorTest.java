package com.feriaconecta.backend.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiErrorTest {

    @Test
    void ofCompletaStatusYFraseEstandarSinDetalles() {
        ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "No se encontró la feria con id 1", "/api/v1/ferias/1");

        assertThat(error.status()).isEqualTo(404);
        assertThat(error.error()).isEqualTo("Not Found");
        assertThat(error.message()).isEqualTo("No se encontró la feria con id 1");
        assertThat(error.path()).isEqualTo("/api/v1/ferias/1");
        assertThat(error.timestamp()).isNotNull();
        assertThat(error.details()).isEmpty();
    }

    @Test
    void ofConDetallesConservaLosErroresPorCampo() {
        var detalle = new ApiError.FieldErrorDetail("nombre", "no debe estar vacío");

        ApiError error = ApiError.of(HttpStatus.BAD_REQUEST, "Datos inválidos", "/api/v1/ferias", List.of(detalle));

        assertThat(error.details()).containsExactly(detalle);
    }
}
