# FeriaConecta

**Plataforma web para organizar ferias y conectar emprendedores**

Trabajo Final Integrador — Tecnicatura en Programación (UTN) • Agosto 2026

## Integrantes y tutor

**Alumnos:**

- Victor Ivan Sierra
- Facundo Miguel Archiria
- Nahuel Alfredo Ayala

**Tutor:**

- Sebastián Bruselario

> **Idea principal:** centralizar en un solo sistema la creación de ferias, la postulación de emprendedores, el pago de la participación, la asignación de puestos y la acreditación mediante un código QR.

---

## 1. Problema y objetivo

La organización de ferias suele realizarse mediante formularios, planillas, mensajes de WhatsApp y anotaciones separadas. Esto dificulta controlar los cupos, conocer el estado de cada postulación, verificar los pagos, asignar puestos sin errores y registrar la asistencia de los participantes.

FeriaConecta busca digitalizar y ordenar el proceso completo, desde la publicación de una feria hasta la acreditación del emprendedor el día del evento. También ofrecerá una vista pública para que cualquier persona pueda consultar las próximas ferias y los emprendimientos participantes.

**Usuarios:** organizador o administrador, emprendedor y público general.

## 2. Alcance de la primera versión

1. Registro e inicio de sesión con roles y permisos.
2. Perfiles de emprendedores con datos de contacto, descripción y rubro.
3. Gestión de ferias: fechas, ubicación, costo de participación, cupos, categorías y estado.
4. Postulación de emprendedores y evaluación por parte del organizador.
5. Pago de la participación mediante Mercado Pago para las postulaciones aprobadas.
6. Confirmación automática de la participación después de verificar el pago.
7. Generación de una credencial QR para cada participante confirmado.
8. Escaneo y validación del QR para registrar la asistencia sin duplicaciones.
9. Creación de puestos y asignación de cada espacio a un participante confirmado.
10. Listado público de ferias y estadísticas básicas para el organizador.
11. Validación, normalización y asistencia para el autocompletado de la dirección de la feria mediante la API GeoRef.

## 3. Flujo principal de funcionamiento

1. El organizador crea una feria y define sus datos, dirección, rubros permitidos, cantidad de puestos y costo de participación.
2. El emprendedor completa su perfil y envía una postulación.
3. El organizador revisa la solicitud y la aprueba o rechaza.
4. Si la postulación es aprobada, queda pendiente de pago y el sistema genera una orden de Mercado Pago.
5. El emprendedor es redirigido a Checkout Pro para abonar la participación.
6. Mercado Pago notifica el resultado mediante un Webhook y el backend verifica la orden antes de confirmar el pago.
7. Cuando el pago es aprobado, la postulación pasa a estado `CONFIRMADA` y el sistema genera una credencial QR.
8. El organizador asigna un puesto al participante confirmado.
9. El día de la feria, el organizador escanea el QR y confirma la asistencia.
10. La información queda guardada para consultar el historial y obtener estadísticas.

## 4. Implementación propuesta

Se desarrollará como una aplicación web responsive. El frontend consumirá una API REST creada con Spring Boot y la información se almacenará en MySQL. La seguridad se manejará mediante autenticación con JWT y permisos según el rol del usuario.

Para mantener el proyecto ordenado se utilizará un **monolito modular** y un único repositorio de GitHub, separado en carpetas de frontend, backend y documentación. El sistema se publicará en servicios en la nube para que pueda probarse en línea durante las entregas.

### 4.1 Arquitectura de despliegue
 
El sistema se despliega en dos entornos distintos según el componente, aprovechando infraestructura propia para el backend/datos y una plataforma especializada para el frontend:
 
| Componente | Dónde se despliega | Detalle |
|---|---|---|
| **Backend (Spring Boot)** | VPS propio (Oracle Cloud) | Contenedor Docker gestionado desde **Portainer**, corriendo sobre Docker Engine. Expone su puerto interno (8080) únicamente dentro de la red Docker del stack — no se expone directamente a internet. |
| **Base de datos (MySQL)** | Mismo VPS (Oracle Cloud) | Contenedor MySQL ya operativo, con volumen persistente para los datos. Accesible solo por red interna Docker desde el contenedor del backend (nombre de servicio, no IP pública). |
| **Frontend (React)** | Vercel | Build estático (Vite) desplegado con CI/CD automático desde GitHub (cada push a `main` dispara un nuevo deploy). Consume la API pública vía HTTPS. |

### 4.2. Integración con la API GeoRef

Se utilizará **GeoRef**, el Servicio de Normalización de Datos Geográficos de Argentina, como API REST externa. Su función será asistir el autocompletado y validar o normalizar **la dirección de la feria** —provincia, municipio, localidad y calle— al momento de crearla.

> Aclaración de alcance: GeoRef trabaja sobre divisiones geográficas y direcciones reales (provincia → localidad → calle), por lo que solo aplica a dónde se realiza la feria en su conjunto. Los puestos (stand 1, stand 2, etc.) no tienen una ubicación geográfica propia: son posiciones dentro del layout interno del evento, identificadas por número/código, sin relación con GeoRef. La distribución visual de puestos dentro de una feria queda fuera del alcance de la v1 (ver sección 8, "plano visual de los puestos" como mejora futura).

La comunicación con GeoRef se realizará desde el backend para centralizar la integración y evitar que el frontend dependa directamente del servicio externo.

- **Documentación oficial:** [Referencia completa de la API GeoRef V2](https://www.argentina.gob.ar/georef/referencia-completa-de-la-api-georef-v-2)
- **URL base utilizada por la aplicación:** `https://apis.datos.gob.ar/georef/api/v2.0/`
- **Endpoint principal para normalizar direcciones:** `GET /direcciones`

### 4.3. Integración con Mercado Pago

Los pagos se implementarán mediante **Checkout Pro vía Orders API**. FeriaConecta no capturará datos de tarjetas: el emprendedor será redirigido al entorno seguro de Mercado Pago para completar la operación.

El flujo técnico será el siguiente:

1. El backend crea una orden mediante `POST https://api.mercadopago.com/v1/orders`.
2. La postulación se vincula con la orden mediante `external_reference`.
3. Cada intento utiliza un valor UUID en el encabezado `X-Idempotency-Key` para evitar órdenes duplicadas.
4. Mercado Pago devuelve un `checkout_url` y el frontend redirige allí al emprendedor.
5. El backend recibe las novedades de la orden mediante un Webhook HTTPS.
6. Antes de modificar datos, el backend valida la firma `x-signature` y consulta la orden mediante `GET /v1/orders/{id}`.
7. La participación se confirma únicamente cuando la orden verificada posee el estado `processed`.

Este proceso evita considerar como válido un pago basándose solamente en la URL de retorno del navegador.

- **Documentación:** [Checkout Pro mediante Orders API](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro-orders/create-order)
- **Notificaciones:** [Webhooks para Checkout Pro](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro-orders/payment-notifications)
- **Consulta de una orden:** [Obtener order por ID](https://www.mercadopago.com.ar/developers/es/reference/online-payments/checkout-pro/get-order/get)

### 4.4. Generación y validación de la credencial QR

Una vez confirmado el pago, el backend creará una credencial con un token UUID aleatorio y generará el código QR mediante la biblioteca **Nayuki QR Code Generator** (`io.nayuki:qrcodegen:1.8.0`). El QR se entregará en formato SVG y se generará bajo demanda.

El código contendrá únicamente una URL de FeriaConecta con el token de la credencial. No incluirá datos personales, información del pago ni datos de la feria. La información real permanecerá almacenada en la base de datos y será consultada por el backend al validar el token.

Durante la acreditación, el sistema comprobará que:

- El token exista y corresponda a la feria.
- La credencial se encuentre `ACTIVA`.
- La postulación esté `CONFIRMADA`.
- El pago se encuentre aprobado.
- La asistencia no haya sido registrada anteriormente.

Después de la confirmación, la credencial cambiará a `UTILIZADA`. Si se vuelve a escanear, el sistema informará que ya fue utilizada y mostrará la fecha y hora del registro original.

- **Repositorio de la biblioteca:** [Nayuki QR Code Generator](https://github.com/nayuki/QR-Code-generator)
- **Dependencia:** [io.nayuki:qrcodegen:1.8.0 en Maven Central](https://central.sonatype.com/artifact/io.nayuki/qrcodegen/1.8.0)

### 4.5. Estados principales del flujo

| Recurso | Estados contemplados |
|---|---|
| **Postulación** | `PENDIENTE` → `APROBADA_PENDIENTE_PAGO` → `CONFIRMADA`; también puede finalizar como `RECHAZADA` o `VENCIDA` |
| **Pago** | `PENDIENTE`, `APROBADO`, `RECHAZADO`, `CANCELADO` o `REEMBOLSADO` |
| **Feria** | `BORRADOR` → `PUBLICADA` → `FINALIZADA` |
| **Puesto** | `LIBRE` → `ASIGNADO` |
| **Credencial QR** | `ACTIVA` → `UTILIZADA` |

## 5. Stack tecnológico
 
| Componente | Tecnología propuesta |
|---|---|
| **Frontend** | React, Vite, TypeScript, HTML y CSS |
| **Backend** | Java 21, Spring Boot y API REST |
| **Base de datos** | MySQL con Spring Data JPA / Hibernate |
| **Seguridad** | Spring Security y JWT |
| **Pruebas y documentación** | JUnit, Mockito, Postman y Swagger / OpenAPI |
| **Despliegue** | Backend y base de datos: VPS Oracle (Docker + Portainer + Nginx Proxy Manager). Frontend: Vercel (CI/CD desde GitHub) 

## 6. Estructura del repositorio

```
/frontend      → proyecto React
/backend       → proyecto Spring Boot
/database      → scripts DDL, diagrama ER
/docs          → informes y entregas
README.md
```

## 7. Seguimiento del proyecto
 
El desarrollo se organiza en sprints mediante [GitHub Projects](https://github.com/users/isierra93/projects/2/views/1), con Issues vinculados a cada funcionalidad del alcance (sección 2) y Milestones por etapa. El historial completo de commits y decisiones técnicas queda documentado en este repositorio.

## 8. Límites y posibles mejoras

La primera versión **no** incluirá:

- Mensajería interna o chat entre usuarios.
- Mapas interactivos.
- Plano visual para distribuir los puestos.

**Mejoras posteriores:**

- Notificaciones automáticas por correo.
- Mapa interactivo con la ubicación de las ferias.
- Plano visual para organizar la distribución interna de los puestos.

---

### Resultado esperado

Un sistema funcional que permita gestionar el ciclo completo de una feria: publicación, postulación, aprobación, pago, asignación de puesto y acreditación mediante QR. De esta manera, se busca reducir las tareas manuales, evitar asignaciones o registros duplicados y conservar un historial confiable de cada evento.
