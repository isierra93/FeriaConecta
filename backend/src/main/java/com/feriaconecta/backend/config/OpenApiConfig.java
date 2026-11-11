package com.feriaconecta.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de la documentación OpenAPI / Swagger UI.
 * <p>
 * Define la información general de la API, el esquema de seguridad Bearer JWT
 * (botón "Authorize" de Swagger UI) y los tags que agrupan los endpoints por
 * módulo del backend.
 * <p>
 * Convenciones para los controllers:
 * <ul>
 *   <li>Anotar la clase con {@code @Tag(name = OpenApiConfig.TAG_...)} del módulo
 *   al que pertenece.</li>
 *   <li>Todos los endpoints requieren JWT por defecto. Los públicos
 *   ({@code /auth/login}, {@code /auth/registro}, {@code /public/**}) se anotan
 *   con {@code @SecurityRequirements()} vacío para quitar el candado.</li>
 * </ul>
 * En producción springdoc está deshabilitado ({@code application-prod.yml}).
 */
@Configuration
public class OpenApiConfig {

    /** Nombre del esquema de seguridad referenciado por los endpoints. */
    public static final String BEARER_AUTH = "bearerAuth";

    public static final String TAG_AUTH = "auth";
    public static final String TAG_FERIA = "feria";
    public static final String TAG_PUESTO = "puesto";
    public static final String TAG_POSTULACION = "postulacion";
    public static final String TAG_PAGO = "pago";
    public static final String TAG_CREDENCIAL = "credencial";
    public static final String TAG_ACREDITACION = "acreditacion";
    public static final String TAG_PANEL = "panel";
    public static final String TAG_PUBLICO = "publico";

    @Bean
    public OpenAPI feriaConectaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FeriaConecta API")
                        .version("v1")
                        .description("""
                                API REST de FeriaConecta, plataforma para organizar ferias de \
                                emprendedores: ferias, postulaciones, pagos con Mercado Pago, \
                                credenciales QR, puestos y acreditación.

                                Para probar endpoints protegidos, obtener un token con \
                                `POST /auth/login` y cargarlo con el botón **Authorize** \
                                (solo el token, sin el prefijo `Bearer`)."""))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en /auth/login")))
                // Por defecto todos los endpoints requieren JWT.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .tags(moduleTags());
    }

    /** Tags alineados con los paquetes de módulo del backend. */
    private static List<Tag> moduleTags() {
        return List.of(
                tag(TAG_AUTH, "Autenticación y registro de usuarios: registro, inicio de sesión con JWT y roles."),
                tag(TAG_FERIA, "Gestión de ferias (CRUD + estados) con su ubicación normalizada por GeoRef."),
                tag(TAG_PUESTO, "Puestos de una feria (CRUD) y su asignación a participantes confirmados."),
                tag(TAG_POSTULACION, "Postulaciones de emprendedores a una feria y su evaluación por el organizador."),
                tag(TAG_PAGO, "Pagos con Mercado Pago: creación de la orden y confirmación por webhook."),
                tag(TAG_CREDENCIAL, "Credenciales QR: token UUID y generación bajo demanda del SVG."),
                tag(TAG_ACREDITACION, "Acreditación por QR el día del evento."),
                tag(TAG_PANEL, "Panel y reportes del organizador (solo lectura)."),
                tag(TAG_PUBLICO, "Consultas públicas sin autenticación: listado de ferias y catálogo de categorías (solo lectura)."));
    }

    private static Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }
}
