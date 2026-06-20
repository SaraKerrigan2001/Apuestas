-- ============================================================
-- PronosticosMundial — Script completo de BD
-- MySQL / MariaDB (XAMPP)
-- ============================================================
CREATE DATABASE IF NOT EXISTS mundial_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mundial_db;

-- ============================================================
-- TABLA 0: Usuarios (Auth)
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(100) UNIQUE NOT NULL,
    usuario  VARCHAR(50) UNIQUE NOT NULL,
    clave    VARCHAR(255) NOT NULL,
    saldo    DECIMAL(10,2) DEFAULT 100.00,
    activo   TINYINT(1) DEFAULT 1,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA 1: Equipos
-- ============================================================
CREATE TABLE IF NOT EXISTS equipos (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    grupo  VARCHAR(20) NOT NULL
);

-- ============================================================
-- TABLA 2: Partidos del fixture
-- ============================================================
CREATE TABLE IF NOT EXISTS partidos (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    grupo            VARCHAR(20)  NOT NULL,
    equipo_local     VARCHAR(50)  NOT NULL,
    equipo_visitante VARCHAR(50)  NOT NULL,
    cuota_local      DECIMAL(5,2) NOT NULL DEFAULT 2.50,
    cuota_empate     DECIMAL(5,2) NOT NULL DEFAULT 3.20,
    cuota_visitante  DECIMAL(5,2) NOT NULL DEFAULT 2.50,
    goles_local      INT          DEFAULT NULL,
    goles_visitante  INT          DEFAULT NULL,
    jugado           TINYINT(1)   DEFAULT 0
);

-- ============================================================
-- TABLA 3: Apostadores
-- ============================================================
CREATE TABLE IF NOT EXISTS apostadores (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50)   NOT NULL,
    saldo  DECIMAL(10,2) DEFAULT 100.00
);

-- ============================================================
-- TABLA 4: Apuestas del simulador de consola
-- ============================================================
CREATE TABLE IF NOT EXISTS apuestas (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_partido       INT           NOT NULL,
    id_apostador     INT           DEFAULT NULL,
    grupo            VARCHAR(20)   NOT NULL,
    equipo_local     VARCHAR(50)   NOT NULL,
    equipo_visitante VARCHAR(50)   NOT NULL,
    prediccion       VARCHAR(5)    NOT NULL,
    marcador_pred    VARCHAR(10)   NOT NULL,
    monto            DECIMAL(10,2) NOT NULL,
    cuota_aplicada   DECIMAL(5,2)  NOT NULL,
    resultado_real   VARCHAR(10)   DEFAULT NULL,
    premio           DECIMAL(10,2) DEFAULT 0.00,
    acerto           TINYINT(1)    DEFAULT NULL,
    fecha_apuesta    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_partido)   REFERENCES partidos(id),
    FOREIGN KEY (id_apostador) REFERENCES apostadores(id)
);

-- ============================================================
-- TABLA 5: Pronósticos app Swing
-- ============================================================
CREATE TABLE IF NOT EXISTS pronosticos (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nombre_jugador   VARCHAR(50) NOT NULL,
    equipo_local     VARCHAR(50) NOT NULL,
    equipo_visitante VARCHAR(50) NOT NULL,
    goles_local      INT         NOT NULL,
    goles_visitante  INT         NOT NULL,
    resultado_real   VARCHAR(10) DEFAULT NULL,
    acerto           TINYINT(1)  DEFAULT NULL,
    fecha_registro   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ÍNDICES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_pronosticos_jugador ON pronosticos(nombre_jugador);
CREATE INDEX IF NOT EXISTS idx_pronosticos_fecha   ON pronosticos(fecha_registro);
CREATE INDEX IF NOT EXISTS idx_apuestas_partido    ON apuestas(id_partido);
CREATE INDEX IF NOT EXISTS idx_apuestas_apostador  ON apuestas(id_apostador);
CREATE INDEX IF NOT EXISTS idx_partidos_grupo      ON partidos(grupo);
CREATE INDEX IF NOT EXISTS idx_equipos_grupo       ON equipos(grupo);

-- ============================================================
-- MIGRACIÓN (para BD existente sin columnas nuevas)
-- Ejecutar solo si ya tienes las tablas creadas previamente
-- ============================================================
-- ALTER TABLE pronosticos ADD COLUMN IF NOT EXISTS resultado_real VARCHAR(10) DEFAULT NULL;
-- ALTER TABLE pronosticos ADD COLUMN IF NOT EXISTS acerto TINYINT(1) DEFAULT NULL;

-- ============================================================
-- DATOS: 48 equipos del Mundial 2026
-- ============================================================
INSERT INTO equipos (nombre, grupo) VALUES
('México','Grupo A'),('Sudáfrica','Grupo A'),('Corea del Sur','Grupo A'),('Chequia','Grupo A'),
('Canadá','Grupo B'),('Bosnia y Herzegovina','Grupo B'),('Qatar','Grupo B'),('Suiza','Grupo B'),
('Brasil','Grupo C'),('Marruecos','Grupo C'),('Haití','Grupo C'),('Escocia','Grupo C'),
('Estados Unidos','Grupo D'),('Paraguay','Grupo D'),('Australia','Grupo D'),('Turquía','Grupo D'),
('Alemania','Grupo E'),('Curazao','Grupo E'),('Costa de Marfil','Grupo E'),('Ecuador','Grupo E'),
('Países Bajos','Grupo F'),('Japón','Grupo F'),('Suecia','Grupo F'),('Túnez','Grupo F'),
('Bélgica','Grupo G'),('Egipto','Grupo G'),('Irán','Grupo G'),('Nueva Zelanda','Grupo G'),
('España','Grupo H'),('Cabo Verde','Grupo H'),('Arabia Saudita','Grupo H'),('Uruguay','Grupo H'),
('Francia','Grupo I'),('Senegal','Grupo I'),('Irak','Grupo I'),('Noruega','Grupo I'),
('Argentina','Grupo J'),('Argelia','Grupo J'),('Austria','Grupo J'),('Jordania','Grupo J'),
('Portugal','Grupo K'),('Rep. Democrática del Congo','Grupo K'),('Uzbekistán','Grupo K'),('Colombia','Grupo K'),
('Inglaterra','Grupo L'),('Croacia','Grupo L'),('Ghana','Grupo L'),('Panamá','Grupo L');

-- ============================================================
-- DATOS: 72 partidos (6 por grupo × 12 grupos)
-- Cuotas reales aproximadas según ranking FIFA 2024
-- ============================================================

-- GRUPO A
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo A','México','Sudáfrica',1.80,3.40,4.50),
('Grupo A','Corea del Sur','Chequia',2.10,3.20,3.60),
('Grupo A','México','Corea del Sur',1.90,3.30,4.00),
('Grupo A','Sudáfrica','Chequia',2.50,3.10,2.90),
('Grupo A','México','Chequia',1.60,3.80,5.50),
('Grupo A','Corea del Sur','Sudáfrica',1.85,3.20,4.20);

-- GRUPO B
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo B','Canadá','Bosnia y Herzegovina',2.20,3.20,3.30),
('Grupo B','Qatar','Suiza',3.80,3.30,1.90),
('Grupo B','Canadá','Qatar',1.70,3.50,4.80),
('Grupo B','Bosnia y Herzegovina','Suiza',2.80,3.20,2.50),
('Grupo B','Canadá','Suiza',2.10,3.30,3.40),
('Grupo B','Bosnia y Herzegovina','Qatar',1.90,3.20,4.00);

-- GRUPO C
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo C','Brasil','Marruecos',1.55,3.80,6.00),
('Grupo C','Haití','Escocia',3.20,3.10,2.30),
('Grupo C','Brasil','Haití',1.20,6.00,12.00),
('Grupo C','Marruecos','Escocia',1.90,3.30,4.00),
('Grupo C','Brasil','Escocia',1.30,5.50,9.00),
('Grupo C','Marruecos','Haití',1.60,3.50,5.50);

-- GRUPO D
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo D','Estados Unidos','Paraguay',1.70,3.50,4.80),
('Grupo D','Australia','Turquía',2.40,3.20,2.90),
('Grupo D','Estados Unidos','Australia',1.80,3.40,4.20),
('Grupo D','Paraguay','Turquía',2.50,3.10,2.80),
('Grupo D','Estados Unidos','Turquía',1.65,3.60,5.20),
('Grupo D','Paraguay','Australia',2.60,3.10,2.70);

-- GRUPO E
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo E','Alemania','Curazao',1.15,7.00,15.00),
('Grupo E','Costa de Marfil','Ecuador',2.20,3.20,3.20),
('Grupo E','Alemania','Costa de Marfil',1.50,4.00,6.50),
('Grupo E','Curazao','Ecuador',3.50,3.20,2.00),
('Grupo E','Alemania','Ecuador',1.45,4.20,7.00),
('Grupo E','Costa de Marfil','Curazao',1.55,3.60,5.50);

-- GRUPO F
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo F','Países Bajos','Japón',1.70,3.50,4.80),
('Grupo F','Suecia','Túnez',1.90,3.20,4.00),
('Grupo F','Países Bajos','Suecia',1.75,3.40,4.50),
('Grupo F','Japón','Túnez',1.80,3.30,4.20),
('Grupo F','Países Bajos','Túnez',1.45,4.00,7.00),
('Grupo F','Japón','Suecia',2.30,3.20,3.10);

-- GRUPO G
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo G','Bélgica','Egipto',1.55,3.80,5.80),
('Grupo G','Irán','Nueva Zelanda',2.00,3.20,3.80),
('Grupo G','Bélgica','Irán',1.60,3.60,5.50),
('Grupo G','Egipto','Nueva Zelanda',1.80,3.30,4.20),
('Grupo G','Bélgica','Nueva Zelanda',1.35,5.00,8.00),
('Grupo G','Egipto','Irán',2.40,3.10,2.90);

-- GRUPO H
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo H','España','Cabo Verde',1.25,5.50,10.00),
('Grupo H','Arabia Saudita','Uruguay',2.60,3.10,2.70),
('Grupo H','España','Arabia Saudita',1.50,4.00,6.50),
('Grupo H','Cabo Verde','Uruguay',3.20,3.10,2.20),
('Grupo H','España','Uruguay',1.65,3.60,5.20),
('Grupo H','Cabo Verde','Arabia Saudita',2.80,3.10,2.50);

-- GRUPO I
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo I','Francia','Senegal',1.60,3.70,5.50),
('Grupo I','Irak','Noruega',3.20,3.10,2.20),
('Grupo I','Francia','Irak',1.25,5.50,10.00),
('Grupo I','Senegal','Noruega',2.00,3.30,3.60),
('Grupo I','Francia','Noruega',1.55,3.80,5.80),
('Grupo I','Senegal','Irak',1.70,3.40,4.80);

-- GRUPO J
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo J','Argentina','Argelia',1.35,5.00,8.50),
('Grupo J','Austria','Jordania',1.65,3.60,5.20),
('Grupo J','Argentina','Austria',1.55,3.80,5.80),
('Grupo J','Argelia','Jordania',1.80,3.30,4.20),
('Grupo J','Argentina','Jordania',1.20,6.00,12.00),
('Grupo J','Argelia','Austria',2.50,3.10,2.80);

-- GRUPO K
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo K','Portugal','Rep. Democrática del Congo',1.30,5.20,9.00),
('Grupo K','Uzbekistán','Colombia',2.80,3.10,2.50),
('Grupo K','Portugal','Uzbekistán',1.25,5.50,10.00),
('Grupo K','Rep. Democrática del Congo','Colombia',2.40,3.20,2.90),
('Grupo K','Portugal','Colombia',1.60,3.70,5.50),
('Grupo K','Rep. Democrática del Congo','Uzbekistán',2.00,3.20,3.80);

-- GRUPO L
INSERT INTO partidos (grupo,equipo_local,equipo_visitante,cuota_local,cuota_empate,cuota_visitante) VALUES
('Grupo L','Inglaterra','Croacia',1.70,3.50,4.80),
('Grupo L','Ghana','Panamá',2.10,3.20,3.60),
('Grupo L','Inglaterra','Ghana',1.50,4.00,6.50),
('Grupo L','Croacia','Panamá',1.65,3.50,5.20),
('Grupo L','Inglaterra','Panamá',1.30,5.50,9.00),
('Grupo L','Ghana','Croacia',2.60,3.10,2.70);

-- ============================================================
-- TABLA 6: Usuarios del sistema de autenticación
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo  VARCHAR(100) NOT NULL DEFAULT '',
    email            VARCHAR(100) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    usuario          VARCHAR(50)  NULL DEFAULT NULL UNIQUE,
    clave            VARCHAR(255) NULL DEFAULT NULL,
    saldo            DECIMAL(10,2) DEFAULT 100.00,
    activo           TINYINT(1)   DEFAULT 1,
    fecha_registro   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
