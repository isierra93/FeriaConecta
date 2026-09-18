# Proceso de refinamiento asistido por IA

Este documento deja constancia de cómo se usó la IA como herramienta de trabajo durante la definición del proyecto, tal como pide la Actividad 3: no como generador de texto final, sino como asistente para confrontar la idea inicial, estructurar el análisis de competencia y ordenar el plan de trabajo.

## Prompting de rol utilizado

Se simuló la consulta a distintos perfiles profesionales para poner la propuesta bajo estrés:

- *"Actuá como un organizador de ferias barriales con 10 años de experiencia. Revisá esta propuesta de plataforma y decime qué le faltaría para que realmente la uses en tu próxima feria."*
- *"Actuá como un desarrollador backend senior especializado en integraciones de pago. Revisá el flujo de Mercado Pago propuesto y señalá puntos ciegos de seguridad o de manejo de errores."*

## Identificación de puntos de dolor

Preguntas usadas para detectar problemas no evidentes a simple vista:

- *"¿Qué parte de este flujo (postulación → pago → credencial → acreditación) es más probable que falle en producción y por qué?"*
- *"¿Qué necesitaría un emprendedor con poca experiencia digital para no abandonar el proceso de postulación a mitad de camino?"*

## Definición del MVP

Se usó la IA para ayudar a distinguir qué era indispensable para la primera versión y qué podía postergarse, lo que derivó directamente en la sección 8 del README ("Límites y posibles mejoras") y en la nota de alcance sobre GeoRef (sección 4.2): mensajería interna, mapas interactivos y plano visual de puestos quedaron fuera de la v1 tras este análisis, por no ser indispensables para validar el valor central de la propuesta.

## Autoprompting

Antes de pedir ayuda con dudas técnicas puntuales (por ejemplo, cómo verificar de forma segura una notificación de pago), se le pidió primero a la IA que sugiriera la mejor forma de formular esa pregunta, para obtener una respuesta más precisa y evitar soluciones genéricas o inseguras (como confiar únicamente en la URL de retorno del navegador, riesgo que quedó explícitamente evitado en la sección 4.3 del README).

## Nivel de detalle según el objetivo

- Para decisiones ya definidas y que requerían precisión (por ejemplo, el flujo exacto de verificación de Mercado Pago), se usaron prompts detallados y acotados.
- Para explorar alternativas todavía abiertas (por ejemplo, qué stack de despliegue convenía dado que ya se contaba con un VPS propio), se usaron prompts más generales para no cerrar opciones prematuramente.

## Resultado del proceso

El uso de IA no reemplazó las decisiones del equipo, sino que sirvió para: (1) confrontar la propuesta inicial contra perspectivas externas simuladas, (2) estructurar el análisis de competencia (`03-analisis-competencia.md`) y (3) ordenar el plan de trabajo y la evaluación de viabilidad (`04-plan-de-trabajo.md` y `05-viabilidad.md`) antes de comenzar el desarrollo.
