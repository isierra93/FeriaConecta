# Justificación del stack tecnológico

El stack se eligió priorizando tecnologías que el equipo ya conoce (reduciendo el costo de aprendizaje en un proyecto con fecha de entrega fija) y que se ajustan a la naturaleza y escala real del problema: una aplicación web transaccional, con integraciones a APIs externas (Mercado Pago, GeoRef), pensada para uso por un número moderado de ferias y usuarios simultáneos — no para una escala masiva de tipo "miles de usuarios concurrentes en tiempo real".

## Frontend: React + Vite + TypeScript

- **Por qué:** es la tecnología con la que el equipo tiene más experiencia previa, lo que permite escribir y revisar código más rápido y anticipar limitaciones desde el inicio.
- **Alternativas consideradas:** Angular (descartado por mayor curva de aprendizaje sin experiencia previa del equipo) y HTML/JS plano (descartado porque el proyecto necesita manejo de estado y componentes reutilizables — formularios de postulación, tablas de puestos, flujo de pago).
- **TypeScript** se suma sobre JavaScript para reducir errores en tiempo de desarrollo, algo valioso dado que hay integraciones externas (Mercado Pago, GeoRef) donde los contratos de datos deben respetarse con precisión.

## Backend: Java 21 + Spring Boot

- **Por qué:** Spring Boot es un framework maduro para aplicaciones web con lógica de negocio compleja (estados de postulación, pago y credencial que cambian según reglas claras), con seguridad de tipado y buen soporte para integraciones REST con terceros.
- **Naturaleza del problema:** no es un sistema de alta concurrencia en tiempo real (no hay chat ni actualizaciones en vivo entre usuarios), por lo que no se justifica optar por Node.js o Go solo por su modelo de I/O no bloqueante. El problema es transaccional (pagos, estados, validaciones), terreno donde Spring Boot es sólido.
- **Alternativas consideradas:** Node.js/NestJS (descartado porque unificar lenguaje con el frontend no compensaba la menor experiencia del equipo en un backend con reglas de negocio complejas) y Laravel/PHP (descartado por no ser la tecnología que el equipo domina).

## Base de datos: MySQL

- **Por qué:** los datos del dominio (usuarios, ferias, postulaciones, pagos, puestos, credenciales) tienen una estructura fija y con relaciones importantes entre sí (una postulación pertenece a una feria y a un emprendedor, un pago pertenece a una postulación, etc.), y se necesita integridad transaccional fuerte, sobre todo en el flujo de pagos. Esto encaja directamente con el escenario donde el material recomienda una base relacional.
- **Alternativa considerada:** MongoDB (descartado porque el modelo de datos no es jerárquico ni de esquema variable — es un dominio clásico de tablas relacionadas con necesidad de ACID en pagos).

## Seguridad: Spring Security + JWT

- **Por qué:** el sistema tiene tres roles con permisos distintos (organizador, emprendedor, público general) y algunos endpoints deben ser públicos (listado de ferias) y otros protegidos (gestión de postulaciones, pagos). JWT permite manejar sesiones sin estado, lo cual simplifica el despliegue del backend en un contenedor Docker sin depender de sesiones persistentes en memoria.

## Pruebas y documentación: JUnit, Mockito, Postman, Swagger/OpenAPI

- **Por qué:** dado que hay flujos críticos (verificación de pagos, validación de QR) que no deben fallar silenciosamente, se prioriza tener pruebas unitarias de las reglas de negocio (JUnit/Mockito) y documentación de la API navegable (Swagger) para facilitar la integración entre frontend y backend en un equipo de tres personas.

## Despliegue: VPS propio (Docker + Portainer + Nginx Proxy Manager) para backend/BD, Vercel para frontend

- **Por qué esta combinación y no otra:**
  - El backend y la base de datos necesitan procesos de larga duración y conexiones persistentes a servicios externos (webhooks de Mercado Pago), lo que **descarta serverless** como opción principal para esa parte (el material señala justamente esta limitación de serverless).
  - Se usa **Docker** porque resuelve el problema clásico de diferencias entre entorno de desarrollo y producción, y facilita que los tres integrantes trabajen sobre el mismo entorno reproducible.
  - Se descartó un **PaaS gestionado tipo Heroku/Render** para el backend por una restricción real: el equipo ya cuenta con un VPS Oracle Cloud disponible sin costo adicional, y el material señala que cuando ya existen recursos técnicos disponibles, conviene adaptarse a ellos en lugar de sumar un servicio nuevo.
  - El **frontend sí se aloja en Vercel** porque, al ser contenido estático, no tiene las mismas necesidades de procesos persistentes, y Vercel ofrece CI/CD automático desde GitHub sin configuración adicional — algo que no se justifica montar manualmente en el VPS para este componente.

## Riesgos identificados y mitigación

| Riesgo | Mitigación |
|---|---|
| Dependencia de un único VPS para backend y base de datos (punto único de falla) | Backups periódicos del volumen de MySQL; documentar el proceso de restauración |
| Webhooks de Mercado Pago no lleguen o lleguen duplicados | Verificación server-side con `GET /v1/orders/{id}` antes de confirmar el pago (ya contemplado en el flujo) y manejo idempotente por `external_reference` |
| Poca experiencia previa del equipo con Spring Security + JWT en profundidad | Reservar tiempo de aprendizaje dirigido en el sprint inicial antes de construir sobre esa base, y validar con pruebas unitarias tempranas |
| Cambios en la API de Mercado Pago u OpenAPI de GeoRef | Aislar ambas integraciones detrás de una capa de servicio propia en el backend, para que un cambio externo no impacte directamente en la lógica de negocio |

## Escala esperada

El sistema está pensado para uso por una feria a la vez con decenas o pocos cientos de participantes, no miles de usuarios concurrentes. Por eso no se justifica una arquitectura de microservicios ni herramientas de orquestación como Kubernetes: sería sobreingeniería para el escenario real del proyecto.
