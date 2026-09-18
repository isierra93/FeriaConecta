# Viabilidad del proyecto

## Viabilidad técnica

El equipo puede implementar la solución con el stack elegido (React + Spring Boot + MySQL), ya que son tecnologías conocidas por al menos parte del equipo (ver justificación en `02-stack-tecnologico.md`). La mayor dependencia externa es Mercado Pago (para el flujo de pago) y GeoRef (para direcciones); ambas son APIs públicas, documentadas y con modo sandbox disponible para pruebas, lo que reduce el riesgo de bloqueo total del desarrollo por causas externas.

**Dependencia crítica que puede bloquear el desarrollo:** la integración de Mercado Pago (webhook + verificación de orden), porque varias funcionalidades posteriores (credencial QR, asignación de puesto) dependen de que una postulación quede `CONFIRMADA`. Por eso se prioriza en el Sprint 4, antes de construir el resto del flujo de acreditación sobre datos simulados.

## Viabilidad operativa

Existen condiciones reales para usar el sistema: los tres roles (organizador, emprendedor, público) representan personas que hoy ya participan del proceso manual, por lo que no se requiere crear un público nuevo, solo migrar un proceso existente a la plataforma. El riesgo operativo principal es la adopción por parte de organizadores con bajo perfil técnico, mitigado con una interfaz simple (ya contemplado como necesidad del actor "Organizador" en `01-definicion-problema.md`).

## Viabilidad temporal

El alcance definido para la v1 (sección 2 del README) es amplio pero está descompuesto en 7 sprints con dependencias claras (`04-plan-de-trabajo.md`). Si los tiempos reales de la cátedra son más ajustados que lo estimado, la primera funcionalidad candidata a postergarse es la generación de **estadísticas para el organizador** (punto 10 del alcance), ya que no es indispensable para el flujo central postulación → pago → acreditación.

## Preguntas de control

- **¿Qué dependencia crítica puede bloquear el desarrollo?** La integración con Mercado Pago (ver viabilidad técnica).
- **¿Qué funcionalidad parece "necesaria" pero podría postergarse?** Las estadísticas básicas para el organizador y el listado público "enriquecido" (podría lanzarse primero como listado simple).
- **¿Qué evidencia mínima necesitamos para validar que la solución aporta valor real?** Que un organizador real complete el ciclo completo (crear feria → recibir postulaciones → cobrar → acreditar) sin depender de WhatsApp o planillas paralelas durante la prueba.
