-- Migración: convierte especialidad de columna simple a tabla de colección (@ElementCollection).
-- Crea psicologo_especialidades, migra los datos existentes y elimina la columna antigua.

CREATE TABLE psicologo_especialidades (
    psicologo_id BIGINT NOT NULL REFERENCES psicologos (id),
    especialidad VARCHAR(80) NOT NULL
);

INSERT INTO psicologo_especialidades (psicologo_id, especialidad)
SELECT id, trim(especialidad)
FROM psicologos
WHERE especialidad IS NOT NULL AND trim(especialidad) != '';

ALTER TABLE psicologos DROP COLUMN especialidad;
