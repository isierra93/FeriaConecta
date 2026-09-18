# Plan de trabajo

## Objetivo general

Desarrollar y desplegar una plataforma web funcional que permita gestionar el ciclo completo de una feria de emprendedores: publicación, postulación, evaluación, pago, asignación de puesto y acreditación mediante QR, medible a través de la entrega de un MVP operativo antes de la fecha de defensa del TP.

## Objetivos específicos

1. Implementar el registro, login y permisos por rol para los tres tipos de usuario.
2. Implementar la gestión de ferias y el flujo de postulación/evaluación.
3. Integrar el cobro de la participación mediante Mercado Pago (Checkout Pro).
4. Generar y validar credenciales QR para el control de asistencia.
5. Desplegar el sistema en un entorno accesible públicamente para las entregas.

## Alcance y no alcance

*(Ya definido en README, secciones 2 y 8 — se referencia aquí para que el plan quede autocontenido.)*

- **Incluye:** roles y permisos, perfiles de emprendedores, gestión de ferias, postulación/evaluación, pago con Mercado Pago, credencial QR, asignación de puestos, listado público, integración con GeoRef.
- **No incluye en la v1:** mensajería interna, mapas interactivos, plano visual de puestos.

## Entregables por etapa

| Etapa | Entregable | Evidencia |
|---|---|---|
| 1 | Modelo de datos y entorno base | Diagrama ER, script DDL, repos de frontend/backend inicializados y desplegando "hola mundo" |
| 2 | Autenticación y gestión de ferias | API funcional de login/roles + CRUD de ferias, documentada en Swagger |
| 3 | Postulación y evaluación | Flujo completo postulación → aprobación/rechazo, con pruebas unitarias de las reglas de estado |
| 4 | Integración de pagos | Flujo de pago end-to-end con Mercado Pago en modo sandbox, webhook verificado |
| 5 | Credencial QR y acreditación | Generación de QR, endpoint de validación, demo de escaneo |
| 6 | Puestos, vista pública y estadísticas | Asignación de puestos, listado público, panel básico de estadísticas para el organizador |
| 7 | Integración final y despliegue | Sistema desplegado (VPS + Vercel) accesible para la defensa, documentación actualizada |

## Estimación de tiempos (sprints de 2 semanas, referencial)

| Sprint | Foco principal | Dependencias |
|---|---|---|
| 1 | Modelo de datos + entorno + autenticación | Ninguna |
| 2 | Gestión de ferias + perfiles de emprendedores | Sprint 1 |
| 3 | Postulación y evaluación | Sprint 2 |
| 4 | Integración de pagos (sandbox) | Sprint 3 (requiere postulación aprobada como disparador) |
| 5 | Credencial QR y acreditación | Sprint 4 (requiere pago confirmado) |
| 6 | Puestos, vista pública y estadísticas | Sprint 3 (puede avanzar en paralelo con 4/5) |
| 7 | Integración, pruebas end-to-end y despliegue final | Todos los anteriores |

*Ajustar la duración real de cada sprint según las fechas de entrega parciales de la cátedra y las horas disponibles del equipo.*

## Riesgos iniciales y mitigaciones

| Riesgo | Tipo | Mitigación |
|---|---|---|
| Retrasos por curva de aprendizaje en Spring Security/JWT | Técnico | Reservar tiempo dedicado en el Sprint 1, antes de que otras features dependan de la autenticación |
| Problemas con el entorno sandbox de Mercado Pago (webhooks no llegan en entorno local) | Técnico | Probar el webhook contra el VPS desplegado tempranamente, no dejarlo para el final |
| Sobrecarga de un integrante si las tareas no están bien repartidas | Organizativo | Usar Issues y Milestones en GitHub Projects (ya definido en README sección 7) con responsables asignados por sprint |
| Postergar la documentación (Actividades 1-3) hasta el final | Organizativo | Mantener actualizado `/docs` en paralelo al desarrollo, no como tarea de último momento |

## Criterios de éxito del MVP

- Un organizador puede crear una feria de punta a punta y recibir postulaciones reales.
- Un emprendedor puede postularse, pagar y recibir su credencial QR sin intervención manual del equipo de desarrollo.
- El QR generado se valida correctamente y rechaza intentos de reutilización.
- El sistema está desplegado y accesible públicamente al momento de la defensa.
- El repositorio `/docs` refleja fielmente las decisiones tomadas (problema, stack, competencia, viabilidad).
