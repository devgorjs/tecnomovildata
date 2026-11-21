-- TecnoMovil Data - esquema para persistir las simulaciones y sus registros.
-- Ejecutar el archivo completo desde MySQL Workbench.

CREATE DATABASE IF NOT EXISTS tecnomovil_data
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tecnomovil_data;

CREATE TABLE IF NOT EXISTS simulaciones (
    id_simulacion BIGINT NOT NULL AUTO_INCREMENT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_usuarios INT NOT NULL,
    total_registros INT NOT NULL,
    semilla BIGINT NOT NULL,
    PRIMARY KEY (id_simulacion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS registros (
    id_registro BIGINT NOT NULL AUTO_INCREMENT,
    id_simulacion BIGINT NOT NULL,
    id_usuario VARCHAR(20) NOT NULL,
    ruta VARCHAR(10) NOT NULL,
    estacion VARCHAR(100) NOT NULL,
    accion ENUM('ENTRADA', 'SALIDA') NOT NULL,
    fecha_evento DATETIME NOT NULL,
    PRIMARY KEY (id_registro),
    CONSTRAINT fk_registros_simulaciones
        FOREIGN KEY (id_simulacion)
        REFERENCES simulaciones (id_simulacion)
        ON DELETE CASCADE,
    CONSTRAINT uq_simulacion_usuario_accion
        UNIQUE (id_simulacion, id_usuario, accion),
    INDEX idx_registros_simulacion_fecha (id_simulacion, fecha_evento),
    INDEX idx_registros_simulacion_ruta (id_simulacion, ruta),
    INDEX idx_registros_simulacion_usuario (id_simulacion, id_usuario)
) ENGINE=InnoDB;

-- Verificación básica del esquema creado.
SHOW TABLES;
