# bdPsicologiaApp — API REST (Backend)

Servidor **Spring Boot 3** del ecosistema **PsicologíaApp** (TFG DAM2). Expone una API REST bajo `/api` para la aplicación Android **acompañame**, con persistencia relacional, autenticación **Firebase**, notificaciones **FCM**, chat coordinado y funciones de **IA** (resumen de notas y detección de riesgo) mediante **Groq**.

<p align="center">
  <strong>Cliente ↔ API ↔ PostgreSQL · Firebase · Groq</strong>
</p>

---

## Descripción

Este backend centraliza la lógica de negocio que no debe residir en el móvil:

- Registro y perfiles de **usuarios**, **pacientes** y **psicólogos**
- **Notas** del paciente visibles para su psicólogo asignado
- **Tareas** terapéuticas creadas por el psicólogo
- **Citas** con control de disponibilidad y estados
- **Chat** (metadatos y notificaciones push al recibir mensajes)
- **Fotos de perfil** almacenadas en disco y servidas por URL pública
- **Resumen IA** de notas y **detección asíncrona de riesgo** (solo notifica al psicólogo en nivel alto)
- Registro de tokens **FCM** para push

La app Android consume estos endpoints con token **Bearer** de Firebase Auth. En desarrollo local se puede usar un atajo de token `dev:<uid>:<email>` (solo con perfil `dev`).

---

## Stack tecnológico

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin 1.9 |
| Framework | Spring Boot 3.3.5 |
| API | Spring Web MVC, Bean Validation |
| Persistencia | Spring Data JPA, Hibernate |
| Base de datos | PostgreSQL (prod), H2 en memoria (perfil `dev`) |
| Migraciones | Flyway (`classpath:db/migration`) |
| Seguridad | Spring Security, filtro JWT Firebase, rate limit |
| Documentación API | SpringDoc OpenAPI (solo perfil `dev`) |
| Push / identidad | Firebase Admin SDK |
| IA | Groq API (OpenAI-compatible) |
| Contenedor | Docker multi-stage (JDK 17) |
| Despliegue | [Render](https://render.com) (`bdpsicologiaapp.onrender.com`) |

**Requisitos locales:** JDK 17, Gradle 8.x (wrapper incluido).

---

## Arquitectura del código

Paquete raíz `dam2.tfg.psicologiaapp.backend.bdPsicologiaApp`:

```
bdPsicologiaApp/
├── BdPsicologiaAppApplication.kt   # Punto de entrada (@EnableAsync)
├── config/                         # Firebase, Groq, OpenAPI, propiedades IA
├── domain/                         # Entidades JPA (Usuario, Psicologo, Paciente, Nota, Tarea, Cita, FcmToken…)
├── repository/                     # Spring Data JPA
├── service/                        # Reglas de negocio (IServicio*, implementaciones)
│   ├── ia/                         # Cliente Groq, DTOs
│   └── deteccionRiesgo/            # Análisis asíncrono de notas
├── security/                       # SecurityFilterChain, FirebaseTokenFilter, roles
└── web/                            # @RestController, DTOs, mappers, GlobalExceptionHandler
```

**Capas:** `web` → `service` → `repository` → `domain`. Los roles `ROLE_PACIENTE` y `ROLE_PSICOLOGO` se resuelven en `ServicioRoles` según existencia de fila en BD vinculada al `firebase_uid`.

---

## API REST (resumen)

Prefijo común: **`/api`**. Autenticación: **`Authorization: Bearer <token>`** (salvo rutas públicas indicadas).

| Controlador | Ruta base | Responsabilidad |
|-------------|-----------|-----------------|
| `UsuarioController` | `/api/usuarios` | Alta, perfil `/me`, foto, email, comprobación de correo |
| `PacienteController` | `/api/pacientes` | Alta paciente, búsqueda, asignación de psicólogo |
| `PsicologoController` | `/api/psicologos` | Alta profesional, búsqueda, pacientes asignados |
| `NotaController` | `/api/notas` | CRUD y sync de notas por rol |
| `TareaController` | `/api/tareas` | Tareas por paciente, estados de sincronización |
| `CitaController` | `/api/citas` | Disponibilidad, reserva, listados |
| `ChatController` | `/api/chats` | Asegurar sala, notificar mensaje |
| `ResumenIaController` | `/api/notas/pacientes/{id}/resumen-ia` | Resumen generado por IA (psicólogo) |
| `NotificacionesController` | `/api/notificaciones` | Alta/baja tokens FCM |
| `ArchivoPerfilController` | `/api/archivos/perfiles` | Descarga pública de fotos (GET) |
| `MantenerActivoController` | `/api/mantener-activo` | Health ping (sin auth) |

Documentación detallada de cada endpoint: [`doc/ENDPOINTS.md`](../../../../../../../doc/ENDPOINTS.md).

En **desarrollo**, Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Modelo de datos (PostgreSQL)

Entidades principales (ver migración `V1__schema_inicial.sql`):

- `usuarios` — identidad Firebase (`firebase_uid`), email, nombre, foto
- `psicologos` — número de colegiado, especialidad, descripción
- `pacientes_v2` — vínculo con usuario y psicólogo asignado
- `notas`, `tareas`, `citas` — con `ultima_modificacion` para sync incremental
- `fcm_tokens` — dispositivos para push

Migraciones adicionales en `src/main/resources/db/migration/`.

---

## Configuración y ejecución

### Desarrollo local (H2, sin Docker)

Perfil **`dev`**: base H2 en memoria, Hibernate `ddl-auto=update`, Flyway desactivado, Swagger habilitado.

```bash
cd bdPsicologiaApp
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Token de prueba en Swagger:**

```http
Authorization: Bearer dev:uid1:uid1@local.test
```

El servidor escucha en `http://localhost:8080`.

### Producción (PostgreSQL + Flyway)

Variables de entorno típicas:

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD |
| `PORT` | Puerto HTTP (Render lo inyecta) |
| `FIREBASE_CREDENTIALS` | JSON completo de cuenta de servicio (una línea) |
| `FIREBASE_PROJECT_ID` | Opcional si no está en el JSON |
| `FIREBASE_DATABASE_URL` | URL RTDB para notificaciones/chat |
| `GROQ_API_KEY` | Clave API Groq (resumen y riesgo) |
| `APP_URL_PUBLICA_BASE` | Origen público del API (URLs de fotos) |
| `APP_FOTOS_PERFIL_DIR` | Directorio de subida de fotos |
| `IA_RIESGO_HABILITADO` | `true` / `false` (default `true`) |

Sin `FIREBASE_CREDENTIALS`, la verificación real de tokens fallará. Sin `GROQ_API_KEY`, el resumen IA responde **503** y la detección de riesgo queda deshabilitada sin romper la creación de notas.

### Tests

```bash
./gradlew test
```

### Docker

```bash
docker build -t bdpsicologiaapp .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=... \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e FIREBASE_CREDENTIALS='...' \
  -e GROQ_API_KEY=... \
  bdpsicologiaapp
```

---

## Seguridad

- API **stateless** (sin sesión HTTP).
- Filtro `FirebaseTokenFilter` valida el ID token con Firebase Admin (o atajo `dev:` solo en perfil `dev`).
- Autorización por método (`@PreAuthorize`) según rol paciente/psicólogo.
- **Rate limiting** en cadena de filtros.
- Rutas públicas limitadas: comprobación de email, fotos de perfil (GET), `/api/mantener-activo`.
- CORS restringido en producción; en `dev` se permiten `localhost:8080` y `localhost:4200`.

La **API key de Groq nunca** debe exponerse al cliente Android.

---

## IA (Groq)

Configuración en `application.yaml` bajo `ia.groq` y `ia.riesgo`:

| Función | Comportamiento |
|---------|----------------|
| **Resumen de notas** | El psicólogo solicita un resumen del historial de notas de un paciente |
| **Detección de riesgo** | Tras crear/editar una nota (paciente), análisis **asíncrono**; si el nivel es **ALTO**, push al psicólogo (con ventana de deduplicación) |

Modelo por defecto: `llama-3.1-8b-instant`.

---

## Relación con el cliente Android

| Aspecto | Backend | App (`psicologiaapp`) |
|---------|---------|------------------------|
| Auth | Valida Firebase ID token | Firebase Auth SDK |
| API REST | Spring Boot `/api` | Retrofit + flavor `local` / `prod` |
| Chat tiempo real | Notificaciones push, metadatos | Firebase Realtime Database |
| IA | Groq en servidor | Solo consume `/resumen-ia` |

Diseño conjunto: repositorio Android → `doc/DISEÑO_SISTEMA.md`.

---

## Estructura del repositorio (raíz)

```
bdPsicologiaApp/
├── src/main/kotlin/.../bdPsicologiaApp/   # Código fuente (este README)
├── src/main/resources/
│   ├── application.yaml                   # Producción
│   ├── application-dev.yaml               # Desarrollo H2
│   └── db/migration/                      # Flyway
├── src/test/kotlin/                         # Tests unitarios / integración
├── doc/ENDPOINTS.md
├── Dockerfile
├── build.gradle.kts
└── gradlew
```

---

## Despliegue en Render

Imagen Docker publicada desde este repositorio. El servicio escucha en `0.0.0.0` y usa la variable `PORT` de la plataforma. URL de referencia del TFG:

`https://bdpsicologiaapp.onrender.com/`

Endpoint de mantenimiento de instancia (evitar sleep en plan gratuito): `GET /api/mantener-activo`.

---

## Autor y contexto académico

Proyecto **TFG** — ciclo **DAM2**.  
Grupo Maven: `dam2.tfg.psicologiaapp.backend` · Versión: `0.0.1-SNAPSHOT`.

---

## Licencia

Proyecto académico. Consulta con el autor antes de reutilizar o redistribuir fuera del ámbito del TFG.
