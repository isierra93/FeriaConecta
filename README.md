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

> **Idea principal:** centralizar en un solo sistema la creación de ferias, la inscripción de emprendedores, la asignación de puestos y el control de asistencia.

---

## 1. Problema y objetivo

La organización de ferias suele realizarse mediante formularios, planillas, mensajes de WhatsApp y anotaciones separadas. Esto dificulta controlar los cupos, saber qué emprendedores fueron aprobados, asignar puestos sin errores y conservar un historial de cada evento.

FeriaConecta busca ordenar ese proceso y facilitar la comunicación entre organizadores y emprendedores. También ofrecerá una vista pública para que cualquier persona pueda consultar las próximas ferias y los emprendimientos participantes.

**Usuarios:** organizador o administrador, emprendedor y público general.

## 2. Alcance de la primera versión

1. Registro e inicio de sesión con roles y permisos.
2. Perfiles de emprendedores con datos de contacto, descripción y rubro.
3. Gestión de ferias: fechas, ubicación, cupos, categorías y estado.
4. Postulación de emprendedores y evaluación por parte del organizador.
5. Creación de puestos y asignación de cada espacio a un participante aprobado.
6. Listado público de ferias y estadísticas básicas para el organizador.
7. Validación, normalización y asistencia para el autocompletado de direcciones mediante la API GeoRef.

## 3. Flujo principal de funcionamiento

1. El organizador crea una feria y define sus datos, rubros permitidos y cantidad de puestos.
2. El emprendedor completa su perfil y envía una postulación.
3. El organizador revisa la solicitud y la aprueba o rechaza.
4. A los emprendimientos aprobados se les asigna un puesto.
5. El emprendedor confirma su participación y el organizador registra la asistencia.
6. La información queda guardada para consultar el historial y obtener estadísticas.

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

Se utilizará **GeoRef**, el Servicio de Normalización de Datos Geográficos de Argentina, como API REST externa. Su función será asistir el autocompletado y validar o normalizar las ubicaciones ingresadas al crear una feria.

La integración podrá consultar provincias, municipios, localidades, calles y direcciones. La comunicación con GeoRef se realizará desde el backend para centralizar su uso y evitar que el frontend dependa directamente del servicio externo.

- **Documentación oficial:** [Referencia completa de la API GeoRef V2](https://www.argentina.gob.ar/georef/referencia-completa-de-la-api-georef-v-2)
- **URL base utilizada por la aplicación:** `https://apis.datos.gob.ar/georef/api/v2.0/`

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

- Pagos en línea
- Mensajería automática
- Mapas interactivos

**Mejoras posteriores:**

- Código QR para validar la asistencia
- Notificaciones por correo
- Plano visual de los puestos

---

### Resultado esperado

Un sistema funcional que reduzca tareas manuales, evite asignaciones duplicadas y permita seguir el estado completo de cada feria.
