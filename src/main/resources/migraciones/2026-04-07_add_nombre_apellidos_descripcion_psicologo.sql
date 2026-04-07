-- Migración manual para Postgres (Render/producción).
-- Añade nombre/apellidos en usuarios, backfill desde nombreUsuario, y añade descripción en psicólogos.
-- También elimina/relaja constraints de unicidad asociadas a nombreUsuario y elimina la columna antigua si existe.

DO $$
DECLARE
    tabla_usuarios_ident TEXT;
    tabla_usuarios_nombre TEXT;
    tabla_psicologos_ident TEXT;
    tabla_psicologos_nombre TEXT;
    tiene_nombre_usuario BOOLEAN := FALSE;
    tiene_nombreUsuario BOOLEAN := FALSE;
    r RECORD;
BEGIN
    -- Resolver nombres reales de tabla (por si existen como "USUARIOS"/"PSICOLOGOS" o como usuarios/psicologos).
    IF to_regclass('public."USUARIOS"') IS NOT NULL THEN
        tabla_usuarios_ident := '"USUARIOS"';
        tabla_usuarios_nombre := 'USUARIOS';
    ELSIF to_regclass('public.usuarios') IS NOT NULL THEN
        tabla_usuarios_ident := 'usuarios';
        tabla_usuarios_nombre := 'usuarios';
    ELSE
        RAISE NOTICE 'Tabla de usuarios no encontrada (USUARIOS/usuarios). Se omite migración de usuarios.';
    END IF;

    IF to_regclass('public."PSICOLOGOS"') IS NOT NULL THEN
        tabla_psicologos_ident := '"PSICOLOGOS"';
        tabla_psicologos_nombre := 'PSICOLOGOS';
    ELSIF to_regclass('public.psicologos') IS NOT NULL THEN
        tabla_psicologos_ident := 'psicologos';
        tabla_psicologos_nombre := 'psicologos';
    ELSE
        RAISE NOTICE 'Tabla de psicólogos no encontrada (PSICOLOGOS/psicologos). Se omite migración de psicólogos.';
    END IF;

    -- USUARIOS: añadir columnas nuevas y backfill desde nombreUsuario/nombre_usuario si existe.
    IF tabla_usuarios_ident IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS nombre VARCHAR(255)', tabla_usuarios_ident);
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS apellidos VARCHAR(255)', tabla_usuarios_ident);

        SELECT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = tabla_usuarios_nombre
              AND column_name = 'nombre_usuario'
        ) INTO tiene_nombre_usuario;

        SELECT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = tabla_usuarios_nombre
              AND column_name = 'nombreUsuario'
        ) INTO tiene_nombreUsuario;

        IF tiene_nombre_usuario THEN
            EXECUTE format(
                'UPDATE %s SET nombre = COALESCE(nombre, nombre_usuario) WHERE nombre IS NULL',
                tabla_usuarios_ident
            );
        ELSIF tiene_nombreUsuario THEN
            EXECUTE format(
                'UPDATE %s SET nombre = COALESCE(nombre, "nombreUsuario") WHERE nombre IS NULL',
                tabla_usuarios_ident
            );
        END IF;

        EXECUTE format(
            'UPDATE %s SET apellidos = COALESCE(apellidos, '''') WHERE apellidos IS NULL',
            tabla_usuarios_ident
        );

        -- Asegurar NOT NULL (siempre que exista al menos algún valor para nombre).
        EXECUTE format('ALTER TABLE %s ALTER COLUMN nombre SET NOT NULL', tabla_usuarios_ident);
        EXECUTE format('ALTER TABLE %s ALTER COLUMN apellidos SET NOT NULL', tabla_usuarios_ident);

        -- Eliminar constraints UNIQUE que dependan de nombre_usuario / nombreUsuario (si existen).
        FOR r IN
            SELECT c.conname
            FROM pg_constraint c
            JOIN pg_class t ON t.oid = c.conrelid
            WHERE t.relname = tabla_usuarios_nombre
              AND c.contype = 'u'
              AND EXISTS (
                  SELECT 1
                  FROM unnest(c.conkey) AS k(attnum)
                  JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
                  WHERE a.attname IN ('nombre_usuario', 'nombreUsuario')
              )
        LOOP
            EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', tabla_usuarios_nombre, r.conname);
        END LOOP;

        -- Eliminar índices UNIQUE sobre nombre_usuario / nombreUsuario (si existen).
        FOR r IN
            SELECT i.relname AS indexname
            FROM pg_class t
            JOIN pg_index ix ON t.oid = ix.indrelid
            JOIN pg_class i ON i.oid = ix.indexrelid
            JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey)
            WHERE t.relname = tabla_usuarios_nombre
              AND ix.indisunique = TRUE
              AND a.attname IN ('nombre_usuario', 'nombreUsuario')
        LOOP
            EXECUTE format('DROP INDEX IF EXISTS %I', r.indexname);
        END LOOP;

        -- Retirar columna antigua si existe (dejando a nombre/apellidos como fuente de verdad).
        IF tiene_nombre_usuario THEN
            EXECUTE format('ALTER TABLE %s DROP COLUMN IF EXISTS nombre_usuario', tabla_usuarios_ident);
        END IF;
        IF tiene_nombreUsuario THEN
            EXECUTE format('ALTER TABLE %s DROP COLUMN IF EXISTS "nombreUsuario"', tabla_usuarios_ident);
        END IF;
    END IF;

    -- PSICOLOGOS: añadir descripción opcional.
    IF tabla_psicologos_ident IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS descripcion VARCHAR(1000)', tabla_psicologos_ident);
    END IF;
END $$;

