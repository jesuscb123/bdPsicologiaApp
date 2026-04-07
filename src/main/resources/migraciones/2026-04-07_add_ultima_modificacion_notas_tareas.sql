-- Migración manual para Postgres (Render/producción).
-- Si usas Flyway/Liquibase en el futuro, puedes mover este archivo a su carpeta de migraciones.

ALTER TABLE NOTAS
    ADD COLUMN IF NOT EXISTS ultima_modificacion TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE TAREAS
    ADD COLUMN IF NOT EXISTS ultima_modificacion TIMESTAMP NOT NULL DEFAULT now();

-- Backfill defensivo por si existían filas con NULL (p. ej. tras cambios manuales).
UPDATE NOTAS
SET ultima_modificacion = now()
WHERE ultima_modificacion IS NULL;

UPDATE TAREAS
SET ultima_modificacion = now()
WHERE ultima_modificacion IS NULL;

