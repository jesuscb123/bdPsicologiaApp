-- Borra los datos existentes para empezar de cero en cada reinicio (opcional pero recomendado para pruebas)
DELETE
FROM psicologo;
DELETE
FROM usuario;

-- Inserta un usuario que será solo un paciente
INSERT INTO usuario (id, fire_base_uid, email, nombre_usuario, rol)
VALUES (1, 'uid_paciente_prueba_123', 'paciente@test.com', 'paciente_test', 'PACIENTE') ON CONFLICT (id) DO NOTHING;
-- Evita errores si el ID ya existe

-- Inserta otro usuario que será un psicólogo
INSERT INTO usuario (id, fire_base_uid, email, nombre_usuario, rol)
VALUES (2, 'uid_psicologo_prueba_456', 'psicologo@test.com', 'psicologo_test', 'PSICOLOGO') ON CONFLICT (id) DO NOTHING;

-- Inserta el perfil del psicólogo, asociándolo al usuario con id=2
INSERT INTO psicologo (id, numero_colegiado, especialidad, usuario_id)
VALUES (1, 'COL-98765', 'Terapia de Pareja', 2) ON CONFLICT (id) DO NOTHING;

-- Es buena práctica reiniciar las secuencias para que los nuevos IDs no choquen
-- La sintaxis puede variar un poco entre bases de datos, para PostgreSQL es así:
SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));
SELECT setval('psicologo_id_seq', (SELECT MAX(id) FROM psicologo));