# APUNTES PROPIOS (NO SUBIR A GITHUB)

Este fichero es para mí, y **no debería subirse** al repo.

## Conexión a PostgreSQL (Render) desde Windows (PowerShell / CMD)

### Datos que necesito (Render → PostgreSQL → Connections)
- **Host**
- **Port** (normalmente `5432`)
- **Database**
- **User**
- **Password**
- (Opcional) **External Database URL**

> Render suele requerir SSL. Si falla la conexión, usar `sslmode=require`.

## Formas de entrar a `psql`

### A) Entrar y que pida la contraseña (recomendado)

```powershell
psql "postgresql://USUARIO@HOST:5432/BD?sslmode=require"
```

Ejemplo (sin contraseña en la URL):

```powershell
psql "postgresql://jesus@dpg-xxxxx.frankfurt-postgres.render.com:5432/bdpsicologia?sslmode=require"
```

### B) Entrar usando `PGPASSWORD` (no deja rastro en el comando, pero sí en la sesión)

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -h HOST -p 5432 -U USUARIO -d BD
Remove-Item Env:PGPASSWORD
```

## Ejecutar una migración `.sql` desde CMD/PowerShell

### Ejecutar y parar al primer error

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 "postgresql://USUARIO@HOST:5432/BD?sslmode=require" -f "RUTA\\AL\\FICHERO.sql"
Remove-Item Env:PGPASSWORD
```

Ejemplo (ruta absoluta al script de migración en este proyecto):

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 "postgresql://USUARIO@HOST:5432/BD?sslmode=require" -f "C:\Users\jesus\Documents\DAM2\TFG\bdPsicologiaApp\bdPsicologiaApp\src\main\resources\migraciones\2026-04-07_add_nombre_apellidos_descripcion_psicologo.sql"
Remove-Item Env:PGPASSWORD
```

### Ejecutar mostrando lo que se está ejecutando (útil para depurar)

```powershell
$env:PGPASSWORD="TU_PASSWORD"
psql -v ON_ERROR_STOP=1 -e -a "postgresql://USUARIO@HOST:5432/BD?sslmode=require" -f "RUTA\\AL\\FICHERO.sql"
Remove-Item Env:PGPASSWORD
```

Notas:
- `-v ON_ERROR_STOP=1`: para al primer fallo y devuelve exit code ≠ 0.
- `-e`: muestra los comandos según se ejecutan.
- `-a`: hace echo de todo el input.

## Ejecutar un `.sql` desde dentro de `psql`

1) Entrar a `psql`.
2) Importar el fichero:

```sql
\i 'C:/Users/jesus/Documents/DAM2/TFG/bdPsicologiaApp/bdPsicologiaApp/src/main/resources/migraciones/2026-04-07_add_nombre_apellidos_descripcion_psicologo.sql'
```

(En `\i` suele ir mejor la ruta con `/` aunque estés en Windows.)

## Comprobar que la migración se aplicó

### Confirmar BD/usuario actuales

```sql
SELECT current_database(), current_user;
```

### Ver columnas (rápido)

En Postgres, si tus tablas se crearon con comillas y mayúsculas, usa:

```sql
\d "USUARIOS"
\d "PSICOLOGOS"
```

Si están en minúsculas sin comillas:

```sql
\d usuarios
\d psicologos
```

### Consultas de verificación con `information_schema`

```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'usuarios'
  AND column_name IN ('nombre', 'apellidos');

SELECT column_name, data_type, is_nullable, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'psicologos'
  AND column_name = 'descripcion';
```

## Buenas prácticas (importante)
- **No pegar URLs con credenciales** en chats, commits ni capturas.
- Si accidentalmente se filtra una password/URL:
  - **Reset password** del Postgres en Render.
  - Actualizar las variables/URLs donde se usen.
- Para no escribir la contraseña cada vez, alternativa: `pgpass.conf` (Windows) en:
  - `%APPDATA%\postgresql\pgpass.conf`
  - Formato: `host:puerto:bd:usuario:password`

## 2026-04-15 — Backend: sección de Citas (bdPsicologiaApp)

### Qué se ha añadido
- **Dominio**:
  - `domain/Cita.kt`: entidad JPA `Cita` con `psicologo`, `paciente`, `inicio` (UTC), `duracionMinutos` (60) y `estado`.
  - `domain/EstadoCita.kt`: enum para el estado persistido (p.ej. `RESERVADA`, `CANCELADA`).
- **Persistencia**:
  - `repository/CitaRepository.kt`: consultas para listar citas del paciente/psicólogo y para comprobar huecos ocupados por psicólogo en un rango.
  - `resources/migraciones/2026-04-15_add_citas.sql`: migración para crear la tabla de citas y el **índice único** para evitar doble reserva por `(psicologo_id, inicio)`.
- **Servicio**:
  - `service/IServicioCita.kt` y `service/ServicioCita.kt`:
    - cálculo de disponibilidad (L–V, 09:00–17:00, slots 1h) en base a `zonaHoraria` (IANA),
    - validaciones (día laborable, hora exacta, etc.),
    - reserva atómica (si se intenta reservar un slot ya ocupado → conflicto),
    - cancelación: marcar `CANCELADA` y liberar hueco.
- **Web/API**:
  - `web/CitaController.kt`: endpoints para disponibilidad, crear cita, listar mis citas (paciente) y listar mis citas como psicólogo, y cancelar cita.
  - `web/dto/citaDTO/*`: request/response y disponibilidad.
  - `web/mapper/CitaMapper.kt`: mapeos entidad ↔ DTO (incluyendo estado calculado “activa/finalizada” a partir de `inicio + duración`, si aplica en la response).

### Notas de comportamiento
- **No doble reserva**: garantizada por constraint único en BD + manejo del conflicto en la capa web.
- **Cancelación**: una cita `CANCELADA` deja el slot disponible de nuevo.

