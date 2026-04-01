# APUNTES (2026-03-31)

## Contexto / objetivo del día

Objetivo principal: permitir que **un mismo `firebaseUid`** pueda tener **rol de Paciente y/o Psicólogo** sin recrear la entidad `Usuario`.

- **Alta inicial**: `POST /api/usuarios` crea `Usuario` (solo una vez).
- **Alta de rol adicional** usando el usuario autenticado:
  - `POST /api/pacientes/me`
  - `POST /api/psicologos/me`
- Comportamiento buscado:
  - Si no existe `Usuario` para ese `firebaseUid`: **409** indicando que primero hay que crear el usuario con `POST /api/usuarios`.
  - Si el rol ya existe: **409 Conflict**.
  - Un `firebaseUid` puede acabar con ambos roles; `ServicioRoles` devuelve ambos (`ROLE_PACIENTE`, `ROLE_PSICOLOGO`).

## Cambios realizados (qué, dónde y por qué)

### 1) `FirebaseConfig` solo fuera de `dev`

Archivo: `src/main/kotlin/.../config/FirebaseConfig.kt`

- **Qué se cambió**: se añadió `@Profile("!dev")` a la configuración.
- **Por qué**: en desarrollo local (`dev`) no siempre existe `FIREBASE_CREDENTIALS`. Al arrancar sin esa variable, la app fallaba en local. En Render (producción) sí se espera que exista.

### 2) Token “de desarrollo” para Swagger/local

Archivo: `src/main/kotlin/.../service/FirebaseService.kt`

- **Qué se cambió**: se añadió un atajo para el perfil `dev` que acepta tokens con formato:
  - `Authorization: Bearer dev:<uid>:<email>`
- **Por qué**: poder autenticar llamadas desde Swagger UI local sin depender de Firebase real.

**Nota práctica**: si en Swagger introduces mal el token (por ejemplo `Bearer Bearer dev:...`), el backend lo interpreta como inválido y devuelve **401**.

### 3) 401 cuando falta autenticación (en vez de 403)

Archivo: `src/main/kotlin/.../security/SecurityConfig.kt`

- **Qué se cambió**:
  - se deshabilitó `httpBasic`, `formLogin`, `logout`
  - se configuró `authenticationEntryPoint` para devolver **401 UNAUTHORIZED**
- **Por qué**: para APIs con Bearer tokens, el comportamiento esperado “sin token” suele ser **401**.

### 4) Perfil `dev` con H2 + Swagger habilitado

Archivo: `src/main/resources/application-dev.yaml`

- **Qué se cambió**:
  - `springdoc.api-docs.enabled: true` y `springdoc.swagger-ui.enabled: true`
  - se configuró datasource **H2 en memoria**
  - `spring.sql.init.mode: never` (para evitar que `data.sql` rompa al no existir tablas al inicio)
- **Por qué**:
  - Swagger solo se quería exponer en desarrollo.
  - Poder arrancar en local sin Postgres/Docker.
  - Evitar fallo al arrancar por scripts de inicialización.

### 5) Dependencia H2 para desarrollo

Archivo: `build.gradle.kts`

- **Qué se cambió**: se añadió `runtimeOnly("com.h2database:h2")`.
- **Por qué**: el perfil `dev` usa H2, así que necesita el driver.

## Problemas encontrados y cómo se resolvieron

### Render fallaba al arrancar mostrando pistas de H2

- **Síntoma**: en logs de Render aparecía el warning de Hibernate sobre `H2Dialect`, señal de que se estaba ejecutando con configuración de `dev`.
- **Causa probable**: variable de entorno `SPRING_PROFILES_ACTIVE=dev` configurada en Render.
- **Solución**:
  - En Render: **eliminar** `SPRING_PROFILES_ACTIVE=dev`.
  - Mantener en Render las variables de producción:
    - `SPRING_DATASOURCE_URL` (jdbc:postgresql://...)
    - `SPRING_DATASOURCE_USERNAME`
    - `SPRING_DATASOURCE_PASSWORD`
    - `FIREBASE_CREDENTIALS` (JSON service account, si se usa Firebase en prod)

### La app no arrancaba en local por DB / Firebase

- **Síntoma**: fallos al iniciar por ausencia de `SPRING_DATASOURCE_URL` y/o `FIREBASE_CREDENTIALS`.
- **Solución**: perfil `dev` con H2 + desactivar `FirebaseConfig` en `dev` + token `dev:*`.

### `Port 8080 was already in use`

- **Síntoma**: al hacer `bootRun`, Spring no podía arrancar.
- **Solución**:
  - identificar PID con `netstat -ano | findstr :8080`
  - matar proceso con `taskkill /PID <PID> /F`
  - alternativa: arrancar con otro puerto (ver instrucciones abajo).

### 401 al llamar endpoints desde Swagger

- **Síntoma**: `401` con `content-length: 0`.
- **Causa**: cabecera `Authorization` mal formada (ej. `Bearer Bearer dev:...`).
- **Solución**: usar exactamente `Authorization: Bearer dev:<uid>:<email>`.

### 409 al “volver a crear usuario”

- **Síntoma**: al repetir `POST /api/usuarios`, devuelve `409`.
- **Causa**: `POST /api/usuarios` solo es para **alta inicial**; si ya existe el `firebaseUid`, debe fallar.
- **Solución**: para añadir un rol nuevo al mismo usuario, usar `POST /api/pacientes/me` o `POST /api/psicologos/me`.

## Instrucciones: probar Swagger en local (perfil `dev`)

### 1) Arrancar el backend en `dev`

En PowerShell, dentro del proyecto:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\gradlew.bat bootRun
```

Si el puerto 8080 está ocupado:

- Opción A: liberar el puerto

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

- Opción B: usar otro puerto (ej. 8081)

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:PORT="8081"
.\gradlew.bat bootRun
```

### 2) Abrir Swagger UI

- Si arrancas en 8080: `http://localhost:8080/swagger-ui/index.html`
- Si arrancas en 8081: `http://localhost:8081/swagger-ui/index.html`

### 3) Token para Swagger en local (sin Firebase real)

En Swagger → botón **Authorize** → pegar:

- `Bearer dev:uid1:a@b.com`

Formato general:

- `Bearer dev:<uid>:<email>`

### 4) Comprobaciones recomendadas

- **Sin token**: endpoints protegidos deben devolver **401**.
- **Con token**:
  - `POST /api/usuarios` crea usuario (201) y repetir da 409.
  - `POST /api/psicologos/me` o `POST /api/pacientes/me` crea el rol faltante (201) y repetir da 409.
  - Validar coexistencia llamando a endpoints con `@PreAuthorize` de ambos roles.

## Instrucciones: obtener un token real de Firebase desde PowerShell (producción)

Hay varias formas; la más común sin frontend es usar el **endpoint REST** de Identity Toolkit.

### Opción 1 (email/password): `signInWithPassword`

Requisitos:
- **API key** de Firebase Web (`FIREBASE_WEB_API_KEY`)
- Un usuario en Firebase Auth con **email** y **password**

PowerShell:

```powershell
$apiKey = "<TU_FIREBASE_WEB_API_KEY>"
$email = "<EMAIL>"
$password = "<PASSWORD>"

$body = @{
  email = $email
  password = $password
  returnSecureToken = $true
} | ConvertTo-Json

$res = Invoke-RestMethod -Method Post `
  -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey" `
  -ContentType "application/json" `
  -Body $body

$idToken = $res.idToken
$idToken
```

Ese `$idToken` es el que debes enviar como:

`Authorization: Bearer <idToken>`

### Opción 2 (desde tu frontend/app)

Si tu frontend (Angular) ya hace login con Firebase, en la consola del navegador puedes obtener el token con:

- `await firebase.auth().currentUser.getIdToken()` (v8)
- `await getIdToken(auth.currentUser)` (v9 modular)

> Esta opción depende de cómo esté montado el frontend.

## Notas para email/password (por si falla)

- Asegúrate de tener habilitado el provider **Email/Password** en Firebase Console → **Authentication** → **Sign-in method**.
- La **Web API Key** se obtiene en Firebase Console → **Project settings** → **General** → sección **Your apps** (config web).
- Si el endpoint devuelve error tipo `INVALID_PASSWORD`, `EMAIL_NOT_FOUND` o `USER_DISABLED`, revisa credenciales/estado del usuario.

