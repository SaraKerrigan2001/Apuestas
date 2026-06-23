-- Actualizar usuarios con nombres correctos y roles
SET NAMES utf8mb4;

UPDATE usuarios 
SET nombre_completo = 'María Paula', es_admin = 0 
WHERE email = 'paitocapacho@gmail.com';

UPDATE usuarios 
SET nombre_completo = 'José Vera', es_admin = 1 
WHERE email = 'josevera@gmail.com';

-- Reinsertar equipos y datos principales si están vacíos
INSERT IGNORE INTO equipos (nombre, grupo) VALUES
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
('Colombia','Grupo K'),('Rep. Democrática del Congo','Grupo K'),('Portugal','Grupo K'),('Uzbekistán','Grupo K'),
('Inglaterra','Grupo L'),('Croacia','Grupo L'),('Ghana','Grupo L'),('Panamá','Grupo L');

SELECT id, nombre_completo, email, es_admin, activo FROM usuarios;
SELECT COUNT(*) AS equipos FROM equipos;
