-- Alinea columnas de notas con NotaRequest (@Size asunto=100, descripcion=2000).
-- V1 creaba VARCHAR(255) para ambas; provocaba PSQLException al superar 255 en descripción.

ALTER TABLE notas
    ALTER COLUMN asunto TYPE VARCHAR(100),
    ALTER COLUMN descripcion TYPE VARCHAR(2000);
