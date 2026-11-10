package com.feriaconecta.backend.common.model;

/**
 * Catálogo de categorías compartido por ferias y postulaciones.
 *
 * Los nombres de las constantes son los utilizados
 * por la API y para su persistencia en la base de datos.
 * El atributo nombre contiene el texto visible para el frontend.
 */
public enum Categoria {

    GASTRONOMIA("Gastronomía"),
    INDUMENTARIA("Indumentaria"),
    ACCESORIOS("Accesorios y marroquinería"),
    ARTESANIAS("Artesanías"),
    DECORACION("Decoración y hogar"),
    COSMETICA("Cosmética y cuidado personal"),
    PLANTAS("Plantas y jardinería"),
    LIBRERIA_PAPELERIA("Librería y papelería"),
    JUGUETES("Juguetes y juegos"),
    MASCOTAS("Productos para mascotas"),
    TECNOLOGIA("Tecnología y electrónica"),
    OTROS("Otros");

    private final String nombre;

    Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
