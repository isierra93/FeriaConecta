package com.feriaconecta.backend.publico.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el catálogo de categorías esté disponible
 * públicamente y devuelva los códigos y nombres acordados.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Comprueba que el endpoint pueda consultarse sin JWT
     * y devuelva exactamente las 12 categorías del catálogo.
     */
    @Test
    void debeListarLasCategoriasSinAutenticacion() throws Exception {

        mockMvc.perform(
                        get("/api/v1/public/categorias")
                                .contextPath("/api/v1"))
                .andExpect(status().isOk())

                //la respuesta debe contener 12 categorias
                .andExpect(jsonPath("$", hasSize(12)))

                //verifica los codigos acordados por el equipo
                .andExpect(jsonPath("$[*].codigo", contains(
                        "GASTRONOMIA",
                        "INDUMENTARIA",
                        "ACCESORIOS",
                        "ARTESANIAS",
                        "DECORACION",
                        "COSMETICA",
                        "PLANTAS",
                        "LIBRERIA_PAPELERIA",
                        "JUGUETES",
                        "MASCOTAS",
                        "TECNOLOGIA",
                        "OTROS"
                )))
                //verifica los nombres visibles para frontend
                .andExpect(jsonPath("$[*].nombre", contains(
                        "Gastronomía",
                        "Indumentaria",
                        "Accesorios y marroquinería",
                        "Artesanías",
                        "Decoración y hogar",
                        "Cosmética y cuidado personal",
                        "Plantas y jardinería",
                        "Librería y papelería",
                        "Juguetes y juegos",
                        "Productos para mascotas",
                        "Tecnología y electrónica",
                        "Otros"
                )));
    }
}
