-- Script DDL inicial de FeriaConecta (borrador)
-- Basado en diagrama-er.md. Ajustar tipos, índices y restricciones antes de usar en producción.

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL, -- ORGANIZADOR, EMPRENDEDOR
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizador_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    fecha DATE NOT NULL,
    provincia VARCHAR(100),
    municipio VARCHAR(100),
    localidad VARCHAR(100),
    calle VARCHAR(200),
    categoria_permitida VARCHAR(100),
    cupos INT NOT NULL,
    costo_participacion DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, PUBLICADA, FINALIZADA
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feria_organizador FOREIGN KEY (organizador_id) REFERENCES usuario(id)
);

CREATE TABLE postulacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feria_id BIGINT NOT NULL,
    emprendedor_id BIGINT NOT NULL,
    rubro VARCHAR(100),
    descripcion_emprendimiento TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, APROBADA_PENDIENTE_PAGO, CONFIRMADA, RECHAZADA, VENCIDA
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_postulacion_feria FOREIGN KEY (feria_id) REFERENCES feria(id),
    CONSTRAINT fk_postulacion_emprendedor FOREIGN KEY (emprendedor_id) REFERENCES usuario(id),
    CONSTRAINT uq_postulacion_feria_emprendedor UNIQUE (feria_id, emprendedor_id)
);

CREATE TABLE pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id BIGINT NOT NULL UNIQUE,
    mp_order_id VARCHAR(100),
    external_reference VARCHAR(100) NOT NULL UNIQUE,
    monto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, APROBADO, RECHAZADO, CANCELADO, REEMBOLSADO
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
);

CREATE TABLE credencial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id BIGINT NOT NULL UNIQUE,
    token_uuid VARCHAR(36) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, UTILIZADA
    utilizada_en DATETIME NULL,
    CONSTRAINT fk_credencial_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
);

CREATE TABLE puesto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feria_id BIGINT NOT NULL,
    postulacion_id BIGINT NULL,
    codigo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'LIBRE', -- LIBRE, ASIGNADO
    CONSTRAINT fk_puesto_feria FOREIGN KEY (feria_id) REFERENCES feria(id),
    CONSTRAINT fk_puesto_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id),
    CONSTRAINT uq_puesto_feria_codigo UNIQUE (feria_id, codigo)
);
