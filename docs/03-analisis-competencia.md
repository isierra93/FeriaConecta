# Análisis de competencia y diferenciación

*Nota metodológica: esta matriz se construyó con apoyo de IA como asistente de investigación, siguiendo el enfoque del material de la cátedra: no se buscó que el modelo "adivine" datos exactos de cada herramienta, sino ordenar criterios de comparación e hipótesis razonables a validar. Los datos de precio/funcionalidades deben confirmarse antes de usarse como argumento definitivo en la defensa del TP.*

## Competidores directos

Herramientas que resuelven específicamente la organización de ferias/eventos con emprendedores:

| Herramienta | Qué resuelve | Limitación frente a nuestro caso |
|---|---|---|
| Formularios de Google + planillas de cálculo | Recolectar postulaciones | No integra pago, no genera credenciales, no controla cupos en tiempo real, todo manual |
| Plataformas de gestión de eventos genéricas (Eventbrite y similares) | Venta de entradas, registro de asistentes | Pensadas para entradas a eventos, no para el flujo específico de "postulación → evaluación → asignación de puesto" propio de una feria de emprendedores |

## Competidores indirectos

Alternativas parciales o manuales que hoy compiten por resolver el mismo dolor:

- Grupos de WhatsApp para coordinar con emprendedores.
- Excel/Google Sheets compartidos para llevar el registro de postulaciones y pagos.
- Coordinación telefónica o presencial para asignar puestos el día del evento.

## Variables de comparación

| Variable | Formularios + planillas | Plataformas de eventos genéricas | FeriaConecta |
|---|---|---|---|
| Precio | Gratis (con límites de mantenimiento manual) | Variable, con comisión por entrada | A definir (posible modelo gratuito para organizadores chicos) |
| Gestión de postulación y evaluación | Manual | No contemplada | Sí, con estados claros |
| Pago integrado | No | Sí (para entradas, no para "participación como expositor") | Sí (Mercado Pago, Checkout Pro) |
| Asignación de puestos | Manual | No | Sí |
| Acreditación (control de asistencia) | Manual/papel | Códigos QR genéricos para entradas | QR propio ligado a la postulación confirmada |
| Vista pública de ferias | No | Parcial | Sí |
| Curva de aprendizaje para el organizador | Baja pero muy operativa | Media-alta | Baja (pensada específicamente para este caso de uso) |

## Diferenciadores de FeriaConecta

- Modela el proceso completo y específico de una feria de emprendedores (no de venta de entradas): postulación, evaluación, pago, puesto y acreditación en un solo flujo.
- Integración con GeoRef para normalizar direcciones argentinas, evitando errores de carga manual de ubicación.
- Credencial QR sin datos sensibles embebidos (solo un token), reduciendo riesgo de exposición de información personal o de pago.

## Simulación de escenarios competitivos

- **¿Qué pasa si aparece un competidor con versión gratuita?** El diferencial de FeriaConecta no está en el precio sino en la especificidad del flujo (evaluación de postulaciones + puesto + acreditación integrados). Una alternativa gratuita genérica seguiría requiriendo procesos manuales para esas partes.
- **¿Qué características serían mínimas para no quedar fuera del mercado?** Postulación online, pago integrado y algún mecanismo de control de asistencia — las tres ya están contempladas en el alcance de la v1.
- **¿Qué ventaja puede sostenerse en el tiempo y cuál es fácilmente copiable?** El flujo de estados (postulación → pago → confirmación → credencial) es fácilmente replicable por otro equipo; lo más difícil de copiar rápido es la integración pulida con Mercado Pago y GeoRef ya validada en producción, y el conocimiento del caso de uso real de ferias barriales/comunitarias.

## Pendiente de validar

- Confirmar si existen en Argentina herramientas específicas para gestión de ferias de emprendedores (no solo de eventos en general), mediante una búsqueda dedicada antes de la defensa final.
