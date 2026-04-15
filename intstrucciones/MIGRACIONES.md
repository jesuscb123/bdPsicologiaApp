# Migraciones (manuales) — cómo aplicarlas por comandos (Opción A)

En este proyecto las migraciones SQL están en:

- `src/main/resources/migraciones/`

Actualmente se aplican **de forma manual** contra la base de datos Postgres (por ejemplo, Render).

## Requisitos

- Tener instalado **PostgreSQL client tools** (para disponer del comando `psql`).
- Tener a mano los datos de conexión de Render:
  - **Host**
  - **Port** (normalmente `5432`)
  - **Database**
  - **User**
  - **Password**

> En Render normalmente necesitas SSL. En ese caso usa `sslmode=require`.

## Opción A (recomendada): PowerShell ejecutando el fichero con `psql`

### 1) Localiza el fichero de migración

Ejemplos:

- `src/main/resources/migraciones/2026-04-15_add_citas.sql`
- `src/main/resources/migraciones/2026-04-07_add_ultima_modificacion_notas_tareas.sql`

### 2) Ejecuta el comando

Copia este comando y reemplaza `TU_PASSWORD`, `USUARIO`, `HOST` y `BD`:

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 "postgresql://USUARIO@HOST:5432/BD?sslmode=require" -f "C:\Users\jesus\Documents\DAM2\TFG\bdPsicologiaApp\bdPsicologiaApp\src\main\resources\migraciones\2026-04-15_add_citas.sql"
Remove-Item Env:PGPASSWORD
```

### Qué hace cada parte

- `PGPASSWORD`: evita que `psql` te pida la contraseña interactivamente.
- `-v ON_ERROR_STOP=1`: **para al primer error** (clave para no dejar cambios a medias).
- `-f "RUTA_AL_SQL"`: ejecuta el fichero `.sql`.
- `sslmode=require`: fuerza SSL (habitual en Render).

## Alternativa más fácil (Render): usar la `External Database URL`

En Render (PostgreSQL → Connections) tienes la **External Database URL**. Puedes usarla directamente en `psql` sin separar `USUARIO@HOST`:

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 "EXTERNAL_DATABASE_URL?sslmode=require" -f "RUTA\\AL\\FICHERO.sql"
Remove-Item Env:PGPASSWORD
```

Notas:
- Si la URL ya trae `?sslmode=require`, **no lo dupliques**.
- Si la URL ya incluye contraseña, **no hace falta** `PGPASSWORD` (y evita pegar URLs con credenciales en chats/commits).

### (Opcional) Ver lo que se ejecuta (modo depuración)

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 -e -a "postgresql://USUARIO@HOST:5432/BD?sslmode=require" -f "RUTA\\AL\\FICHERO.sql"
Remove-Item Env:PGPASSWORD
```

## Recomendación de orden al desplegar

- 1) **Aplicar migración** en la BD (Render)
- 2) **Desplegar** el backend actualizado
- 3) **Probar** endpoints

Así evitas que el backend nuevo intente usar tablas/columnas que todavía no existen.

## Verificación rápida (opcional)

Después de ejecutar, puedes entrar con `psql` y comprobar tablas/columnas:

```sql
SELECT current_database(), current_user;
```

