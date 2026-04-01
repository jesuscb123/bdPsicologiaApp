# ENDPOINTS (Backend `bdPsicologiaApp`)

Este documento describe **todos los endpoints expuestos** actualmente por la API (según controllers y el OpenAPI local en `/v3/api-docs`).

## Autenticación (Bearer)

- **Cabecera**: `Authorization: Bearer <token>`
- **Sin token**: los endpoints protegidos devuelven **401**.

### Token de desarrollo (perfil `dev`)

En local, con perfil `dev`, se acepta:

- `Authorization: Bearer dev:<uid>:<email>`

Ejemplo:

- `Authorization: Bearer dev:uid1:uid1@local.test`

> Importante: en Swagger pega exactamente eso (sin duplicar “Bearer”).

## Convenciones de respuesta (resumen)

- **200 OK**: operación correcta con cuerpo.
- **201 Created**: recurso creado.
- **204 No Content**: operación correcta pero lista vacía / sin contenido.
- **400 Bad Request**: validación o estado no permitido (según endpoint).
- **401 Unauthorized**: falta token o token inválido.
- **403 Forbidden**: rol incorrecto o acceso no permitido (según reglas de servicio/`@PreAuthorize`).
- **404 Not Found**: recurso inexistente.
- **409 Conflict**: el recurso ya existía o el estado no permite crear (casos de alta de usuario/rol).

---

## Notas (`/api/notas`)

### GET `/api/notas`

- **Qué hace**: devuelve las **notas del paciente autenticado**.
- **Rol**: `PACIENTE`.
- **Cómo llamarlo**:
  - Header `Authorization`.
- **Respuestas típicas**:
  - **200** lista de `NotaResponse`.
  - **204** si no hay notas.
- **Por qué existe**: el paciente solo debe ver sus notas, sin pasar `firebaseUid` por URL.

### POST `/api/notas`

- **Qué hace**: crea una nota del **paciente autenticado** dirigida a su psicólogo asignado.
- **Rol**: `PACIENTE`.
- **Body** (`NotaRequest`):

```json
{
  "asunto": "Asunto",
  "descripcion": "Texto de la nota"
}
```

- **Respuestas típicas**:
  - **201** con `NotaResponse`.
  - **400** si el estado del paciente no permite crear la nota (ej. sin psicólogo asignado, según lógica de servicio).
- **Por qué existe**: endpoint “oficial” para crear notas coherente con autenticación (no requiere `firebaseUid` en la URL).

### GET `/api/notas/pacientes/{pacienteId}`

- **Qué hace**: devuelve las notas de **un paciente concreto**, pero **solo si el paciente pertenece al psicólogo autenticado** (validación en servicio).
- **Rol**: `PSICOLOGO`.
- **Path params**:
  - `pacienteId` (Long): ID interno de `pacientes_v2`.
- **Respuestas típicas**:
  - **200** lista de notas.
  - **204** si el paciente no tiene notas.
  - **403** si el paciente no está asignado al psicólogo.
- **Por qué existe**: permite al psicólogo revisar notas por paciente sin exponer UIDs ni permitir accesos cruzados.

### PUT `/api/notas/{notaId}`

- **Qué hace**: actualiza una nota del paciente.
- **Rol**: `PACIENTE`.
- **Path params**: `notaId` (Long).
- **Body**: `NotaRequest` (mismo formato que POST).
- **Respuestas típicas**:
  - **200** nota actualizada.
  - **403** si la nota no pertenece al paciente autenticado.
  - **404** si no existe.
- **Por qué existe**: edición de notas propias.

### DELETE `/api/notas/{notaId}`

- **Qué hace**: elimina una nota del paciente.
- **Rol**: `PACIENTE`.
- **Path params**: `notaId` (Long).
- **Respuestas típicas**:
  - **204** si se elimina.
  - **403** si no pertenece.
  - **404** si no existe.
- **Por qué existe**: gestión del ciclo de vida de notas propias.

> Endpoint eliminado (legacy): **NO debe existir** `POST /api/notas/paciente/firebaseId/{firebaseId}`.

---

## Tareas (`/api/tareas`)

### GET `/api/tareas`

- **Qué hace**: devuelve las **tareas asignadas al paciente autenticado**.
- **Rol**: `PACIENTE`.
- **Respuestas típicas**:
  - **200** lista de tareas.
  - **204** si no hay tareas.
- **Por qué existe**: el paciente solo debe ver sus tareas.

### GET `/api/tareas/pacientes/{pacienteId}`

- **Qué hace**: devuelve las tareas de un paciente (vista del psicólogo).
- **Rol**: `PSICOLOGO`.
- **Path params**: `pacienteId` (Long).
- **Respuestas típicas**:
  - **200** lista de tareas.
  - **204** si no hay.
  - **403** si el paciente no pertenece al psicólogo (según lógica de servicio).
- **Por qué existe**: el psicólogo gestiona tareas por paciente asignado.

### POST `/api/tareas/pacientes/{pacienteId}`

- **Qué hace**: crea/asigna una tarea a un paciente.
- **Rol**: `PSICOLOGO`.
- **Path params**: `pacienteId` (Long).
- **Body** (`TareaCrearRequest`):

```json
{
  "titulo": "Título",
  "descripcion": "Descripción"
}
```

- **Respuestas típicas**:
  - **201** tarea creada.
  - **403** si el paciente no pertenece al psicólogo.
- **Por qué existe**: flujo principal de asignación de tareas.

### PATCH `/api/tareas/{tareaId}/realizada`

- **Qué hace**: marca una tarea como realizada/no realizada.
- **Rol**: `PACIENTE`.
- **Path params**: `tareaId` (Long).
- **Body** (`TareaActualizarRealizadaRequest`):

```json
{
  "realizada": true
}
```

- **Respuestas típicas**:
  - **200** tarea actualizada.
  - **403** si la tarea no pertenece al paciente.
- **Por qué existe**: el paciente actualiza su progreso sin poder editar el contenido.

### PUT `/api/tareas/{tareaId}`

- **Qué hace**: actualiza título/descripcion de una tarea.
- **Rol**: `PSICOLOGO`.
- **Path params**: `tareaId` (Long).
- **Body** (`TareaActualizarRequest`):

```json
{
  "titulo": "Nuevo título",
  "descripcion": "Nueva descripción"
}
```

- **Respuestas típicas**:
  - **200** tarea actualizada.
  - **403** si la tarea no pertenece al psicólogo autenticado.
  - **404** si no existe.
- **Por qué existe**: el psicólogo puede corregir/ajustar tareas asignadas.

### DELETE `/api/tareas/{tareaId}`

- **Qué hace**: elimina una tarea.
- **Rol**: `PSICOLOGO`.
- **Path params**: `tareaId` (Long).
- **Respuestas típicas**:
  - **204** eliminada.
  - **403** si no pertenece.
  - **404** si no existe.
- **Por qué existe**: el psicólogo puede retirar tareas asignadas.

---

## Pacientes (`/api/pacientes`)

### GET `/api/pacientes`

- **Qué hace**: lista pacientes.
- **Rol**: (no está anotado en controller; depende de la configuración global de seguridad).
- **Respuestas típicas**: **200** lista.
- **Por qué existe**: utilitario/listado (normalmente para administración o para psicólogo; recomendable protegerlo por rol si se expone en producción).

### POST `/api/pacientes/me`

- **Qué hace**: crea el rol **PACIENTE** para el usuario autenticado (si existe `Usuario`).
- **Rol**: (no está anotado; se apoya en autenticación y lógica de servicio).
- **Body** (`CrearPacienteMeRequest`):

```json
{
  "psicologoId": 1
}
```

`psicologoId` es opcional; puede ser `null` para crearte como paciente sin asignación inicial.

- **Respuestas típicas**:
  - **201** paciente creado.
  - **409** si no existe usuario previo o si ya era paciente.
- **Por qué existe**: permite añadir rol paciente sin recrear `Usuario` (mismo `firebaseUid`).

### GET `/api/pacientes/me`

- **Qué hace**: devuelve el **perfil de paciente** del usuario autenticado.
- **Rol**: `PACIENTE`.
- **Respuestas típicas**:
  - **200** `PacienteResponse`.
  - **404** si el usuario no tiene rol paciente creado todavía.
- **Por qué existe**: el frontend no debe pasar `firebaseUid` para “mi perfil paciente”.

### PATCH `/api/pacientes/me/psicologo`

- **Qué hace**: asigna/cambia el psicólogo del paciente autenticado.
- **Rol**: `PACIENTE`.
- **Body** (`AsignarPsicologoRequest`):

```json
{
  "psicologoId": 1
}
```

- **Respuestas típicas**:
  - **200** paciente actualizado.
  - **400** si el psicólogo es inválido o se intenta asignar a sí mismo (según reglas).
- **Por qué existe**: vínculo paciente-psicólogo gestionado por el propio paciente.

### GET `/api/pacientes/buscar?nombreUsuario=...`

- **Qué hace**: busca pacientes por nombre de usuario (contiene, case-insensitive).
- **Rol**: `PSICOLOGO`.
- **Query params**:
  - `nombreUsuario` (String).
- **Respuestas típicas**:
  - **200** lista (puede estar vacía).
- **Por qué existe**: UX de búsqueda para psicólogo.

### GET `/api/pacientes/firebaseId/{firebaseId}`

- **Qué hace**: obtiene un paciente por `firebaseUid`.
- **Rol**: (no está anotado).
- **Path params**:
  - `firebaseId`: uid de Firebase del usuario.
- **Respuestas típicas**:
  - **200** si existe.
  - **404** si no.
- **Por qué existe**: endpoint de lookup por UID (útil para integraciones o debugging). Para “mi perfil” se recomienda `GET /api/pacientes/me`.

### GET `/api/pacientes/id/{id}`

- **Qué hace**: obtiene un paciente por ID interno.
- **Rol**: (no está anotado).
- **Path params**: `id` (Long).
- **Respuestas típicas**:
  - **200** si existe.
  - **404** si no.
- **Por qué existe**: rutas internas que trabajan con IDs de BD.

---

## Psicólogos (`/api/psicologos`)

### GET `/api/psicologos`

- **Qué hace**: lista psicólogos.
- **Rol**: (no está anotado).
- **Respuestas típicas**: **200** lista.
- **Por qué existe**: listado/selección (por ejemplo, para asignación inicial).

### POST `/api/psicologos/me`

- **Qué hace**: crea el rol **PSICOLOGO** para el usuario autenticado (si existe `Usuario`).
- **Rol**: (no está anotado).
- **Body** (`CrearPsicologoMeRequest`):

```json
{
  "numeroColegiado": "COL-1234",
  "especialidad": "Psicologia clinica"
}
```

- **Respuestas típicas**:
  - **201** psicólogo creado.
  - **409** si no existe usuario previo o si ya era psicólogo.
- **Por qué existe**: añadir rol psicólogo sin duplicar usuario.

### GET `/api/psicologos/me`

- **Qué hace**: devuelve el **perfil de psicólogo** del usuario autenticado.
- **Rol**: `PSICOLOGO`.
- **Respuestas típicas**:
  - **200** `PsicologoResponse`.
  - **404** si el usuario no tiene rol psicólogo creado todavía.
- **Por qué existe**: “mi perfil psicólogo” sin pasar UID por URL.

### GET `/api/psicologos/me/pacientes`

- **Qué hace**: devuelve los pacientes asignados al psicólogo autenticado.
- **Rol**: `PSICOLOGO`.
- **Respuestas típicas**:
  - **200** lista de pacientes.
  - **204** si no tiene pacientes.
- **Por qué existe**: dashboard/listado de pacientes del psicólogo.

### GET `/api/psicologos/buscar?nombreUsuario=...`

- **Qué hace**: busca psicólogos por nombre de usuario.
- **Rol**: `PACIENTE`.
- **Query params**:
  - `nombreUsuario` (String).
- **Respuestas típicas**:
  - **200** lista (puede estar vacía).
- **Por qué existe**: UX para que el paciente encuentre psicólogo.

### GET `/api/psicologos/firebaseId/{firebaseId}`

- **Qué hace**: obtiene un psicólogo por `firebaseUid`.
- **Rol**: (no está anotado).
- **Respuestas típicas**: **200/404**.
- **Por qué existe**: lookup por UID (para “mi perfil” usar `GET /api/psicologos/me`).

### GET `/api/psicologos/id/{id}`

- **Qué hace**: obtiene un psicólogo por ID interno.
- **Rol**: (no está anotado).
- **Respuestas típicas**: **200/404**.
- **Por qué existe**: rutas internas por ID de BD.

---

## Usuarios (`/api/usuarios`)

### GET `/api/usuarios`

- **Qué hace**: lista usuarios.
- **Rol**: (no está anotado).
- **Respuestas típicas**: **200** lista.
- **Por qué existe**: administración/debug (recomendable protegerlo en producción).

### POST `/api/usuarios`

- **Qué hace**: crea el **Usuario base** (una sola vez por `firebaseUid`) usando el token actual para obtener `uid` y `email`.
- **Rol**: requiere autenticación (usa `@AuthenticationPrincipal`).
- **Body** (`UsuarioRequest` polimórfico por `rol`):
  - Si creas usuario tipo psicólogo:

```json
{
  "rol": "PSICOLOGO",
  "nombreUsuario": "psicologo1",
  "fotoPerfilUrl": null,
  "numeroColegiado": "COL-1234",
  "especialidad": "Psicologia clinica"
}
```

  - Si creas usuario tipo paciente:

```json
{
  "rol": "PACIENTE",
  "nombreUsuario": "paciente1",
  "fotoPerfilUrl": null,
  "psicologoId": null
}
```

- **Respuestas típicas**:
  - **201** usuario creado.
  - **409** si ya existía el usuario para ese `firebaseUid`.
- **Por qué existe**: alta inicial de `Usuario` (identidad base) que luego puede adquirir roles adicionales (`/pacientes/me`, `/psicologos/me`).

### GET `/api/usuarios/me`

- **Qué hace**: devuelve el perfil del usuario autenticado (incluye datos y roles).
- **Rol**: requiere autenticación.
- **Respuestas típicas**: **200**.
- **Por qué existe**: “mi perfil” sin pasar UID.

### PATCH `/api/usuarios/me/email`

- **Qué hace**: actualiza el email del usuario autenticado.
- **Rol**: requiere autenticación.
- **Body** (`ActualizarEmailRequest`):

```json
{
  "nuevoEmail": "nuevo@email.com"
}
```

- **Respuestas típicas**:
  - **200** perfil actualizado.
  - **400/409** según validación y conflictos.
- **Por qué existe**: mantener sincronizado/corregir email a nivel de BD.

### DELETE `/api/usuarios/me`

- **Qué hace**: elimina el usuario autenticado (y lo que la lógica de servicio decida en cascada).
- **Rol**: requiere autenticación.
- **Respuestas típicas**:
  - **204** eliminado.
  - **404** si no existe.
- **Por qué existe**: derecho de borrado / limpieza de cuenta.

### GET `/api/usuarios/{fireBaseUid}`

- **Qué hace**: obtiene un usuario por `firebaseUid`.
- **Rol**: (no está anotado).
- **Respuestas típicas**: **200/404**.
- **Por qué existe**: lookup por UID (administración/debug/integración).

