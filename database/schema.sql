-- Script DDL inicial de FeriaConecta
-- Basado en database/diagrama-er.md y docs/07-diagrama-de-clases.md

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE usuario_rol (
    usuario_id INT NOT NULL,
    rol VARCHAR(20) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ORGANIZADOR','EMPRENDEDOR'))
);

-- Regla (app): una feria requiere >= 1 puesto para pasar a PUBLICADA.
CREATE TABLE feria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organizador_id INT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    fecha DATE NOT NULL,
    categoria_permitida VARCHAR(100) NOT NULL,
    costo_participacion DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_feria_organizador_nombre_fecha UNIQUE (organizador_id, nombre, fecha),
    CONSTRAINT fk_feria_organizador FOREIGN KEY (organizador_id) REFERENCES usuario(id),
    CONSTRAINT chk_feria_estado CHECK (estado IN ('BORRADOR','PUBLICADA','EN_CURSO','FINALIZADA','CANCELADA'))
);

-- Regla (app): toda feria debe tener exactamente una ubicacion (1-1 obligatorio del ER); el DDL no lo fuerza.
CREATE TABLE ubicacion (
    feria_id INT PRIMARY KEY,
    provincia VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    localidad VARCHAR(100) NOT NULL,
    calle VARCHAR(200) NOT NULL,
    altura VARCHAR(20) NOT NULL,
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    CONSTRAINT fk_ubicacion_feria FOREIGN KEY (feria_id) REFERENCES feria(id)
);

CREATE TABLE postulacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    feria_id INT NOT NULL,
    emprendedor_id INT NOT NULL,
    rubro VARCHAR(100) NOT NULL,
    descripcion_emprendimiento TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_postulacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_postulacion_feria FOREIGN KEY (feria_id) REFERENCES feria(id),
    CONSTRAINT fk_postulacion_emprendedor FOREIGN KEY (emprendedor_id) REFERENCES usuario(id),
    CONSTRAINT uq_postulacion_feria_emprendedor UNIQUE (feria_id, emprendedor_id),
    CONSTRAINT chk_postulacion_estado CHECK (estado IN ('PENDIENTE','APROBADA_PENDIENTE_PAGO','CONFIRMADA','RECHAZADA','VENCIDA'))
);

CREATE TABLE pago (
    postulacion_id INT PRIMARY KEY,
    monto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha DATETIME,
    referencia_externa VARCHAR(100) NOT NULL,
    id_orden_externo VARCHAR(100),
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pago_referencia_externa UNIQUE (referencia_externa),
    CONSTRAINT fk_pago_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id),
    CONSTRAINT chk_pago_estado CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO','CANCELADO','REEMBOLSADO'))
);

CREATE TABLE credencial (
    postulacion_id INT PRIMARY KEY,
    token VARCHAR(36) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_uso DATETIME,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_credencial_token UNIQUE (token),
    CONSTRAINT fk_credencial_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id),
    CONSTRAINT chk_credencial_estado CHECK (estado IN ('ACTIVA','UTILIZADA','EXPIRADA'))
);

-- Reglas (app): el puesto y su postulacion deben ser de la misma feria, y la postulacion debe estar CONFIRMADA; el DDL no lo fuerza.
CREATE TABLE puesto (
    feria_id INT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    postulacion_id INT,
    creado_en DATETIME DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (feria_id, codigo),
    CONSTRAINT uq_puesto_postulacion UNIQUE (postulacion_id),
    CONSTRAINT fk_puesto_feria FOREIGN KEY (feria_id) REFERENCES feria(id),
    CONSTRAINT fk_puesto_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
);
