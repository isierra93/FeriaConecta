package com.feriaconecta.backend.common.model;

/**
 * Catálogo de categorías compartido por ferias y postulaciones.
 * <p>
 * El nombre de cada constante ({@link #name()}) es el código que utiliza la
 * API y el que se persiste en las columnas {@code feria.categoria_permitida}
 * y {@code postulacion.rubro}. La etiqueta es solo el texto visible para el
 * frontend y nunca se persiste.
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

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
