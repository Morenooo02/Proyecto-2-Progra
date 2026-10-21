-- Sistema de Reserva de Recursos
-- Esquema inicial de base de datos para MySQL 8+
-- Este script crea la estructura; no migra datos desde datos.xml.

CREATE DATABASE IF NOT EXISTS reservas_recursos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE reservas_recursos;

-- 1. Usuarios: credenciales y rol de acceso.
CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(50) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_usuarios_rol
      CHECK (rol IN ('ADMINISTRADOR', 'FUNCIONARIO'))
) ENGINE=InnoDB;

-- 2. Funcionarios: cada funcionario tiene una cuenta con el mismo ID.
-- Los usuarios administradores pueden existir sin un registro en funcionarios.
CREATE TABLE IF NOT EXISTS funcionarios (
    id VARCHAR(50) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_funcionarios_usuario
      FOREIGN KEY (id) REFERENCES usuarios(id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. Categorías de recursos.
CREATE TABLE IF NOT EXISTS categorias (
    id VARCHAR(50) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 4. Recursos: cada recurso pertenece a una categoría.
CREATE TABLE IF NOT EXISTS recursos (
    id VARCHAR(50) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    categoria_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_recursos_categoria (categoria_id),
    CONSTRAINT fk_recursos_categoria
      FOREIGN KEY (categoria_id) REFERENCES categorias(id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. Reservas: cada reserva pertenece a un funcionario.
CREATE TABLE IF NOT EXISTS reservas (
    id VARCHAR(100) NOT NULL,
    actividad VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    funcionario_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_reservas_funcionario (funcionario_id),
    INDEX idx_reservas_fecha_horas (fecha, hora_inicio, hora_fin),
    CONSTRAINT fk_reservas_funcionario
      FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT,
    CONSTRAINT chk_reservas_estado
      CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    CONSTRAINT chk_reservas_horas
      CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;

-- Tabla intermedia: una reserva puede asignar varios recursos,
-- y un recurso puede participar en distintas reservas en fechas/horarios distintos.
CREATE TABLE IF NOT EXISTS reserva_recurso (
    reserva_id VARCHAR(100) NOT NULL,
    recurso_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (reserva_id, recurso_id),
    INDEX idx_reserva_recurso_recurso (recurso_id),
    CONSTRAINT fk_reserva_recurso_reserva
      FOREIGN KEY (reserva_id) REFERENCES reservas(id)
      ON UPDATE CASCADE
      ON DELETE CASCADE,
    CONSTRAINT fk_reserva_recurso_recurso
      FOREIGN KEY (recurso_id) REFERENCES recursos(id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Tabla intermedia: conserva las categorías solicitadas originalmente
-- por una reserva, además de los recursos que finalmente se asignaron.
CREATE TABLE IF NOT EXISTS reserva_categoria (
    reserva_id VARCHAR(100) NOT NULL,
    categoria_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (reserva_id, categoria_id),
    INDEX idx_reserva_categoria_categoria (categoria_id),
    CONSTRAINT fk_reserva_categoria_reserva
      FOREIGN KEY (reserva_id) REFERENCES reservas(id)
      ON UPDATE CASCADE
      ON DELETE CASCADE,
    CONSTRAINT fk_reserva_categoria_categoria
      FOREIGN KEY (categoria_id) REFERENCES categorias(id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Comprobación rápida del esquema creado:
SHOW TABLES;
