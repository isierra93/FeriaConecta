/**
 * Módulo <b>pago</b>. Pagos con Mercado Pago: creación de la orden y confirmación por webhook.
 *
 * <p>Servicio expuesto a otros módulos: {@code PagoService}.</p>
 * <p>Depende de: postulacion, credencial.</p>
 * <p>Regla: otros módulos usan únicamente sus servicios, nunca su {@code repository}.</p>
 */
package com.feriaconecta.backend.pago;
