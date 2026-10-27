/**
 * Módulo <b>acreditacion</b>. Acreditación por QR el día del evento. No tiene tabla propia: opera sobre credencial y postulación.
 *
 * <p>Servicio expuesto a otros módulos: {@code AcreditacionService}.</p>
 * <p>Depende de: credencial, postulacion, pago.</p>
 * <p>Regla: otros módulos usan únicamente sus servicios, nunca su {@code repository}.</p>
 */
package com.feriaconecta.backend.acreditacion;
