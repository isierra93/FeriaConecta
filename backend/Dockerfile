# Imagen del backend de FeriaConecta (Spring Boot, Java 21).
# Build (desde backend/): docker build -t feriaconecta-backend .
#
# El VPS de Oracle es ARM (arm64). Las imagenes base son multi-arquitectura, asi que
# basta con construir en el propio VPS, o desde una PC x86 con:
#   docker buildx build --platform linux/arm64 -t feriaconecta-backend .

# --- ETAPA 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos solo el pom.xml primero para aprovechar la cache de Docker
COPY pom.xml .
# Descargamos dependencias (queda en cache mientras no cambie el pom)
RUN mvn -B dependency:go-offline

# Copiamos el codigo fuente y compilamos
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- ETAPA 2: Run ---
# JRE 21 sobre Alpine (liviana)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# El contenedor corre con un usuario sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring

# Copiamos SOLO el JAR desde la etapa "build"
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

# La configuracion llega por variables de entorno (Portainer / docker run -e).
# El .env local nunca entra a la imagen (ver .dockerignore).
ENV SPRING_PROFILES_ACTIVE=prod
# Usa como maximo el 75% de la memoria asignada al contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
